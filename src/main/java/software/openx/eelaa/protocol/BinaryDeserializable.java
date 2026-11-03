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

/**
 * Denotes that an implementing class is binary deserializable.
 *
 * @param <T> the type of the deserialized object instance
 * @author Alireza Pourtaghi
 */
public interface BinaryDeserializable<T> {

    /**
     * Deserializes a binary representation into its object instance.
     *
     * @param serialized binary representation as a {@link MemorySegment}
     * @return an instance of type {@link T}
     */
    T deserialize(MemorySegment serialized);

    /**
     * Deserializes a binary representation into its object instance.
     *
     * @param serialized binary representation as a {@link ByteBuf}
     * @return an instance of type {@link T}
     */
    T deserialize(ByteBuf serialized);
}
