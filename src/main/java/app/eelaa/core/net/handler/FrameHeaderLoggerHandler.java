package app.eelaa.core.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler that logs the header section of incoming frame. Not recommended to be used in production environments.
 *
 * @author Alireza Pourtaghi
 */
public final class FrameHeaderLoggerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(FrameHeaderLoggerHandler.class);

    public FrameHeaderLoggerHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        var readerIndex = buf.readerIndex();
        logger.info("Frame received with size:{}, version:{}, flags:{}, length:{}",
                buf.readableBytes(),
                buf.getByte(readerIndex),
                buf.getByte(readerIndex + 1),
                buf.getInt(readerIndex + 2));

        ctx.fireChannelRead(buf);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.channel().close();
        logger.error("{}", cause.getMessage(), cause);
    }
}
