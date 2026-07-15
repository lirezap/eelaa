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
import software.openx.eelaa.net.http.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Base abstract HTTP router implementation to contain some helpers and utilities.
 *
 * @author Alireza pourtaghi
 */
abstract class AbstractHTTPRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final TypeReference<Message<FetchAccount>> FETCH_ACCOUNT_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Message<FetchWallet>> FETCH_WALLET_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Message<ArrayList<Transaction>>> BATCH_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Message<FetchTransaction>> FETCH_TRANSACTION_TYPE = new TypeReference<>() {
    };

    private static final ByteBuf EMPTY = Unpooled.buffer();
    private static final ByteBuf METHOD_NOT_SUPPORTED = Unpooled.buffer();
    private static final ByteBuf HANDLER_NOT_FOUND = Unpooled.buffer();
    private static final ByteBuf INVALID_CONTENT_TYPE = Unpooled.buffer();
    private static final ByteBuf INVALID_SEQUENCE_ID = Unpooled.buffer();
    private static final ByteBuf INVALID_TS = Unpooled.buffer();
    private static final ByteBuf DATA_IS_REQUIRED = Unpooled.buffer();
    private static final ByteBuf RESOURCE_NOT_FOUND = Unpooled.buffer();
    private static final ByteBuf SERVER_ERROR = Unpooled.buffer();

    private static final List<String> QUERY_PARAMETER_ZERO = new ArrayList<>(1);

    static {
        METHOD_NOT_SUPPORTED.writeCharSequence("{\"code\":\"method.not_supported\",\"message\":\"http method not supported\"}", UTF_8);
        HANDLER_NOT_FOUND.writeCharSequence("{\"code\":\"handler.not_found\",\"message\":\"http handler not found\"}", UTF_8);
        INVALID_CONTENT_TYPE.writeCharSequence("{\"code\":\"content_type.invalid\",\"message\":\"empty or invalid content type header provided\"}", UTF_8);
        INVALID_SEQUENCE_ID.writeCharSequence("{\"code\":\"sequenceId.invalid\",\"message\":\"invalid sequence id provided\"}", UTF_8);
        INVALID_TS.writeCharSequence("{\"code\":\"ts.invalid\",\"message\":\"invalid timestamp provided\"}", UTF_8);
        DATA_IS_REQUIRED.writeCharSequence("{\"code\":\"data.is_required\",\"message\":\"data object in the json body is required\"}", UTF_8);
        RESOURCE_NOT_FOUND.writeCharSequence("{\"code\":\"resource.not_found\",\"message\":\"requested resource not found\"}", UTF_8);
        SERVER_ERROR.writeCharSequence("{\"code\":\"server.error\",\"message\":\"internal server error occurred\"}", UTF_8);

        QUERY_PARAMETER_ZERO.add("0");
    }

    public final Message<FetchAccount> fetchAccountMessage(final FullHttpRequest request) {
        return MAPPER.readValue((InputStream) new ByteBufInputStream(request.content()), FETCH_ACCOUNT_TYPE);
    }

    public final Message<FetchWallet> fetchWalletMessage(final FullHttpRequest request) {
        return MAPPER.readValue((InputStream) new ByteBufInputStream(request.content()), FETCH_WALLET_TYPE);
    }

    public final Message<ArrayList<Transaction>> batchMessage(final FullHttpRequest request) {
        return MAPPER.readValue((InputStream) new ByteBufInputStream(request.content()), BATCH_TYPE);
    }

    public final Message<FetchTransaction> fetchTransactionMessage(final FullHttpRequest request) {
        return MAPPER.readValue((InputStream) new ByteBufInputStream(request.content()), FETCH_TRANSACTION_TYPE);
    }

    public final void writeJsonValue(final ByteBuf buf, final Object value) {
        MAPPER.writeValue((OutputStream) new ByteBufOutputStream(buf), value);
    }

    public final void respondJson(final ChannelHandlerContext ctx, final ByteBuf json) {
        respond(ctx, HttpResponseStatus.OK, json);
    }

    public final void respondEmpty(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.OK, EMPTY.retainedDuplicate());
    }

    public final void respondMethodNotSupported(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.BAD_REQUEST, METHOD_NOT_SUPPORTED.retainedDuplicate());
    }

    public final void respondHandlerNotFound(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.NOT_FOUND, HANDLER_NOT_FOUND.retainedDuplicate());
    }

    public final void respondInvalidContentType(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.BAD_REQUEST, INVALID_CONTENT_TYPE.retainedDuplicate());
    }

    public final void respondInvalidSequenceId(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.BAD_REQUEST, INVALID_SEQUENCE_ID.retainedDuplicate());
    }

    public final void respondInvalidTs(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.BAD_REQUEST, INVALID_TS.retainedDuplicate());
    }

    public final void respondDataIsRequired(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.BAD_REQUEST, DATA_IS_REQUIRED.retainedDuplicate());
    }

    public final void respondResourceNotFound(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.NOT_FOUND, RESOURCE_NOT_FOUND.retainedDuplicate());
    }

    public final void respondServerError(final ChannelHandlerContext ctx) {
        respond(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR.retainedDuplicate());
    }

    public final int intValuePathParameter(final QueryStringDecoder uri, final String key) {
        try {
            return Integer.parseInt(uri.parameters().getOrDefault(key, QUERY_PARAMETER_ZERO).getFirst());
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }

    private void respond(final ChannelHandlerContext ctx, final HttpResponseStatus status, final ByteBuf json) {
        final var response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, json);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, json.readableBytes());
        ctx.writeAndFlush(response);
    }
}
