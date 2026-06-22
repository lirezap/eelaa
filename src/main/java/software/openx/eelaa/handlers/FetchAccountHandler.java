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
            final var account = ledger.fetchAccount(getBuf().readInt(), getBuf().readLong()).get();
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
