package software.openx.eelaa.handlers;

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
