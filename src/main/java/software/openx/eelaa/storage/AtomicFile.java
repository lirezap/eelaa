/*
 * Copyright 2026 Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.openx.eelaa.storage;

import org.agrona.SystemUtil;

import java.io.EOFException;
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

/**
 * An atomic file implementation based on {@link FileChannel}. The implementation holds the position value itself.
 *
 * @author Alireza Pourtaghi
 */
public final class AtomicFile implements AutoCloseable {
    public static final boolean IS_MAC = SystemUtil.isMac();

    private final Path filePath;
    private final Path movePath;
    private final AtomicFileHeader header;
    private final FileChannel file;
    private long position;

    private AtomicFile(final Path filePath, final Path movePath, final AtomicFileHeader header,
                       final FileChannel file, final long position) {

        this.filePath = filePath;
        this.movePath = movePath;
        this.header = header;
        this.file = file;
        this.position = position;
    }

    public static AtomicFile newInstance(final Path filePath, final long magic) throws IOException {
        requireNonNull(filePath);
        requireNonDirectory(filePath);

        final var movePath = filePath.resolveSibling(filePath.getFileName() + ".mv");
        recoverIfNeeded(filePath, movePath, magic);

        final var header = AtomicFileHeader.newInstance(magic);
        final var instance = new AtomicFile(filePath, movePath, header, openCreateReadWrite(filePath), -1);

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
            if (!IS_MAC) movedFile.force(true);
            header.incrementDurabilitySize(movedFile, bytesWritten);
        } finally {
            try {
                Files.move(movePath, filePath, ATOMIC_MOVE);
                syncDirectory(filePath);
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
            var bytesWritten = 0;
            while (buffer.hasRemaining()) {
                bytesWritten =
                        Math.addExact(bytesWritten, movedFile.write(buffer, Math.addExact(position, bytesWritten)));
            }
            if (!IS_MAC) movedFile.force(true);
            header.incrementDurabilitySize(movedFile, bytesWritten);

            try {
                position = Math.addExact(position, bytesWritten);
            } catch (final ArithmeticException _) {
                // Noop! Next write operation will fail because of previous Math.addExact(position, bufferBytesWritten).
                // Let current write to be success.
            }
        } finally {
            try {
                Files.move(movePath, filePath, ATOMIC_MOVE);
                syncDirectory(filePath);
            } catch (final Exception _) {
            }
        }
    }

    public void append(final MemorySegment segment) throws IOException {
        append(segment.asByteBuffer());
    }

    public MemorySegment read(final Arena arena, final long position, final long size) throws IOException {
        final var segment = arena.allocate(size);
        final var buffer = segment.asByteBuffer();

        while (buffer.hasRemaining()) {
            if (file.read(buffer, position + buffer.position()) <= 0) {
                break;
            }
        }

        if (buffer.hasRemaining()) {
            throw new EOFException();
        }

        return segment;
    }

    public void read(final MemorySegment segment, final long position) throws IOException {
        final var buffer = segment.asByteBuffer();

        while (buffer.hasRemaining()) {
            if (file.read(buffer, position + buffer.position()) <= 0) {
                break;
            }
        }

        if (buffer.hasRemaining()) {
            throw new EOFException();
        }
    }

    public long size() throws IOException {
        return file.size();
    }

    private void prepareFileHeader() throws IOException {
        try (final var _ = file.lock()) {
            if (file.size() == 0) {
                header.persistHeader(file);
            } else {
                header.restoreHeader(file);
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

    private static void recoverIfNeeded(final Path filePath, final Path movePath, final long magic) throws IOException {
        if (!Files.exists(filePath) && Files.exists(movePath)) {
            // System crash or non-graceful shutdown?!
            try (final var movedFile = openReadWriteSync(movePath)) {
                try (final var _ = movedFile.lock()) {
                    try (final var header = AtomicFileHeader.newInstance(magic)) {
                        header.restoreHeader(movedFile);
                        movedFile.truncate(header.getDurabilitySize());

                        Files.move(movePath, filePath, ATOMIC_MOVE);
                        syncDirectory(filePath.getParent());
                    }
                }
            }
        }
    }

    private static void syncDirectory(final Path path) throws IOException {
        try (final var directory = FileChannel.open(path, READ)) {
            directory.force(true);
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
        header.close();
    }
}
