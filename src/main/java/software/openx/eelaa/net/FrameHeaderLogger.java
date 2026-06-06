package software.openx.eelaa.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler that logs the header section of incoming frame. Not recommended to be used in production environments.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class FrameHeaderLogger extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(FrameHeaderLogger.class);

    public FrameHeaderLogger() {
        super(false);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            log(buf);
            ctx.fireChannelRead(buf);
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private void log(final ByteBuf buf) {
        final var readerIndex = buf.readerIndex();
        logger.info("Frame received with size:{}, version:{}, flags:{}, length:{}",
                buf.readableBytes(),
                buf.getByte(readerIndex),
                buf.getByte(readerIndex + 1),
                buf.getInt(readerIndex + 2));
    }
}
