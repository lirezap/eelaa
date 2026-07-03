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
import software.openx.eelaa.lz4.LZ4;

/**
 * A handler that decompresses incoming frame's data section.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class FrameDataDecompressor extends SimpleChannelInboundHandler<ByteBuf> {
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final LZ4 lz4;

    public FrameDataDecompressor(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4) {
        super(false);
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.lz4 = lz4;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        try {
            validateFrameLength(buf);
            submitDecompressorTask(ctx, buf, extractActualSize(buf));
        } catch (final Exception ex) {
            ReferenceCountUtil.release(buf);
            throw ex;
        } finally {
            // Exception propagates into pipeline and reaches inbound exception handler.
            ctx.pipeline().remove(this);
        }
    }

    private void validateFrameLength(final ByteBuf buf) {
        final var length = buf.readInt();

        // At least actual size as int value is required.
        if (length < 4) {
            throw InvalidFrameLengthException.INSTANCE;
        }
    }

    private int extractActualSize(final ByteBuf buf) {
        final var actualSize = buf.readInt();

        // This is the actual size (real size).
        // At least frame numeric type, sequence id and timestamp are required.
        if (actualSize < 16) {
            throw InvalidActualSizeException.INSTANCE;
        }

        return actualSize;
    }

    private void submitDecompressorTask(final ChannelHandlerContext ctx, final ByteBuf buf, final int actualSize) {
        cpuHeavyTaskExecutor
                .submit(new LZ4DecompressorTask(ctx, buf, lz4, actualSize))
                .addListener(new ReleaseByteBufFutureListener(buf));
    }
}
