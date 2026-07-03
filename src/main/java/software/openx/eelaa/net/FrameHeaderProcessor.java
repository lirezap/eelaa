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
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * A handler that processes incoming frame's header both for validation and flags extraction.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class FrameHeaderProcessor extends SimpleChannelInboundHandler<ByteBuf> {
    private final FrameDataDecompressor frameDataDecompressor;

    public FrameHeaderProcessor(final FrameDataDecompressor frameDataDecompressor) {
        super(false);
        this.frameDataDecompressor = frameDataDecompressor;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            processFrameVersion(buf);
            processFrameFlags(ctx, buf);
            ctx.fireChannelRead(buf);
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        }
    }

    private void processFrameVersion(final ByteBuf buf) {
        final var version = buf.readByte();
        if (version != 0b00000001) {
            throw FrameVersionNotSupportedException.INSTANCE;
        }
    }

    private void processFrameFlags(final ChannelHandlerContext ctx, final ByteBuf buf) {
        final var flags = buf.readByte();
        if ((flags & 0b11111110) != 0) {
            throw FrameFlagsNotSupportedException.INSTANCE;
        }

        if ((flags & 0b00000001) == 0b00000001) {
            ctx.pipeline().addAfter(
                    this.getClass().getName(), frameDataDecompressor.getClass().getName(), frameDataDecompressor);
        } else {
            processFrameLength(buf);
        }
    }

    private void processFrameLength(final ByteBuf buf) {
        final var length = buf.readInt();

        // At least frame numeric type, sequence id and timestamp are required.
        if (length < 16) {
            throw InvalidFrameLengthException.INSTANCE;
        }
    }
}
