package app.eelaa.core.net;

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
        // TODO: Add remote address data in log.
        switch (cause) {
            case FrameVersionNotSupportedException _ -> logger.error("frame version is not supported!");
            case FrameFlagsNotSupportedException _ -> logger.error("frame flags is not supported!");
            case InvalidFrameLengthException _ -> logger.error("invalid length value provided!");
            case InvalidActualSizeException _ -> logger.error("invalid actual size value provided!");
            case DecompressionFailedException _ -> logger.error("could not decompress the data appropriately!");
            case InvalidHandlerException _ -> logger.error("assigned handler can't be called for this frame!");
            case HandlerNotFoundException _ -> logger.error("could not find any handler to handle incoming frame!");
            default -> logger.error("exception: {}", cause.getMessage(), cause);
        }
    }
}
