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
import software.openx.eelaa.net.Handler;

import static software.openx.eelaa.net.FrameNumericType.ACCOUNT;
import static software.openx.eelaa.net.FrameNumericType.FETCH_ACCOUNT;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchAccountHandler extends Handler {
    private final Ledger ledger;

    public FetchAccountHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                               final int sequenceId, final Ledger ledger) {

        super(ctx, buf, frameNumericType, sequenceId);
        this.ledger = ledger;
    }

    @Override
    protected int frameNumericType() {
        return FETCH_ACCOUNT.value();
    }

    @Override
    protected boolean isValid() {
        // Must only include ledger (4 bytes) and account (8 bytes) identifiers.
        return getBuf().readableBytes() == 12;
    }

    @Override
    protected void handle() throws Exception {
        if (isValid()) {
            final var account =
                    ledger.fetchAccount(getBuf().readInt(), getBuf().readLong()).get();

            releaseFrameBuffer();

            if (account != null && !account.isEmpty()) {
                var length = 0;
                for (final var wallet : account) {
                    length = Math.addExact(length, wallet.frameBinarySize());
                }

                final var frameHeader = newV1FrameHeaderBuf(8, length);
                frameHeader.writeInt(ACCOUNT.value());
                frameHeader.writeInt(getSequenceId());
                write(frameHeader);

                for (final var wallet : account) {
                    write(wallet.encodeV1(getCtx().alloc()));
                }

                flush();
            } else {
                respondNothing();
            }
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
