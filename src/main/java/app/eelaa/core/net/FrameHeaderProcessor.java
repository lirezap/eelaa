package app.eelaa.core.net;

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
final class FrameHeaderProcessor extends SimpleChannelInboundHandler<ByteBuf> {
    private final FrameDataDecompressor frameDataDecompressor;

    public FrameHeaderProcessor(final FrameDataDecompressor frameDataDecompressor) {
        super(false);
        this.frameDataDecompressor = frameDataDecompressor;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            final var version = buf.readByte();
            if (!isV1FrameFormat(version)) {
                // TODO: Complete implementation.
                ctx.writeAndFlush(null);
                ReferenceCountUtil.release(buf);
                return;
            }

            final var flags = buf.readByte();
            if (isCompressed(flags)) {
                ctx.pipeline().addAfter(
                        this.getClass().getName(), frameDataDecompressor.getClass().getName(), frameDataDecompressor);
            }

            ctx.fireChannelRead(buf);
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private boolean isV1FrameFormat(final byte version) {
        return version == 0b00000001;
    }

    private boolean isCompressed(final byte flags) {
        return (flags & 0b00000001) == 0b00000001;
    }
}
