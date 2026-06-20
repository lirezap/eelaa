package software.openx.eelaa.ledger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.net.Handler;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

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
            if (account != null) {
                if (!account.isEmpty()) {
                    try (final var arena = Arena.ofConfined()) {
                        var index = 0;
                        var allocationSize = 0L;

                        final var encodedWallets = new MemorySegment[account.size()];
                        for (final var wallet : account) {
                            final var encodedWallet = wallet.encodeV1(arena);
                            encodedWallets[index++] = encodedWallet;
                            allocationSize = Math.addExact(allocationSize, encodedWallet.byteSize());
                        }

                        final var response = newV1Buf((int) Math.addExact(8, allocationSize));
                        response.writeInt(ACCOUNT.value());
                        response.writeInt(getSequenceId());
                        for (final var encodedWallet : encodedWallets) {
                            response.writeBytes(encodedWallet.asByteBuffer());
                        }

                        writeAndFlush(response);
                    }
                } else {
                    respondNothing();
                }
            } else {
                respondNothing();
            }

            releaseFrameBuffer();
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
