/*
 * Copyright 2026 Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.openx.eelaa.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.memory.StringUtil;

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
     * Flushes the channel.
     */
    protected final void flush() {
        ctx.flush();
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
        final var buf = ctx.alloc().buffer(6 + length);
        buf.writeByte(0b00000001);
        buf.writeByte(0b00000000);
        buf.writeInt(length);

        return buf;
    }

    /**
     * Creates a new frame header buffer based on version one message format.
     *
     * @param headerLengthOfDataSection  the length of header in the data section of frame
     * @param contentLengthOfDataSection the length of content in the data section of frame
     * @return newly created buffer
     */
    protected final ByteBuf newV1FrameHeaderBuf(final int headerLengthOfDataSection,
                                                final int contentLengthOfDataSection) {

        final var buf = ctx.alloc().buffer(6 + headerLengthOfDataSection);
        buf.writeByte(0b00000001);
        buf.writeByte(0b00000000);
        buf.writeInt(Math.addExact(headerLengthOfDataSection, contentLengthOfDataSection));

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
        final var codeLength = StringUtil.requiredNullTerminatedUTF8BytesLength(code);
        final var error = ctx.alloc().buffer(14 + codeLength);
        error.writeByte(0b00000001);
        error.writeByte(0b00000000);
        error.writeInt(8 + codeLength);
        error.writeInt(ERROR.value());
        error.writeInt(sequenceId);
        error.writeCharSequence(code, UTF_8);
        error.writeZero(1);

        writeAndFlush(error);
    }

    public final ChannelHandlerContext getCtx() {
        return ctx;
    }

    protected final ByteBuf getBuf() {
        return buf;
    }

    protected final int getSequenceId() {
        return sequenceId;
    }
}
