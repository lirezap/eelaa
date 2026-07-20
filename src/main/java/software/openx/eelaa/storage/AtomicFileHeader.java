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

import software.openx.eelaa.memory.MemorySegmentUtil;

import java.io.EOFException;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;

import static software.openx.eelaa.memory.MemorySegmentUtil.LONG_LE;
import static software.openx.eelaa.storage.AtomicFile.IS_MAC;

/**
 * File header for atomic file identity and durability guarantee.
 *
 * @author Alireza Pourtaghi
 */
final class AtomicFileHeader implements AutoCloseable {
    private final Arena allocator;
    private final MemorySegment header;
    private final long magic;

    private AtomicFileHeader(final Arena allocator, final MemorySegment header, final long magic) {
        this.allocator = allocator;
        this.header = header;
        this.magic = magic;
    }

    public static AtomicFileHeader newInstance(final long magic) {
        final var allocator = Arena.ofConfined();
        final var header = allocator.allocate(256, 64);

        // Magic
        header.set(LONG_LE, 0, magic);

        // Generation
        header.set(LONG_LE, 8, 0);

        // Durability Size
        header.set(LONG_LE, 16, header.byteSize());

        // CRC
        header.set(LONG_LE, 24, MemorySegmentUtil.computeCRC32C(header.asSlice(0, 24)));

        // Second Record
        header.set(LONG_LE, 128, magic);
        header.set(LONG_LE, 136, 0);
        header.set(LONG_LE, 144, header.byteSize());
        header.set(LONG_LE, 152, MemorySegmentUtil.computeCRC32C(header.asSlice(128, 24)));

        return new AtomicFileHeader(allocator, header, magic);
    }

    public void persistHeader(final FileChannel file) throws IOException {
        final var headerByteBuffer = header.asByteBuffer();
        var bytesWritten = 0;
        while (headerByteBuffer.hasRemaining()) {
            bytesWritten = Math.addExact(bytesWritten, file.write(headerByteBuffer, bytesWritten));
        }

        if (!IS_MAC) file.force(true);
    }

    public void restoreHeader(final FileChannel file) throws IOException {
        final var headerByteBuffer = header.asByteBuffer();
        while (headerByteBuffer.hasRemaining()) {
            if (file.read(headerByteBuffer, headerByteBuffer.position()) <= 0) {
                break;
            }
        }

        if (headerByteBuffer.hasRemaining()) {
            throw new EOFException();
        }
    }

    public void incrementDurabilitySize(final FileChannel file, final long incrementSize) throws IOException {
        final var firstRecordGeneration = header.get(LONG_LE, 8);
        final var secondRecordGeneration = header.get(LONG_LE, 136);

        if (firstRecordGeneration <= secondRecordGeneration) {
            // First record is older.
            final var secondRecordDurabilitySize = header.get(LONG_LE, 144);
            header.set(LONG_LE, 8, Math.addExact(secondRecordGeneration, 1));
            header.set(LONG_LE, 16, Math.addExact(secondRecordDurabilitySize, incrementSize));
            header.set(LONG_LE, 24, MemorySegmentUtil.computeCRC32C(header.asSlice(0, 24)));
            persistFirstRecord(file);
        } else {
            // Second record is older.
            final var firstRecordDurabilitySize = header.get(LONG_LE, 16);
            header.set(LONG_LE, 136, Math.addExact(firstRecordGeneration, 1));
            header.set(LONG_LE, 144, Math.addExact(firstRecordDurabilitySize, incrementSize));
            header.set(LONG_LE, 152, MemorySegmentUtil.computeCRC32C(header.asSlice(128, 24)));
            persistSecondRecord(file);
        }
    }

    public long getDurabilitySize() throws IOException {
        final var isFirstRecordValid = (magic == header.get(LONG_LE, 0)) &&
                MemorySegmentUtil.isComputedCRC32CValid(header.asSlice(0, 24), header.get(LONG_LE, 24));

        final var isSecondRecordValid = (magic == header.get(LONG_LE, 128)) &&
                MemorySegmentUtil.isComputedCRC32CValid(header.asSlice(128, 24), header.get(LONG_LE, 152));

        if (isFirstRecordValid && isSecondRecordValid) {
            final var firstRecordGeneration = header.get(LONG_LE, 8);
            final var secondRecordGeneration = header.get(LONG_LE, 136);
            if (firstRecordGeneration <= secondRecordGeneration) {
                // First record is older -> return the value stored in second record.
                return header.get(LONG_LE, 144);
            } else {
                // Second record is older -> return the value stored in first record.
                return header.get(LONG_LE, 16);
            }
        } else if (isFirstRecordValid) {
            return header.get(LONG_LE, 16);
        } else if (isSecondRecordValid) {
            return header.get(LONG_LE, 144);
        } else {
            throw new IOException("file header corrupted!");
        }
    }

    private void persistFirstRecord(final FileChannel file) throws IOException {
        final var firstRecordByteBuffer = header.asSlice(0, 128).asByteBuffer();
        var bytesWritten = 0;
        while (firstRecordByteBuffer.hasRemaining()) {
            bytesWritten = Math.addExact(bytesWritten, file.write(firstRecordByteBuffer, bytesWritten));
        }

        if (!IS_MAC) file.force(true);
    }

    private void persistSecondRecord(final FileChannel file) throws IOException {
        final var secondRecordByteBuffer = header.asSlice(128, 128).asByteBuffer();
        var bytesWritten = 128;
        while (secondRecordByteBuffer.hasRemaining()) {
            bytesWritten = Math.addExact(bytesWritten, file.write(secondRecordByteBuffer, bytesWritten));
        }

        if (!IS_MAC) file.force(true);
    }

    @Override
    public void close() {
        allocator.close();
    }
}
