package software.openx.eelaa.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import software.openx.eelaa.ping.PingHandler;

/**
 * Dispatcher implementation that selects appropriate handler for incoming frame.
 *
 * @author Alireza Pourtaghi
 */
final class Dispatcher extends SimpleChannelInboundHandler<ByteBuf> {
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final SequenceIdsHolder sequenceIdsHolder;

    public Dispatcher(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor) {
        super(false);
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.sequenceIdsHolder = new SequenceIdsHolder();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        // Here, we will receive two kinds of buffers:
        // 1- Raw incoming buffer (6 bytes previously read)
        // 2- Decompressed data section
        final var frameNumericType = buf.readInt();
        final var sequenceId = buf.readInt();
        final var ts = buf.readLong();

        try {
            if (!addSequenceId(sequenceId)) {
                throw BadSequenceIdException.INSTANCE;
            }

            if (!isValidTimestamp(ts)) {
                throw BadTimestampException.INSTANCE;
            }

            switch (FrameNumericType.of(frameNumericType)) {
                case PING -> cpuHeavyTaskExecutor.submit(new PingHandler(ctx, buf, frameNumericType, sequenceId));
                case null, default -> throw HandlerNotFoundException.INSTANCE;
            }
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private boolean addSequenceId(final int sequenceId) {
        return sequenceIdsHolder.addSequenceId(sequenceId);
    }

    private boolean isValidTimestamp(final long ts) {
        final var now = System.currentTimeMillis();
        return (now - 5000) < ts && ts < (now + 5000);
    }
}
