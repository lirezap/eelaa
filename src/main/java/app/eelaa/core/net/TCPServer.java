package app.eelaa.core.net;

import app.eelaa.core.lz4.LZ4;
import app.eelaa.core.os.OSDetector;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * TCP server implementation based on netty.
 *
 * @author Alireza Pourtaghi
 */
public final class TCPServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    private final TCPServerConfig config;
    private final NativeServerBootstrap nativeServerBootstrap;
    private final CPUHeavyTaskExecutor cpuHeavyTaskExecutor;
    private final LZ4 lz4;

    private TCPServer(final TCPServerConfig config) {
        this.config = config;
        this.nativeServerBootstrap = NativeServerBootstrap.newInstance();
        this.cpuHeavyTaskExecutor = CPUHeavyTaskExecutor.newInstance(config.getCpuHeavyTaskExecutorConfig());
        this.lz4 = LZ4.newInstance(config.getLz4Config());
    }

    public static TCPServer newInstance(final TCPServerConfig config) {
        return new TCPServer(config);
    }

    public void start() throws Exception {
        // TODO: Check server channel options.
        nativeServerBootstrap.group(group());
        nativeServerBootstrap.channel(channel());
        nativeServerBootstrap.childHandler(new ClientSocketChannelInitializer(config, tlsContext(), cpuHeavyTaskExecutor, lz4));
        nativeServerBootstrap.bind(config.getHost(), config.getPort()).sync();
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

    private TLSContext tlsContext() throws Exception {
        if (config.getTlsContextConfig().isUseTls()) {
            return TLSContext.newInstance(config.getTlsContextConfig());
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        nativeServerBootstrap.config().group().shutdownGracefully(
                config.getShutdownQuitePeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS).sync();

        cpuHeavyTaskExecutor.shutdownGracefully().sync();
        lz4.close();

        logger.info("TCP server closed gracefully!");
    }
}
