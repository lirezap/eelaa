package app.eelaa.core.net;

import app.eelaa.core.net.exception.FrameVersionNotSupportedException;
import app.eelaa.core.net.exception.InvalidActualSizeException;
import app.eelaa.core.net.exception.InvalidFrameLengthException;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tail handler that logs exception thrown by handlers in pipeline.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class InboundExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(InboundExceptionHandler.class);

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        // No need to respond, just close the client socket channel.
        ctx.close();
        log(cause);
    }

    private void log(final Throwable cause) {
        if (cause instanceof FrameVersionNotSupportedException) {
            logger.error("frame version is not supported!");
        } else if (cause instanceof InvalidFrameLengthException) {
            logger.error("invalid length value provided!");
        } else if (cause instanceof InvalidActualSizeException) {
            logger.error("invalid actual size value provided!");
        } else {
            logger.error("exception: {}", cause.getMessage(), cause);
        }
    }
}
