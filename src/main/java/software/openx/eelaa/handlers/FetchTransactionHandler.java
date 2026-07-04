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
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.memory.StringUtil;
import software.openx.eelaa.net.Handler;

import static software.openx.eelaa.net.FrameNumericType.FETCH_TRANSACTION;
import static software.openx.eelaa.net.FrameNumericType.TRANSACTION;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchTransactionHandler extends Handler {
    private final Ledger ledger;

    public FetchTransactionHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                                   final int sequenceId, final Ledger ledger) {

        super(ctx, buf, frameNumericType, sequenceId);
        this.ledger = ledger;
    }

    @Override
    protected int frameNumericType() {
        return FETCH_TRANSACTION.value();
    }

    @Override
    protected boolean isValid() {
        // Must at least include a 4 bytes ledger id.
        return getBuf().readableBytes() > 4;
    }

    @Override
    protected void handle() throws Exception {
        if (isValid()) {
            final var transaction = ledger.fetchTransaction(getBuf().readInt(), StringUtil.readNullTerminatedUTF8String(getBuf())).get();
            releaseFrameBuffer();

            if (transaction != null) {
                final var frameHeader = newV1FrameHeaderBuf(8, transaction.frameBinarySize());
                frameHeader.writeInt(TRANSACTION.value());
                frameHeader.writeInt(getSequenceId());
                write(frameHeader);
                writeAndFlush(transaction.encodeV1(getCtx().alloc()));
            } else {
                respondNothing();
            }
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
