package software.openx.eelaa.storage;

import org.agrona.SystemUtil;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardOpenOption.*;
import static software.openx.eelaa.ValueLayouts.LONG_LE;

/**
 * An atomic file implementation based on {@link FileChannel}. The implementation holds the position value itself.
 *
 * @author Alireza Pourtaghi
 */
public final class AtomicFile implements AutoCloseable {
    private static final boolean IS_MAC = SystemUtil.isMac();

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
                openCreateReadWrite(filePath),
                -1);

        instance.prepareFileHeader();
        instance.adjustPosition();

        return instance;
    }

    public void write(final ByteBuffer buffer, final long position) throws IOException {
        Files.move(filePath, movePath, ATOMIC_MOVE);
        try (final var movedFile = openReadWrite(movePath)) {
            var bytesWritten = 0;
            while (buffer.hasRemaining()) {
                bytesWritten =
                        Math.addExact(bytesWritten, movedFile.write(buffer, Math.addExact(position, bytesWritten)));
            }

            incrementDurabilitySize(bytesWritten);
            final var fileHeaderAsBuffer = fileHeaderMemory.asByteBuffer();
            bytesWritten = 0;
            while (fileHeaderAsBuffer.hasRemaining()) {
                bytesWritten =
                        Math.addExact(bytesWritten, movedFile.write(fileHeaderAsBuffer, bytesWritten));
            }
            if (!IS_MAC) movedFile.force(false);
        } finally {
            try {
                Files.move(movePath, filePath, ATOMIC_MOVE);
            } catch (final Exception _) {
            }
        }
    }

    public void write(final MemorySegment segment, final long position) throws IOException {
        write(segment.asByteBuffer(), position);
    }

    public void append(final ByteBuffer buffer) throws IOException {
        Files.move(filePath, movePath, ATOMIC_MOVE);
        try (final var movedFile = openReadWrite(movePath)) {
            var bufferBytesWritten = 0;
            while (buffer.hasRemaining()) {
                bufferBytesWritten =
                        Math.addExact(bufferBytesWritten, movedFile.write(buffer, Math.addExact(position, bufferBytesWritten)));
            }

            incrementDurabilitySize(bufferBytesWritten);
            final var fileHeaderAsBuffer = fileHeaderMemory.asByteBuffer();
            var headerBytesWritten = 0;
            while (fileHeaderAsBuffer.hasRemaining()) {
                headerBytesWritten =
                        Math.addExact(headerBytesWritten, movedFile.write(fileHeaderAsBuffer, headerBytesWritten));
            }
            if (!IS_MAC) movedFile.force(false);

            try {
                position = Math.addExact(position, bufferBytesWritten);
            } catch (final ArithmeticException _) {
                // Noop! Next write operation will fail because of previous Math.addExact(position, bufferBytesWritten).
                // Let current write to be success.
            }
        } finally {
            try {
                Files.move(movePath, filePath, ATOMIC_MOVE);
            } catch (final Exception _) {
            }
        }
    }

    public void append(final MemorySegment segment) throws IOException {
        append(segment.asByteBuffer());
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

    private void writeAllBytes(final MemorySegment segment, final long position) throws IOException {
        writeAllBytes(segment.asByteBuffer(), position);
    }

    private void writeAllBytes(final ByteBuffer buffer, final long position) throws IOException {
        var bytesWritten = 0;
        while (buffer.hasRemaining()) {
            bytesWritten = Math.addExact(bytesWritten, file.write(buffer, Math.addExact(position, bytesWritten)));
        }

        if (!IS_MAC) file.force(false);
    }

    private void incrementDurabilitySize(final long incrementValue) {
        final var newValue = Math.addExact(fileHeaderMemory.get(LONG_LE, 0), incrementValue);
        fileHeaderMemory.set(LONG_LE, 0, newValue);
    }

    private void prepareFileHeader() throws IOException {
        try (final var _ = file.lock()) {
            if (file.size() == 0) {
                fileHeaderMemory.set(LONG_LE, 0, fileHeaderMemory.byteSize());
                writeAllBytes(fileHeaderMemory, 0);
            } else {
                read(fileHeaderMemory, 0);
            }
        }
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
            try (final var movedFile = openReadWriteSync(movePath)) {
                try (final var _ = movedFile.lock()) {
                    try (final var arena = Arena.ofConfined()) {
                        // 256 bytes file header.
                        final var header = arena.allocate(256);
                        if (header.byteSize() == movedFile.read(header.asByteBuffer().clear(), 0)) {
                            movedFile.truncate(header.get(LONG_LE, 0));
                            Files.move(movePath, filePath, ATOMIC_MOVE);
                        } else {
                            throw new IOException("incomplete read or file corrupted!");
                        }
                    }
                }
            }
        }
    }

    private static FileChannel openCreateReadWrite(final Path path) throws IOException {
        if (IS_MAC) {
            return FileChannel.open(path, CREATE, READ, WRITE, SYNC);
        }

        return FileChannel.open(path, CREATE, READ, WRITE);
    }

    private static FileChannel openReadWrite(final Path path) throws IOException {
        if (IS_MAC) {
            return FileChannel.open(path, READ, WRITE, SYNC);
        }

        return FileChannel.open(path, READ, WRITE);
    }

    private static FileChannel openReadWriteSync(final Path path) throws IOException {
        return FileChannel.open(path, READ, WRITE, SYNC);
    }

    @Override
    public void close() throws Exception {
        if (file.isOpen()) file.close();
        fileHeaderMemoryAllocator.close();
    }
}
