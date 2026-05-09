package app.eelaa.core.net;

import app.eelaa.core.util.OSDetector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP server implementation based on netty.
 *
 * @author Alireza Pourtaghi
 */
public final class TCPServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    private final TCPServerConfig config;
    private final ServerBootstrap bootstrap;
    private final EventExecutorGroup cpuHeavyExecutor;

    private TCPServer(final TCPServerConfig config) {
        this.config = config;
        this.bootstrap = new ServerBootstrap();
        this.cpuHeavyExecutor = new DefaultEventExecutorGroup(config.getCpuHeavyExecutorThreads());
    }

    public static TCPServer newInstance(final TCPServerConfig config) {
        return new TCPServer(config);
    }

    public void start() throws Exception {
        // TODO: Check server channel options.
        bootstrap.group(group());
        bootstrap.channel(channel());
        bootstrap.childHandler(new ClientSocketChannelInitializer(config, cpuHeavyExecutor));
        bootstrap.bind(config.getHost(), config.getPort()).sync();
        logger.info("Started TCP server using configuration: {}", config);
    }

    private EventLoopGroup group() {
        final var os = OSDetector.os();
        logger.info("Detected {} as operating system", os);

        return switch (os) {
            case LINUX -> new MultiThreadIoEventLoopGroup(EpollIoHandler.newFactory());
            case MACOS -> new MultiThreadIoEventLoopGroup(KQueueIoHandler.newFactory());
            case OTHER -> new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        };
    }

    private Class<? extends ServerChannel> channel() {
        return switch (OSDetector.os()) {
            case LINUX -> EpollServerSocketChannel.class;
            case MACOS -> KQueueServerSocketChannel.class;
            case OTHER -> NioServerSocketChannel.class;
        };
    }

    @Override
    public void close() throws Exception {
        // TODO: Set timeout for shutdown tasks.
        cpuHeavyExecutor.shutdownGracefully().sync();
        bootstrap.config().group().shutdownGracefully().sync();
        logger.info("TCP server closed gracefully!");
    }
}
