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
package software.openx.eelaa.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static software.openx.eelaa.memory.MemorySegmentUtil.putByteLE;
import static software.openx.eelaa.memory.MemorySegmentUtil.putIntLE;

/**
 * Version one of the binary frame format definition.
 *
 * @author Alireza Pourtaghi
 */
public interface V1BinaryFrameSerializable extends BinarySerializable {

    /**
     * The length of the data section for the binary frame format.
     *
     * @return length in bytes
     */
    int dataLength();

    /**
     * Serializes the data section.
     *
     * @param memory   allocated memory to be filled by implementor
     * @param position current position
     */
    void serialize(MemorySegment memory, long position);

    /**
     * Serializes the data section.
     *
     * @param buffer allocated buffer to be filled by implementor
     */
    void serialize(ByteBuf buffer);

    @Override
    default MemorySegment serialize(final Arena arena) {
        // version(1 byte) + flags(1 byte) + data length(4 bytes) + data(data-length bytes)

        final var dataLength = dataLength();
        final var memory = arena.allocate(Math.addExact(6, dataLength));

        var position = putByteLE(memory, 0, (byte) 0b00000001);
        position = putByteLE(memory, position, (byte) 0b00000000);
        position = putIntLE(memory, position, dataLength);
        serialize(memory, position);

        return memory;
    }

    @Override
    default ByteBuf serialize(final ByteBufAllocator allocator) {
        // version(1 byte) + flags(1 byte) + data length(4 bytes) + data(data-length bytes)

        final var dataLength = dataLength();
        final var buffer = allocator.buffer(Math.addExact(6, dataLength));

        buffer.writeByte(0b00000001);
        buffer.writeByte(0b00000000);
        buffer.writeInt(dataLength);
        serialize(buffer);

        return buffer;
    }

    default int frameSize() {
        return Math.addExact(6, dataLength());
    }
}
