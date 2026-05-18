package app.eelaa.core.net;

import app.eelaa.core.ping.PingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * Dispatcher implementation that selects appropriate handler for incoming frame.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class Dispatcher extends SimpleChannelInboundHandler<ByteBuf> {
    private final HandlerNotFoundException handlerNotFoundException;
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;

    public Dispatcher(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor) {
        super(false);
        this.handlerNotFoundException = new HandlerNotFoundException();
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        // We here will receive two kinds of buffers:
        // 1- Raw incoming buffer (6 bytes previously read)
        // 2- Decompressed data section
        final var frameNumericType = buf.readInt();
        final var sequenceId = buf.readInt();

        try {
            switch (frameNumericType) {
                case 100 -> new PingHandler(ctx, buf, frameNumericType, sequenceId).handle();
                default -> throw handlerNotFoundException;
            }
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }
}
