/*
 * Copyright 2026 lirezap@protonmail.com
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
package software.openx.eelaa.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import software.openx.eelaa.net.Handler;

import static software.openx.eelaa.net.FrameNumericType.PING;
import static software.openx.eelaa.net.FrameNumericType.PONG;

/**
 * @author Alireza Pourtaghi
 */
public final class PingHandler extends Handler {

    public PingHandler(final ChannelHandlerContext ctx, final ByteBuf buf, final int frameNumericType,
                       final int sequenceId) {

        super(ctx, buf, frameNumericType, sequenceId);
    }

    @Override
    protected int frameNumericType() {
        return PING.value();
    }

    @Override
    protected boolean isValid() {
        // Must not contain any extra data.
        return getBuf().readableBytes() == 0;
    }

    @Override
    protected void handle() throws Exception {
        if (isValid()) {
            releaseFrameBuffer();

            final var response = newV1Buf(8);
            response.writeInt(PONG.value());
            response.writeInt(getSequenceId());
            writeAndFlush(response);
        } else {
            releaseFrameBufferThenClose();
        }
    }
}
