package app.eelaa.core.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * A handler that decompresses incoming frame's data section.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class FrameDataDecompressor extends SimpleChannelInboundHandler<ByteBuf> {
    private final EventExecutorGroup cpuHeavyTaskExecutor;

    public FrameDataDecompressor(final EventExecutorGroup cpuHeavyTaskExecutor) {
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
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
