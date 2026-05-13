package app.eelaa.core.net.task;

import app.eelaa.core.lz4.LZ4;
import app.eelaa.core.net.exception.DecompressFailedException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.lang.foreign.MemorySegment;

/**
 * Task to decompress a provided byte buf using LZ4.
 *
 * @author Alireza Pourtaghi
 */
public final class LZ4DecompressorTask implements Runnable {
    private final ChannelHandlerContext ctx;
    private final ByteBuf buf;
    private final LZ4 lz4;
    private final int actualSize;

    public LZ4DecompressorTask(final ChannelHandlerContext ctx, final ByteBuf buf, final LZ4 lz4,
                               final int actualSize) {

        this.ctx = ctx;
        this.buf = buf;
        this.lz4 = lz4;
        this.actualSize = actualSize;
    }

    @Override
    public void run() {
        try {
            final var readerIndex = buf.readerIndex();
            final var readableBytes = buf.readableBytes();
            final var newBuf = ctx.alloc().directBuffer(actualSize);

            final var src = MemorySegment.ofBuffer(buf.internalNioBuffer(readerIndex, readableBytes));
            final var dst = MemorySegment.ofBuffer(newBuf.internalNioBuffer(0, actualSize));

            if (lz4.decompressSafe(src, dst, readableBytes, actualSize) < 0) {
                throw new DecompressFailedException();
            }

            newBuf.writeInt(actualSize);
            ctx.fireChannelRead(newBuf);
        } catch (final Throwable cause) {
            ctx.fireExceptionCaught(cause);
        }
    }
}
