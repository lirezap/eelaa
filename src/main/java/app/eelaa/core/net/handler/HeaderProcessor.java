package app.eelaa.core.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * A handler that processes incoming frame's header both for validation and flags extraction.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
public final class HeaderProcessor extends SimpleChannelInboundHandler<ByteBuf> {

    public HeaderProcessor() {
        super(false);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            // Only v1 frame format is supported currently.
            if (!isV1FrameFormat(buf)) {
                // TODO: Complete implementation.
                ctx.writeAndFlush(null);
                ReferenceCountUtil.release(buf);
                return;
            }

            // Should we decompress data section?
            if (isCompressed(buf)) {
                // TODO: Complete implementation.
                ctx.fireChannelRead(null);
                ReferenceCountUtil.release(buf);
                return;
            }

            ctx.fireChannelRead(buf);
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private boolean isV1FrameFormat(final ByteBuf buf) {
        return buf.getByte(buf.readerIndex()) == 0b00000001;
    }

    private boolean isCompressed(final ByteBuf buf) {
        return (buf.getByte(buf.readerIndex() + 1) & 0b00000001) == 0b00000001;
    }
}
