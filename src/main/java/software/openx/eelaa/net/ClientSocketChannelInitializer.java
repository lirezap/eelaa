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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.lz4.LZ4;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Channel initializer to be able to initialize incoming client socket channels.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class ClientSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final TCPServerConfig config;
    private final TLSContext tlsContext;
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final Ledger ledger;

    // Sharable handlers.
    private final HeartbeatHandler heartbeatHandler;
    private final FrameHeaderLogger frameHeaderLogger;
    private final FrameHeaderProcessor frameHeaderProcessor;
    private final InboundExceptionHandler inboundExceptionHandler;

    public ClientSocketChannelInitializer(final TCPServerConfig config, final TLSContext tlsContext,
                                          final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4,
                                          final Ledger ledger) {

        this.config = config;
        this.tlsContext = tlsContext;
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.ledger = ledger;
        this.heartbeatHandler = new HeartbeatHandler();
        this.frameHeaderLogger = new FrameHeaderLogger();
        this.frameHeaderProcessor = new FrameHeaderProcessor(new FrameDataDecompressor(cpuHeavyTaskExecutor, lz4));
        this.inboundExceptionHandler = new InboundExceptionHandler();
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        addTLSHandler(channel);              // Must be the first inbound handler.
        addIdleStateHandler(channel);
        addHeartbeatHandler(channel);

        if (config.getHttpServerConfig().isEnabled()) {
            addHttpServerCodec(channel);
            addHttpContentDecompressor(channel);
            addHttpServerExpectContinueHandler(channel);
            addHttpObjectAggregator(channel);
            addHttpRouter(channel);
            addHttpContentCompressor(channel);
        } else {
            addFrameDecoder(channel);
            addFrameHeaderLogger(channel);
            addFrameHeaderProcessor(channel);
            addDispatcher(channel);
        }

        addInboundExceptionHandler(channel); // Must be the last inbound handler.
    }

    private void addTLSHandler(final SocketChannel channel) {
        if (tlsContext != null) {
            final var handler = tlsContext.newHandler(channel.alloc());
            handler.setHandshakeTimeout(tlsContext.config().getHandshakeTimeoutSeconds(), SECONDS);
            channel.pipeline().addLast(handler);
        }
    }

    private void addIdleStateHandler(final SocketChannel channel) {
        channel.pipeline().addLast(new IdleStateHandler(0, 0, config.getAllIdleTimeoutSeconds()));
    }

    private void addHeartbeatHandler(final SocketChannel channel) {
        channel.pipeline().addLast(heartbeatHandler);
    }

    private void addFrameDecoder(final SocketChannel channel) {
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.getMaxFrameSize(), 2, 4, 0, 0));
    }

    private void addFrameHeaderLogger(final SocketChannel channel) {
        if (config.isLogFrameHeader()) {
            channel.pipeline().addLast(frameHeaderLogger);
        }
    }

    private void addFrameHeaderProcessor(final SocketChannel channel) {
        channel.pipeline().addLast(frameHeaderProcessor.getClass().getName(), frameHeaderProcessor);
    }

    private void addDispatcher(final SocketChannel channel) {
        channel.pipeline().addLast(new Dispatcher(cpuHeavyTaskExecutor, ledger));
    }

    private void addInboundExceptionHandler(final SocketChannel channel) {
        channel.pipeline().addLast(inboundExceptionHandler);
    }

    private void addHttpServerCodec(final SocketChannel channel) {
        channel.pipeline().addLast(new HttpServerCodec(
                config.getHttpServerConfig().getMaxInitialLineLength(),
                config.getHttpServerConfig().getMaxHeaderSize(),
                config.getHttpServerConfig().getMaxChunkSize()));
    }

    private void addHttpContentDecompressor(final SocketChannel channel) {
        channel.pipeline().addLast(new HttpContentDecompressor(0));
    }

    private void addHttpServerExpectContinueHandler(final SocketChannel channel) {
        channel.pipeline().addLast(new HttpServerExpectContinueHandler());
    }

    private void addHttpObjectAggregator(final SocketChannel channel) {
        channel.pipeline().addLast(new HttpObjectAggregator(config.getHttpServerConfig().getMaxContentLength()));
    }

    private void addHttpRouter(final SocketChannel channel) {
        channel.pipeline().addLast(new HTTPRouter(cpuHeavyTaskExecutor, ledger));
    }

    private void addHttpContentCompressor(final SocketChannel channel) {
        final var contentSizeThreshold =
                config.getHttpServerConfig().getHttpServerCompressionConfig().getContentSizeThreshold();

        final var compressionLevel =
                config.getHttpServerConfig().getHttpServerCompressionConfig().getCompressionLevel();

        final var windowBits =
                config.getHttpServerConfig().getHttpServerCompressionConfig().getWindowBits();

        final var memLevel =
                config.getHttpServerConfig().getHttpServerCompressionConfig().getMemLevel();

        channel.pipeline().addLast(new HttpContentCompressor(
                contentSizeThreshold,
                StandardCompressionOptions.gzip(compressionLevel, windowBits, memLevel)));
    }
}
