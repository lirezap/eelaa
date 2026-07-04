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
package software.openx.eelaa.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import software.openx.eelaa.handlers.*;
import software.openx.eelaa.ledger.Ledger;

/**
 * Dispatcher implementation that selects appropriate handler for incoming frame.
 *
 * @author Alireza Pourtaghi
 */
final class Dispatcher extends SimpleChannelInboundHandler<ByteBuf> {
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final Ledger ledger;
    private final SequenceIdsHolder sequenceIdsHolder;

    public Dispatcher(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final Ledger ledger) {
        super(false);
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.ledger = ledger;
        this.sequenceIdsHolder = new SequenceIdsHolder();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        // Here, we will receive two kinds of buffers:
        // 1- Raw incoming buffer (6 bytes previously read)
        // 2- Decompressed data section
        final var frameNumericType = buf.readInt();
        final var sequenceId = buf.readInt();
        final var ts = buf.readLong();

        try {
            if (!addSequenceId(sequenceId)) {
                throw BadSequenceIdException.INSTANCE;
            }

            if (!isValidTimestamp(ts)) {
                throw BadTimestampException.INSTANCE;
            }

            switch (FrameNumericType.of(frameNumericType)) {
                case PING -> cpuHeavyTaskExecutor.submit(new PingHandler(ctx, buf, frameNumericType, sequenceId));
                case FETCH_ACCOUNT ->
                        cpuHeavyTaskExecutor.submit(new FetchAccountHandler(ctx, buf, frameNumericType, sequenceId, ledger));
                case FETCH_WALLET ->
                        cpuHeavyTaskExecutor.submit(new FetchWalletHandler(ctx, buf, frameNumericType, sequenceId, ledger));
                case BATCH ->
                        cpuHeavyTaskExecutor.submit(new BatchHandler(ctx, buf, frameNumericType, sequenceId, ledger));
                case ATOMIC_BATCH ->
                        cpuHeavyTaskExecutor.submit(new AtomicBatchHandler(ctx, buf, frameNumericType, sequenceId, ledger));
                case FETCH_TRANSACTION ->
                        cpuHeavyTaskExecutor.submit(new FetchTransactionHandler(ctx, buf, frameNumericType, sequenceId, ledger));

                case null, default -> throw HandlerNotFoundException.INSTANCE;
            }
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private boolean addSequenceId(final int sequenceId) {
        return sequenceIdsHolder.addSequenceId(sequenceId);
    }

    private boolean isValidTimestamp(final long ts) {
        final var now = System.currentTimeMillis();
        return (now - 5000) < ts && ts < (now + 5000);
    }
}
