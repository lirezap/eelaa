package app.eelaa.core.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tail handler that logs exception thrown by handlers in pipeline.
 *
 * @author Alireza Pourtaghi
 */
public final class InboundExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(InboundExceptionHandler.class);

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        logger.error("Exception: {}", cause.getMessage(), cause);
        // TODO: Complete implementation.
        ctx.writeAndFlush(null);
    }
}
