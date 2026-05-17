package app.eelaa.core.net;

import app.eelaa.core.os.OSDetector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueChannelOption;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Native configured server bootstrap.
 *
 * @author Alireza Pourtaghi
 */
final class NativeServerBootstrap extends ServerBootstrap {
    private final TCPServerConfig config;

    private NativeServerBootstrap(final TCPServerConfig config) {
        this.config = config;
    }

    public static NativeServerBootstrap newInstance(final TCPServerConfig config) {
        return new NativeServerBootstrap(config);
    }

    public void configure() {
        // TODO: Use all appropriate server/client side channel options.
        this.group(selectGroup());
        this.channel(selectChannel());

        if (Epoll.isAvailable()) {
            this.option(EpollChannelOption.SO_BACKLOG, config.getSoBacklog());
            this.option(EpollChannelOption.SO_REUSEADDR, true);
            this.option(EpollChannelOption.SO_REUSEPORT, true);
        } else if (KQueue.isAvailable()) {
            this.option(KQueueChannelOption.SO_BACKLOG, config.getSoBacklog());
            this.option(KQueueChannelOption.SO_REUSEADDR, true);
            this.option(KQueueChannelOption.SO_REUSEPORT, true);
        } else {
            this.option(ChannelOption.SO_BACKLOG, config.getSoBacklog());
            this.option(ChannelOption.SO_REUSEADDR, true);
        }

        this.childOption(ChannelOption.TCP_NODELAY, true);
        this.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        this.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark());
    }

    private EventLoopGroup selectGroup() {
        final var os = OSDetector.os();

        return switch (os) {
            case LINUX -> new MultiThreadIoEventLoopGroup(EpollIoHandler.newFactory());
            case MACOS -> new MultiThreadIoEventLoopGroup(KQueueIoHandler.newFactory());
            case OTHER -> new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        };
    }

    private Class<? extends ServerChannel> selectChannel() {
        return switch (OSDetector.os()) {
            case LINUX -> EpollServerSocketChannel.class;
            case MACOS -> KQueueServerSocketChannel.class;
            case OTHER -> NioServerSocketChannel.class;
        };
    }

    private WriteBufferWaterMark writeBufferWaterMark() {
        return new WriteBufferWaterMark(config.getLowWriteBufferWaterMark(), config.getHighWriteBufferWaterMark());
    }
}
