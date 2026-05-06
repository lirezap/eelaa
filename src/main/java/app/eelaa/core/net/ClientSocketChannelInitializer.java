package app.eelaa.core.net;

import app.eelaa.core.net.handler.FrameHeaderLoggerHandler;
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
        addFrameDecoderHandler(channel);
        addFrameHeaderLoggerHandler(channel);
    }

    private void addFrameDecoderHandler(final SocketChannel channel) {
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.getMaxFrameSize(), 2, 4, 0, 0));
    }

    private void addFrameHeaderLoggerHandler(final SocketChannel channel) {
        if (config.isLogFrameHeader()) {
            channel.pipeline().addLast(new FrameHeaderLoggerHandler());
        }
    }
}
