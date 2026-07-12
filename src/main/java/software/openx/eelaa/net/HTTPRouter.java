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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.net.http.FetchAccount;
import software.openx.eelaa.net.http.Message;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * HTTP router implementation.
 *
 * @author Alireza Pourtaghi
 */
final class HTTPRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HTTPRouter.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Message<FetchAccount>> FETCH_ACCOUNT_TYPE = new TypeReference<>() {
    };

    private static final ByteBuf EMPTY = Unpooled.buffer();
    private static final ByteBuf METHOD_NOT_SUPPORTED = Unpooled.buffer();
    private static final ByteBuf HANDLER_NOT_FOUND = Unpooled.buffer();
    private static final ByteBuf CONTENT_TYPE_NOT_SUPPORTED = Unpooled.buffer();
    private static final ByteBuf RESOURCE_NOT_FOUND = Unpooled.buffer();
    private static final ByteBuf INTERNAL_SERVER_ERROR = Unpooled.buffer();

    private static final List<String> QUERY_PARAMETER_ZERO = new ArrayList<>(1);

    static {
        METHOD_NOT_SUPPORTED.writeCharSequence("{\"code\":\"method.not_supported\",\"message\":\"method not supported\"}", UTF_8);
        HANDLER_NOT_FOUND.writeCharSequence("{\"code\":\"handler.not_found\",\"message\":\"handler not found\"}", UTF_8);
        CONTENT_TYPE_NOT_SUPPORTED.writeCharSequence("{\"code\":\"content_type.not_supported\",\"message\":\"content not supported\"}", UTF_8);
        RESOURCE_NOT_FOUND.writeCharSequence("{\"code\":\"resource.not_found\",\"message\":\"requested resource not found\"}", UTF_8);
        INTERNAL_SERVER_ERROR.writeCharSequence("{\"code\":\"server.error\",\"message\":\"internal server error occurred\"}", UTF_8);

        QUERY_PARAMETER_ZERO.add("0");
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
        try {
            final var method = request.method();
            final var uri = new QueryStringDecoder(request.uri());
            final var contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE, "");

            if (method != HttpMethod.POST) {
                respondMethodNotSupported(ctx);
                return;
            }

            if (!uri.path().equals("/messages")) {
                respondHandlerNotFound(ctx);
                return;
            }

            if (!contentType.equals("application/json") &&
                    !contentType.equalsIgnoreCase("application/json; charset=UTF-8")) {

                respondContentTypeNotSupported(ctx);
                return;
            }

            switch (FrameNumericType.of(messageNumericType(uri))) {
                case FrameNumericType.PING -> respondEmpty(ctx);
                case FrameNumericType.FETCH_ACCOUNT -> handleFetchAccount(ctx, request);

                case null, default -> respondHandlerNotFound(ctx);
            }
        } catch (final Exception ex) {
            respondInternalServerError(ctx);
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    private void handleFetchAccount(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        // TODO: Handle null data.
        final var message = MAPPER.readValue((InputStream) new ByteBufInputStream(request.content()), FETCH_ACCOUNT_TYPE);
        final var account = ledger.fetchAccount(message.getData().getLedger(), message.getData().getAccount()).get();
        if (account == null) {
            respondResourceNotFound(ctx);
            return;
        }

        final var json = ctx.alloc().buffer(64);
        MAPPER.writeValue((OutputStream) new ByteBufOutputStream(json), account);

        respondJson(ctx, json);
    }

    private static void respondJson(final ChannelHandlerContext ctx, final ByteBuf json) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                json);

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, json.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondEmpty(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                EMPTY.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, EMPTY.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondMethodNotSupported(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST,
                METHOD_NOT_SUPPORTED.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, METHOD_NOT_SUPPORTED.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondHandlerNotFound(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                HANDLER_NOT_FOUND.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, HANDLER_NOT_FOUND.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondContentTypeNotSupported(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST,
                CONTENT_TYPE_NOT_SUPPORTED.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, CONTENT_TYPE_NOT_SUPPORTED.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondResourceNotFound(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                RESOURCE_NOT_FOUND.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, RESOURCE_NOT_FOUND.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static void respondInternalServerError(final ChannelHandlerContext ctx) {
        final var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR.retainedDuplicate());

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, INTERNAL_SERVER_ERROR.readableBytes());

        ctx.writeAndFlush(response);
    }

    private static int messageNumericType(final QueryStringDecoder uri) {
        try {
            return Integer.parseInt(uri.parameters().getOrDefault("numericType", QUERY_PARAMETER_ZERO).getFirst());
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }
}
