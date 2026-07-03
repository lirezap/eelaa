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
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.lz4.LZ4;

import java.lang.foreign.MemorySegment;

/**
 * Task to decompress a provided byte buf using LZ4.
 *
 * @author Alireza Pourtaghi
 */
final class LZ4DecompressorTask implements Runnable {
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
            final var newBuf = ctx.alloc().directBuffer(actualSize).writerIndex(actualSize);

            final var src = MemorySegment.ofBuffer(buf.internalNioBuffer(readerIndex, readableBytes));
            final var dst = MemorySegment.ofBuffer(newBuf.internalNioBuffer(0, actualSize));

            if (lz4.decompressSafe(src, dst, readableBytes, actualSize) < 0) {
                throw DecompressionFailedException.INSTANCE;
            }

            ctx.fireChannelRead(newBuf);
        } catch (final Throwable cause) {
            ctx.fireExceptionCaught(cause);
        }
    }
}
