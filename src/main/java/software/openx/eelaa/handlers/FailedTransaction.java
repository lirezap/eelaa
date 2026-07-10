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
package software.openx.eelaa.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import software.openx.eelaa.memory.StringUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Alireza Pourtaghi
 */
final class FailedTransaction {
    private final String id;
    private final String reason;

    public FailedTransaction(final String id, final String reason) {
        this.id = id == null ? "" : id;
        this.reason = reason == null ? "" : reason;
    }

    private int binarySize() {
        return Math.addExact(
                StringUtil.requiredNullTerminatedUTF8BytesLength(id),
                StringUtil.requiredNullTerminatedUTF8BytesLength(reason));
    }

    public int frameBinarySize() {
        return Math.addExact(6, binarySize());
    }

    public ByteBuf encodeV1(final ByteBufAllocator allocator) {
        final var binarySize = binarySize();
        final var buffer = allocator.buffer(Math.addExact(6, binarySize));

        buffer.writeByte(0b00000001);
        buffer.writeByte(0b00000000);
        buffer.writeInt(binarySize);
        buffer.writeCharSequence(getId(), UTF_8);
        buffer.writeZero(1);
        buffer.writeCharSequence(getReason(), UTF_8);
        buffer.writeZero(1);

        return buffer;
    }

    public static FailedTransaction decode(final ByteBuf buf) {
        return new FailedTransaction(
                StringUtil.readNullTerminatedUTF8String(buf),
                StringUtil.readNullTerminatedUTF8String(buf)
        );
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }
}
