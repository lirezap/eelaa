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
package software.openx.eelaa.ledger;

import io.netty.buffer.ByteBuf;
import software.openx.eelaa.protocol.V1BinaryFrameDeserializable;
import software.openx.eelaa.protocol.V1BinaryFrameSerializable;
import software.openx.eelaa.util.StringUtil;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.openx.eelaa.util.ByteBufUtil.readNullTerminatedUTF8String;
import static software.openx.eelaa.util.MemorySegmentUtil.getString;
import static software.openx.eelaa.util.MemorySegmentUtil.putString;

/**
 * @author Alireza Pourtaghi
 */
public final class FailedTransaction implements
        V1BinaryFrameSerializable, V1BinaryFrameDeserializable<FailedTransaction> {

    public static final FailedTransaction DESERIALIZER = new FailedTransaction("", "");

    private final String id;
    private final String reason;

    public FailedTransaction(final String id, final String reason) {
        this.id = id == null ? "" : id;
        this.reason = reason == null ? "" : reason;
    }

    @Override
    public int dataLength() {
        return Math.addExact(
                StringUtil.requiredNullTerminatedUTF8BytesLength(id),
                StringUtil.requiredNullTerminatedUTF8BytesLength(reason));
    }

    @Override
    public void serialize(final MemorySegment memory, long position) {
        position = putString(memory, position, getId());
        putString(memory, position, getReason());
    }

    @Override
    public void serialize(final ByteBuf buffer) {
        buffer.writeCharSequence(getId(), UTF_8);
        buffer.writeZero(1);
        buffer.writeCharSequence(getReason(), UTF_8);
        buffer.writeZero(1);
    }

    @Override
    public FailedTransaction deserialize(final MemorySegment serialized, final AtomicLong position, final byte version,
                                         final byte flags, final int dataLength) {

        return new FailedTransaction(getString(serialized, position), getString(serialized, position));
    }

    @Override
    public FailedTransaction deserialize(final ByteBuf serialized, final byte version, final byte flags,
                                         final int dataLength) {

        return new FailedTransaction(readNullTerminatedUTF8String(serialized), readNullTerminatedUTF8String(serialized));
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }
}
