package software.openx.eelaa.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.ledger.Transaction;
import software.openx.eelaa.net.Handler;

import static software.openx.eelaa.net.FrameNumericType.BATCH;
import static software.openx.eelaa.net.FrameNumericType.FAILED_TRANSACTIONS;

/**
 * @author Alireza Pourtaghi
 */
public sealed class BatchHandler extends Handler permits AtomicBatchHandler {
    private final Ledger ledger;

    public BatchHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                        final int sequenceId, final Ledger ledger) {

        super(ctx, buf, frameNumericType, sequenceId);
        this.ledger = ledger;
    }

    @Override
    protected int frameNumericType() {
        return BATCH.value();
    }

    @Override
    protected final boolean isValid() {
        return getBuf().readableBytes() > 0;
    }

    @Override
    protected final void handle() throws Exception {
        if (isValid()) {
            process(decodeBatch());
        } else {
            releaseFrameBufferThenClose();
        }
    }

    protected void process(final Transaction[] batch) throws Exception {
        if (ledger.process(batch).get()) {
            respondProcessed(batch);
        } else {
            respondError("batch_process.failed");
        }
    }

    protected void respondProcessed(final Transaction[] batch) {
        var failedCount = 0;
        for (final var transaction : batch) {
            if (transaction.is_failed()) failedCount++;
        }

        final var failedTransactions = new ByteBuf[failedCount];
        var index = 0;
        var length = 0;
        for (final var transaction : batch) {
            if (transaction.is_failed()) {
                final var failedTransaction = new FailedTransaction(transaction.getId(), transaction.get_failReason());
                failedTransactions[index++] = failedTransaction.encodeV1(getCtx().alloc());
                length = Math.addExact(length, failedTransaction.frameBinarySize());
            }
        }

        final var frameHeader = newV1FrameHeaderBuf(8, length);
        frameHeader.writeInt(FAILED_TRANSACTIONS.value());
        frameHeader.writeInt(getSequenceId());
        write(frameHeader);

        for (final var failedTransaction : failedTransactions) {
            write(failedTransaction);
        }

        flush();
    }

    private Transaction[] decodeBatch() {
        final var readerIndex = getBuf().readerIndex();
        var count = 0;
        // Counting number of items.
        while (getBuf().readableBytes() > 0) {
            getBuf().readByte();
            getBuf().readByte();

            final var length = getBuf().readInt();
            getBuf().readerIndex(Math.addExact(getBuf().readerIndex(), length));
            count++;
        }

        // Reset reader index.
        getBuf().readerIndex(readerIndex);

        final var batch = new Transaction[count];
        var index = 0;
        while (getBuf().readableBytes() > 0) {
            final var version = getBuf().readByte();
            final var flags = getBuf().readByte();
            final var length = getBuf().readInt();

            batch[index++] = Transaction.decode(getBuf().slice(getBuf().readerIndex(), length));
            getBuf().readerIndex(Math.addExact(getBuf().readerIndex(), length));
        }

        // Release incoming frame buffer.
        releaseFrameBuffer();

        return batch;
    }

    protected final Ledger getLedger() {
        return ledger;
    }
}
