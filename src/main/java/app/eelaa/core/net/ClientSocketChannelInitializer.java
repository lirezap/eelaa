package app.eelaa.core.net;

import app.eelaa.core.net.handler.FrameHeaderLogger;
import app.eelaa.core.net.handler.HeaderProcessor;
import app.eelaa.core.net.handler.InboundExceptionHandler;
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

    public ClientSocketChannelInitializer(final TCPServerConfig config) {
        this.config = config;
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
            channel.pipeline().addLast(new FrameHeaderLogger());
        }
    }

    private void addHeaderProcessor(final SocketChannel channel) {
        channel.pipeline().addLast(new HeaderProcessor());
    }

    private void addInboundExceptionHandler(final SocketChannel channel) {
        channel.pipeline().addLast(new InboundExceptionHandler());
    }
}
