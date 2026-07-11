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
package software.openx.eelaa.net.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.ledger.Transaction;

import static software.openx.eelaa.net.FrameNumericType.ATOMIC_BATCH;
import static software.openx.eelaa.net.FrameNumericType.FAILED_TRANSACTIONS;

/**
 * @author Alireza Pourtaghi
 */
public final class AtomicBatchHandler extends BatchHandler {

    public AtomicBatchHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                              final int sequenceId, final Ledger ledger) {

        super(ctx, buf, frameNumericType, sequenceId, ledger);
    }

    @Override
    protected int frameNumericType() {
        return ATOMIC_BATCH.value();
    }

    @Override
    protected void process(final Transaction[] batch) throws Exception {
        if (getLedger().processAtomically(batch).get()) {
            respondProcessed(batch);
        } else {
            respondError("batch_process.failed");
        }
    }

    @Override
    protected void respondProcessed(final Transaction[] batch) {
        FailedTransaction failedTransaction = null;
        var length = 0;
        for (final var transaction : batch) {
            if (transaction.is_failed()) {
                failedTransaction = new FailedTransaction(transaction.getId(), transaction.get_failReason());
                length = failedTransaction.frameBinarySize();
                break;
            }
        }

        final var frameHeader = newV1FrameHeaderBuf(8, length);
        frameHeader.writeInt(FAILED_TRANSACTIONS.value());
        frameHeader.writeInt(getSequenceId());
        write(frameHeader);

        if (failedTransaction != null) {
            write(failedTransaction.encodeV1(getCtx().alloc()));
        }

        flush();
    }
}
