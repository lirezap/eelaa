package software.openx.eelaa.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.net.Handler;

import static software.openx.eelaa.net.FrameNumericType.PING;
import static software.openx.eelaa.net.FrameNumericType.PONG;

/**
 * @author Alireza Pourtaghi
 */
public final class PingHandler extends Handler {

    public PingHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                       final int sequenceId) {

        super(ctx, buf, frameNumericType, sequenceId);
    }

    @Override
    protected int frameNumericType() {
        return PING.value();
    }

    @Override
    protected boolean isValid() {
        // Must not contain any extra data.
        return getBuf().readableBytes() == 0;
    }

    @Override
    protected void handle() throws Exception {
        if (isValid()) {
            releaseFrameBuffer();

            final var response = newV1Buf(8);
            response.writeInt(PONG.value());
            response.writeInt(getSequenceId());
            writeAndFlush(response);
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
