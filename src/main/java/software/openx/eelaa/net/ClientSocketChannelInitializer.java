package software.openx.eelaa.net;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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
        addFrameDecoder(channel);
        addFrameHeaderLogger(channel);
        addFrameHeaderProcessor(channel);
        addDispatcher(channel);
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
}
