package app.eelaa.core.ping;

import app.eelaa.core.net.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import static app.eelaa.core.net.FrameNumericType.PING;
import static app.eelaa.core.net.FrameNumericType.PONG;

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
        // Ping message must not contain any extra data.
        return getBuf().readableBytes() == 0;
    }

    @Override
    protected void handle() {
        if (isValid()) {
            var response = newV1Buf(8);
            response.writeInt(PONG.value());
            response.writeInt(getSequenceId());
            writeAndFlushThenClose(response);
            releaseFrameBuffer();
        } else {
            close();
        }
    }
}
