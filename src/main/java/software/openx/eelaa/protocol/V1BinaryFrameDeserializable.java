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

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicLong;

import static software.openx.eelaa.protocol.MemorySegmentUtil.getByteLE;
import static software.openx.eelaa.protocol.MemorySegmentUtil.getIntLE;

/**
 * Version one of the binary frame format deserialization definition.
 *
 * @author Alireza Pourtaghi
 */
public interface V1BinaryFrameDeserializable<T> extends BinaryDeserializable<T> {

    /**
     * Deserializes the data section.
     *
     * @param serialized the binary frame itself
     * @param position   the current position
     * @param version    the version of binary frame
     * @param flags      the flags of binary frame
     * @param dataLength the data section's length
     * @return a new instance of {@link T}
     */
    T deserialize(MemorySegment serialized, AtomicLong position, byte version, byte flags, int dataLength);

    /**
     * Deserializes the data section.
     *
     * @param serialized the binary frame itself
     * @param version    the version of binary frame
     * @param flags      the flags of binary frame
     * @param dataLength the data section's length
     * @return a new instance of {@link T}
     */
    T deserialize(ByteBuf serialized, byte version, byte flags, int dataLength);

    @Override
    default T deserialize(final MemorySegment serialized) {
        final var position = new AtomicLong(0);
        final var version = getByteLE(serialized, position);
        final var flags = getByteLE(serialized, position);
        final var dataLength = getIntLE(serialized, position);

        return deserialize(serialized, position, version, flags, dataLength);
    }

    @Override
    default T deserialize(final ByteBuf serialized) {
        final var version = serialized.readByte();
        final var flags = serialized.readByte();
        final var dataLength = serialized.readInt();

        return deserialize(serialized, version, flags, dataLength);
    }
}
