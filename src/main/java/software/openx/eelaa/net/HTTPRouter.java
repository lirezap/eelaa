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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import software.openx.eelaa.ledger.Ledger;
import tools.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * HTTP router implementation.
 *
 * @author Alireza Pourtaghi
 */
final class HTTPRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ByteBuf METHOD_NOT_SUPPORTED = Unpooled.buffer();
    private static final ByteBuf HANDLER_NOT_FOUND = Unpooled.buffer();

    static {
        METHOD_NOT_SUPPORTED.writeCharSequence("Method not supported!", UTF_8);
        HANDLER_NOT_FOUND.writeCharSequence("Handler not found!", UTF_8);
    }

    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final Ledger ledger;
    private final SequenceIdsHolder sequenceIdsHolder;

    public HTTPRouter(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final Ledger ledger) {
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.ledger = ledger;
        this.sequenceIdsHolder = new SequenceIdsHolder();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        if (request.method() == HttpMethod.POST) {
            if (request.uri().equals("/messages")) {
                // TODO: Complete implementation.
                final var response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.EMPTY_BUFFER);

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);

                ctx.writeAndFlush(response);
            } else {
                respondHandlerNotFound(ctx);
            }
        } else {
            respondMethodNotSupported(ctx);
        }
    }

    private static void respondMethodNotSupported(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST,
                METHOD_NOT_SUPPORTED.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, METHOD_NOT_SUPPORTED.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondHandlerNotFound(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                HANDLER_NOT_FOUND.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, HANDLER_NOT_FOUND.readableBytes());

        ctx.writeAndFlush(response);
    }
}
