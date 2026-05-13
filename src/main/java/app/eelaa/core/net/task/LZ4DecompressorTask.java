package app.eelaa.core.net.task;

import app.eelaa.core.lz4.LZ4;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Task to decompress a provided byte buf using LZ4.
 *
 * @author Alireza Pourtaghi
 */
public final class LZ4DecompressorTask implements Runnable {
    private final ChannelHandlerContext ctx;
    private final ByteBuf buf;
    private final LZ4 lz4;
    private final int compressedSize;

    public LZ4DecompressorTask(final ChannelHandlerContext ctx, final ByteBuf buf, final LZ4 lz4,
                               final int compressedSize) {

        this.ctx = ctx;
        this.buf = buf;
        this.lz4 = lz4;
        this.compressedSize = compressedSize;
    }

    @Override
    public void run() {
        // TODO: Complete implementation.
    }
}
