package app.eelaa.core.net;

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

    // Sharable handlers.
    private final FrameHeaderLogger frameHeaderLogger;
    private final HeaderProcessor headerProcessor;
    private final InboundExceptionHandler inboundExceptionHandler;

    public ClientSocketChannelInitializer(final TCPServerConfig config) {
        this.config = config;
        this.frameHeaderLogger = new FrameHeaderLogger();
        this.headerProcessor = new HeaderProcessor();
        this.inboundExceptionHandler = new InboundExceptionHandler();
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        addFrameDecoder(channel);
        addFrameHeaderLogger(channel);
        addHeaderProcessor(channel);

        // Must be the latest inbound handler.
        addInboundExceptionHandler(channel);
    }

    private void addFrameDecoder(final SocketChannel channel) {
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.getMaxFrameSize(), 2, 4, 0, 0));
    }

    private void addFrameHeaderLogger(final SocketChannel channel) {
        if (config.isLogFrameHeader()) {
            channel.pipeline().addLast(frameHeaderLogger);
        }
    }

    private void addHeaderProcessor(final SocketChannel channel) {
        channel.pipeline().addLast(headerProcessor);
    }

    private void addInboundExceptionHandler(final SocketChannel channel) {
        channel.pipeline().addLast(inboundExceptionHandler);
    }
}
