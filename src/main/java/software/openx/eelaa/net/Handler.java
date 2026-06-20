package software.openx.eelaa.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.openx.eelaa.net.FrameNumericType.ERROR;
import static software.openx.eelaa.net.FrameNumericType.NOTHING;

/**
 * Basic required handling functionalities that every handler must implement.
 *
 * @author Alireza Pourtaghi
 */
public abstract class Handler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

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

        if (!canHandle()) throw InvalidHandlerException.INSTANCE;
    }

    @Override
    public final void run() {
        try {
            handle();
        } catch (final Exception ex) {
            logger.error("sequence id: {}, message: {}", sequenceId, ex.getMessage());

            releaseFrameBuffer();
            fireException(ex);
        }
    }

    /**
     * Determines that incoming buffer can be handled by this handler or not.
     *
     * @return true if it can be handled otherwise false
     */
    private boolean canHandle() {
        return frameNumericType == frameNumericType();
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
    protected abstract void handle() throws Exception;

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
     * Releases the incoming frame's buffer.
     */
    protected final void releaseFrameBuffer() {
        ReferenceCountUtil.release(buf);
    }

    /**
     * Releases frame buffer, then closes the connection.
     */
    protected final void releaseFrameBufferThenClose() {
        releaseFrameBuffer();
        close();
    }

    /**
     * Fires an exception into channel's pipeline.
     *
     * @param ex exception instance
     */
    private void fireException(final Exception ex) {
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
     * Creates a new buffer based on version one message format and compressed flag enabled.
     *
     * @param length data section length
     * @return newly created buffer
     */
    protected final ByteBuf newV1BufCompressed(final int length) {
        var buf = ctx.alloc().buffer(6 + length);
        buf.writeByte(0b00000001);
        buf.writeByte(0b00000001);
        buf.writeInt(length);

        return buf;
    }

    /**
     * Responds NOTHING model.
     */
    protected final void respondNothing() {
        writeAndFlush(newV1Buf(8).writeInt(NOTHING.value()).writeInt(sequenceId));
    }

    /**
     * Responds ERROR model.
     */
    protected final void respondError(final String code) {
        // Null terminated
        final var codeLength = code.getBytes(UTF_8).length + 1;
        final var error = ctx.alloc().buffer(18 + codeLength);
        error.writeByte(0b00000001);
        error.writeByte(0b00000000);
        error.writeInt(12 + codeLength);
        error.writeInt(ERROR.value());
        error.writeInt(getSequenceId());
        error.writeInt(codeLength);
        error.writeCharSequence(code, UTF_8);
        error.writeByte(0b00000000);

        writeAndFlush(error);
    }

    protected final ByteBuf getBuf() {
        return buf;
    }

    protected final int getSequenceId() {
        return sequenceId;
    }
}
