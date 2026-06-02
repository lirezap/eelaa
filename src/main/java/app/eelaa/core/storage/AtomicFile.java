package app.eelaa.core.storage;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardOpenOption.*;

/**
 * An atomic file implementation based on {@link FileChannel}. The implementation holds the position value itself.
 *
 * @author Alireza Pourtaghi
 */
final class AtomicFile implements AutoCloseable {
    private final Path filePath;
    private final Path movePath;
    private final Arena fileHeaderMemoryAllocator;
    private final MemorySegment fileHeaderMemory;
    private final FileChannel file;
    private long position;

    private AtomicFile(final Path filePath, final Path movePath, final Arena fileHeaderMemoryAllocator,
                       final MemorySegment fileHeaderMemory, final FileChannel file, final long position) {

        this.filePath = filePath;
        this.movePath = movePath;
        this.fileHeaderMemoryAllocator = fileHeaderMemoryAllocator;
        this.fileHeaderMemory = fileHeaderMemory;
        this.file = file;
        this.position = position;
    }

    public static AtomicFile newInstance(final Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonDirectory(filePath);

        final var movePath = filePath.resolveSibling(filePath.getFileName() + ".mv");
        recoverIfNeeded(filePath, movePath);

        final var fileHeaderMemoryAllocator = Arena.ofConfined();
        final var fileHeaderMemory = fileHeaderMemoryAllocator.allocate(256);
        final var instance = new AtomicFile(
                filePath,
                movePath,
                fileHeaderMemoryAllocator,
                fileHeaderMemory,
                FileChannel.open(filePath, CREATE, READ, WRITE),
                -1);

        instance.prepareFileHeader();
        instance.adjustPosition();

        return instance;
    }

    public MemorySegment read(final Arena arena, final long position, final long size) throws IOException {
        final var segment = arena.allocate(size);
        file.read(segment.asByteBuffer().clear(), position);
        return segment;
    }

    public void read(final MemorySegment segment, final long position) throws IOException {
        file.read(segment.asByteBuffer().clear(), position);
    }

    public long size() throws IOException {
        return file.size();
    }

    private void prepareFileHeader() throws IOException {
        try (final var _ = file.lock()) {
            if (file.size() == 0) {
                fileHeaderMemory.set(JAVA_LONG, 0, fileHeaderMemory.byteSize());
                writeAllBytes(fileHeaderMemory.asByteBuffer(), 0);
            } else {
                read(fileHeaderMemory, 0);
            }
        }
    }

    private void writeAllBytes(final ByteBuffer buffer, final long position) throws IOException {
        var bytesWritten = 0;
        while (buffer.hasRemaining()) {
            bytesWritten += file.write(buffer, position + bytesWritten);
        }

        file.force(true);
    }

    private void adjustPosition() throws IOException {
        try (final var _ = file.lock()) {
            position = file.size();
        }
    }

    private static void requireNonNull(final Path filePath) {
        Objects.requireNonNull(filePath);
    }

    private static void requireNonDirectory(final Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            throw new IOException("provided path is not a file, it is a directory!");
        }
    }

    private static void recoverIfNeeded(final Path filePath, final Path movePath) throws IOException {
        if (!Files.exists(filePath) && Files.exists(movePath)) {
            // System crash or non-graceful shutdown?!
            try (final var movedFile = FileChannel.open(movePath, READ, WRITE, SYNC)) {
                try (final var _ = movedFile.lock()) {
                    try (final var arena = Arena.ofConfined()) {
                        // 256 bytes file header.
                        final var header = arena.allocate(256);
                        if (header.byteSize() == movedFile.read(header.asByteBuffer().clear(), 0)) {
                            movedFile.truncate(header.get(JAVA_LONG, 0));
                            Files.move(movePath, filePath, ATOMIC_MOVE);
                        } else {
                            throw new IOException("incomplete read or file corrupted!");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (file.isOpen()) file.close();
        fileHeaderMemoryAllocator.close();
    }
}
