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
    protected void handle() {
        if (isValid()) {
            ledger.fetchAccount(getBuf().readInt(), getBuf().readLong())
                    .thenAccept(account -> {
                        if (account != null) {
                            try (final var arena = Arena.ofConfined()) {
                                var index = 0;
                                var allocationSize = 0L;

                                final var wallets = new MemorySegment[account.size()];
                                for (final var wallet : account) {
                                    final var segment = wallet.encodeV1(arena);
                                    wallets[index++] = segment;
                                    allocationSize += segment.byteSize();
                                }

                                final var buf = newV1Buf(8 + (int) allocationSize);
                                buf.writeInt(ACCOUNT.value());
                                buf.writeInt(getSequenceId());
                                for (final var wallet : wallets) {
                                    buf.writeBytes(wallet.asByteBuffer());
                                }

                                writeAndFlush(buf);
                            }
                        } else {
                            respondNothing();
                        }
                    });

            releaseFrameBuffer();
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
