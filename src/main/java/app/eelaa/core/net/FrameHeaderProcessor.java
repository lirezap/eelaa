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
            validateHeaderVersion(buf);
            addFrameDataDecompressorIfNeeded(ctx, buf);
            ctx.fireChannelRead(buf);
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private void validateHeaderVersion(final ByteBuf buf) {
        final var version = buf.readByte();
        if (version != 0b00000001) {
            throw new FrameVersionNotSupportedException();
        }
    }

    private void addFrameDataDecompressorIfNeeded(final ChannelHandlerContext ctx, final ByteBuf buf) {
        final var flags = buf.readByte();
        if ((flags & 0b00000001) == 0b00000001) {
            ctx.pipeline().addAfter(
                    this.getClass().getName(), frameDataDecompressor.getClass().getName(), frameDataDecompressor);
        }
    }
}
