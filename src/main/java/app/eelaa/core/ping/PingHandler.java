package app.eelaa.core.ping;

import app.eelaa.core.net.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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
        return 100;
    }

    @Override
    protected boolean isValid() {
        // Ping message must not contain any extra data.
        return getBuf().readableBytes() == 0;
    }

    @Override
    public void handle() {
        if (isValid()) {
            var response = newV1Buf(8);
            response.writeInt(101);
            response.writeInt(getSequenceId());
            writeAndFlushThenClose(response);
        } else {
            close();
        }
    }
}
