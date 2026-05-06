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
    private final int maxFrameSize;

    public ClientSocketChannelInitializer(final int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        // Frame decoder handler.
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrameSize, 2, 4, 0, 0));
    }
}
