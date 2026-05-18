package app.eelaa.core.net;

import app.eelaa.core.lz4.LZ4;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Channel initializer to be able to initialize incoming client socket channels.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class ClientSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final TCPServerConfig config;
    private final TLSContext tlsContext;

    // Sharable handlers.
    private final FrameHeaderLogger frameHeaderLogger;
    private final FrameHeaderProcessor frameHeaderProcessor;
    private final Dispatcher dispatcher;
    private final InboundExceptionHandler inboundExceptionHandler;

    public ClientSocketChannelInitializer(final TCPServerConfig config, final TLSContext tlsContext,
                                          final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4) {

        this.config = config;
        this.tlsContext = tlsContext;
        this.frameHeaderLogger = new FrameHeaderLogger();
        this.frameHeaderProcessor = new FrameHeaderProcessor(new FrameDataDecompressor(cpuHeavyTaskExecutor, lz4));
        this.dispatcher = new Dispatcher(cpuHeavyTaskExecutor);
        this.inboundExceptionHandler = new InboundExceptionHandler();
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        // Must be the first inbound handler.
        addTLSHandler(channel);

        addFrameDecoder(channel);
        addFrameHeaderLogger(channel);
        addFrameHeaderProcessor(channel);
        addDispatcher(channel);

        // Must be the last inbound handler.
        addInboundExceptionHandler(channel);
    }

    private void addTLSHandler(final SocketChannel channel) {
        if (tlsContext != null) {
            channel.pipeline().addLast(tlsContext.newHandler(channel.alloc()));
        }
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
        channel.pipeline().addLast(dispatcher);
    }

    private void addInboundExceptionHandler(final SocketChannel channel) {
        channel.pipeline().addLast(inboundExceptionHandler);
    }
}
