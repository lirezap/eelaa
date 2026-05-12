package app.eelaa.core.net;

import app.eelaa.core.lz4.LZ4;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * A handler that decompresses incoming frame's data section.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class FrameDataDecompressor extends SimpleChannelInboundHandler<ByteBuf> {
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final LZ4 lz4;

    public FrameDataDecompressor(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4) {
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.lz4 = lz4;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            // TODO: Complete implementation.
            ctx.writeAndFlush(null);
        } finally {
            // Exception propagates into pipeline and reaches inbound exception handler.
            ctx.pipeline().remove(this);
        }
    }
}
