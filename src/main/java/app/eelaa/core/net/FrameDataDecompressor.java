package app.eelaa.core.net;

import app.eelaa.core.lz4.LZ4;
import app.eelaa.core.net.exception.InvalidLengthException;
import app.eelaa.core.net.listener.ReleaseByteBufFutureListener;
import app.eelaa.core.net.task.LZ4DecompressorTask;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * A handler that decompresses incoming frame's data section.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class FrameDataDecompressor extends SimpleChannelInboundHandler<ByteBuf> {
    private final InvalidLengthException invalidLengthException;
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final LZ4 lz4;

    public FrameDataDecompressor(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4) {
        super(false);
        this.invalidLengthException = new InvalidLengthException();
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.lz4 = lz4;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            final var length = extractLength(buf);
            submitDecompressorTask(ctx, buf, length);
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        } finally {
            // Exception propagates into pipeline and reaches inbound exception handler.
            ctx.pipeline().remove(this);
        }
    }

    private int extractLength(final ByteBuf buf) {
        final var length = buf.readInt();
        if (length < 1) {
            throw invalidLengthException;
        }

        return length;
    }

    private void submitDecompressorTask(final ChannelHandlerContext ctx, final ByteBuf buf, final int length) {
        cpuHeavyTaskExecutor
                .submit(new LZ4DecompressorTask(ctx, buf, lz4, length))
                .addListener(new ReleaseByteBufFutureListener(buf));
    }
}
