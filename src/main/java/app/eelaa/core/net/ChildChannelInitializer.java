package app.eelaa.core.net;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Channel initializer to be able to initialize incoming client socket channels.
 *
 * @author Alireza Pourtaghi
 */
@Sharable
final class ChildChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        // TODO: Add handlers.
        ch.pipeline().addLast();
    }
}
