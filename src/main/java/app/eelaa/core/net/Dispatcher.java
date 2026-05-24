package app.eelaa.core.net;

import app.eelaa.core.ping.PingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

/**
 * Dispatcher implementation that selects appropriate handler for incoming frame.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class Dispatcher extends SimpleChannelInboundHandler<ByteBuf> {
    private final BadSequenceIdException badSequenceIdException;
    private final HandlerNotFoundException handlerNotFoundException;
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final AttributeKey<SequenceIdsHolder> sequenceIdsHolderKey;

    public Dispatcher(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor) {
        super(false);
        this.badSequenceIdException = new BadSequenceIdException();
        this.handlerNotFoundException = new HandlerNotFoundException();
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.sequenceIdsHolderKey = AttributeKey.newInstance("sequenceIdsHolderKey");
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        // Here, we will receive two kinds of buffers:
        // 1- Raw incoming buffer (6 bytes previously read)
        // 2- Decompressed data section
        final var frameNumericType = buf.readInt();
        final var sequenceId = buf.readInt();

        try {
            if (!addSequenceId(ctx, sequenceId)) {
                throw badSequenceIdException;
            }

            switch (FrameNumericType.of(frameNumericType)) {
                case PING -> cpuHeavyTaskExecutor.submit(new PingHandler(ctx, buf, frameNumericType, sequenceId));
                case null, default -> throw handlerNotFoundException;
            }
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private boolean addSequenceId(final ChannelHandlerContext ctx, final int sequenceId) {
        final var sequenceIdsHolderValue = ctx.channel().attr(sequenceIdsHolderKey);
        if (sequenceIdsHolderValue.get() == null) {
            sequenceIdsHolderValue.set(new SequenceIdsHolder());
        }

        return sequenceIdsHolderValue.get().addSequenceId(sequenceId);
    }
}
