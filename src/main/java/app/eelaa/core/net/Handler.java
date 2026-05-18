package app.eelaa.core.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * Basic required handling functionalities that every handler must implement.
 *
 * @author Alireza Pourtaghi
 */
public abstract class Handler {
    private static final InvalidHandlerException invalidHandlerException = new InvalidHandlerException();

    private final ChannelHandlerContext ctx;
    private final ByteBuf buf;
    private final int frameNumericType;
    private final int sequenceId;

    public Handler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                   final int sequenceId) {

        this.ctx = ctx;
        this.buf = buf;
        this.frameNumericType = frameNumericType;
        this.sequenceId = sequenceId;

        if (!canHandle()) throw invalidHandlerException;
    }

    /**
     * Determines that incoming buffer can be handled by this handler or not.
     *
     * @return true if it can be handled otherwise false
     */
    private boolean canHandle() {
        return frameNumericType() == frameNumericType;
    }

    /**
     * For which frame numeric type this handler is designed for?
     *
     * @return frame numeric type value
     */
    protected abstract int frameNumericType();

    /**
     * Determines that incoming buffer is a valid buffer for method handle to be called with or not.
     *
     * @return true if it can be handled otherwise false
     */
    protected abstract boolean isValid();

    /**
     * Main handling functionality implementation.
     */
    public abstract void handle();

    /**
     * Writes buf into channel.
     *
     * @param response response buffer to be written
     */
    protected final void write(final ByteBuf response) {
        ctx.write(response);
    }

    /**
     * Writes and flushes buf into channel.
     *
     * @param response response buffer to be written
     */
    protected final void writeAndFlush(final ByteBuf response) {
        ctx.writeAndFlush(response);
    }

    /**
     * Writes and flushes buf into channel then closes the connection.
     *
     * @param response response buffer to be written
     */
    protected final void writeAndFlushThenClose(final ByteBuf response) {
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Fires an exception into channel's pipeline.
     *
     * @param ex exception instance
     */
    protected final void fireException(final Exception ex) {
        ctx.fireExceptionCaught(ex);
    }

    /**
     * Closes the connection.
     */
    protected final void close() {
        ctx.close();
    }

    /**
     * Creates a new buffer based on version one message format.
     *
     * @param length data section length
     * @return newly created buffer
     */
    protected final ByteBuf newV1Buf(final int length) {
        var buf = ctx.alloc().buffer(6 + length);
        buf.writeByte(0b00000001);
        buf.writeByte(0b00000000);
        buf.writeInt(length);

        return buf;
    }

    /**
     * Releases the incoming frame's buffer.
     */
    protected final void releaseFrameBuffer() {
        ReferenceCountUtil.release(buf);
    }

    protected final ByteBuf getBuf() {
        return buf;
    }

    protected final int getSequenceId() {
        return sequenceId;
    }
}
