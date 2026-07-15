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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.ledger.FailedTransaction;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.net.http.Message;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP router implementation.
 *
 * @author Alireza Pourtaghi
 */
final class HTTPRouter extends AbstractHTTPRouter {
    private static final Logger logger = LoggerFactory.getLogger(HTTPRouter.class);

    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final Ledger ledger;
    private final SequenceIdsHolder sequenceIdsHolder;

    public HTTPRouter(final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final Ledger ledger) {
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.ledger = ledger;
        this.sequenceIdsHolder = new SequenceIdsHolder();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) {
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

                respondInvalidContentType(ctx);
                return;
            }

            switch (FrameNumericType.of(intValuePathParameter(uri, "numericType"))) {
                case FrameNumericType.PING -> respondEmpty(ctx);
                case FrameNumericType.FETCH_ACCOUNT -> handleFetchAccount(ctx, request);
                case FrameNumericType.FETCH_WALLET -> handleFetchWallet(ctx, request);
                case FrameNumericType.BATCH -> handleBatch(ctx, request, false);
                case FrameNumericType.ATOMIC_BATCH -> handleBatch(ctx, request, true);
                case FrameNumericType.FETCH_TRANSACTION -> handleFetchTransaction(ctx, request);

                case null, default -> respondHandlerNotFound(ctx);
            }
        } catch (final Exception ex) {
            respondServerError(ctx);
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    private void handleFetchAccount(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        final var message = fetchAccountMessage(request);
        if (!isValidMessage(ctx, message)) {
            return;
        }

        final var account = ledger.fetchAccount(
                message.getData().getLedger(), message.getData().getAccount()).get();

        if (account == null) {
            respondResourceNotFound(ctx);
            return;
        }

        final var buf = ctx.alloc().buffer(64);
        writeJsonValue(buf, account);
        respondJson(ctx, buf);
    }

    private void handleFetchWallet(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        final var message = fetchWalletMessage(request);
        if (!isValidMessage(ctx, message)) {
            return;
        }

        final var wallet = ledger.fetchWallet(
                message.getData().getLedger(), message.getData().getAccount(), message.getData().getWallet()).get();

        if (wallet == null) {
            respondResourceNotFound(ctx);
            return;
        }

        final var buf = ctx.alloc().buffer(32);
        writeJsonValue(buf, wallet);
        respondJson(ctx, buf);
    }

    private void handleBatch(final ChannelHandlerContext ctx, final FullHttpRequest request, final boolean atomic) {
        final var message = batchMessage(request);
        if (!isValidMessage(ctx, message)) {
            return;
        }

        final var data = message.getData();
        final var batch = new software.openx.eelaa.ledger.Transaction[data.size()];
        for (int i = 0; i < data.size(); i++) {
            final var item = data.get(i);
            batch[i] = new software.openx.eelaa.ledger.Transaction(
                    item.getLedger(),
                    item.getSourceAccount(),
                    item.getSourceWallet(),
                    item.getDestinationAccount(),
                    item.getDestinationWallet(),
                    item.getId(),
                    item.getCurrency(),
                    item.getAmount(),
                    item.getMaxOverdraftAmount(),
                    item.getMetadata());
        }

        CompletableFuture<Boolean> result;
        if (atomic) {
            result = ledger.processAtomically(batch);
        } else {
            result = ledger.process(batch);
        }

        result.thenAcceptAsync(successful -> {
            if (successful) {
                var failedCount = 0;
                for (final var transaction : batch) {
                    if (transaction.is_failed()) failedCount++;
                }

                final var failedList = new ArrayList<FailedTransaction>(failedCount);
                for (final var transaction : batch) {
                    if (transaction.is_failed()) {
                        failedList.add(new FailedTransaction(transaction.getId(), transaction.get_failReason()));
                    }
                }

                final var buf = ctx.alloc().buffer(failedCount * 64);
                writeJsonValue(buf, failedList);
                respondJson(ctx, buf);
            } else {
                respondServerError(ctx);
            }
        }, cpuHeavyTaskExecutor);
    }

    private void handleFetchTransaction(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        final var message = fetchTransactionMessage(request);
        if (!isValidMessage(ctx, message)) {
            return;
        }

        ledger.fetchTransaction(message.getData().getLedger(), message.getData().getId())
                .thenAcceptAsync(transaction -> {
                    if (transaction == null) {
                        respondResourceNotFound(ctx);
                        return;
                    }

                    final var buf = ctx.alloc().buffer(128);
                    writeJsonValue(buf, transaction);
                    respondJson(ctx, buf);
                }, cpuHeavyTaskExecutor);
    }

    private <T> boolean isValidMessage(final ChannelHandlerContext ctx, final Message<T> message) {
        if (!addSequenceId(message.getSequenceId())) {
            respondInvalidSequenceId(ctx);
            return false;
        }

        if (!isValidTimestamp(message.getTs())) {
            respondInvalidTs(ctx);
            return false;
        }

        if (message.getData() == null) {
            respondDataIsRequired(ctx);
            return false;
        }

        return true;
    }

    private boolean addSequenceId(final int sequenceId) {
        return sequenceIdsHolder.addSequenceId(sequenceId);
    }

    private boolean isValidTimestamp(final long ts) {
        final var now = System.currentTimeMillis();
        return (now - 5000) < ts && ts < (now + 5000);
    }
}
