package software.openx.eelaa.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.net.Handler;

import static software.openx.eelaa.net.FrameNumericType.FETCH_WALLET;
import static software.openx.eelaa.net.FrameNumericType.WALLET;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchWalletHandler extends Handler {
    private final Ledger ledger;

    public FetchWalletHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                              final int sequenceId, final Ledger ledger) {

        super(ctx, buf, frameNumericType, sequenceId);
        this.ledger = ledger;
    }

    @Override
    protected int frameNumericType() {
        return FETCH_WALLET.value();
    }

    @Override
    protected boolean isValid() {
        // Must only include ledger (4 bytes), account (8 bytes) and wallet (4 bytes) identifiers.
        return getBuf().readableBytes() == 16;
    }

    @Override
    protected void handle() throws Exception {
        if (isValid()) {
            final var wallet = ledger.fetchWallet(getBuf().readInt(), getBuf().readLong(), getBuf().readInt()).get();
            releaseFrameBuffer();

            if (wallet != null) {
                final var frameHeader = newV1FrameHeaderBuf(8, wallet.frameBinarySize());
                frameHeader.writeInt(WALLET.value());
                frameHeader.writeInt(getSequenceId());
                write(frameHeader);
                writeAndFlush(wallet.encodeV1(getCtx().alloc()));
            } else {
                respondNothing();
            }
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
