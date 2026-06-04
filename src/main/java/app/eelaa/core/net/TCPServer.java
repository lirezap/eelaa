package app.eelaa.core.net;

import app.eelaa.core.lz4.LZ4;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
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

    private TCPServer(final TCPServerConfig config, final NativeServerBootstrap nativeServerBootstrap,
                      final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4) {

        this.config = config;
        this.nativeServerBootstrap = nativeServerBootstrap;
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.lz4 = lz4;
    }

    public static TCPServer newInstance(final TCPServerConfig config) {
        final var nativeServerBootstrap = NativeServerBootstrap.newInstance(config);
        final var cpuHeavyTaskExecutor = CPUHeavyTaskExecutor.newInstance(config.getCpuHeavyTaskExecutorConfig());
        final var lz4 = LZ4.newInstance(config.getLz4Config());

        return new TCPServer(config, nativeServerBootstrap, cpuHeavyTaskExecutor, lz4);
    }

    public void start() throws Exception {
        if (config.isDetectResourceLeak()) {
            ResourceLeakDetector.setLevel(Level.ADVANCED);
        } else {
            ResourceLeakDetector.setLevel(Level.DISABLED);
        }

        nativeServerBootstrap.configure();
        nativeServerBootstrap.childHandler(new ClientSocketChannelInitializer(config, tlsContext(), cpuHeavyTaskExecutor, lz4));
        nativeServerBootstrap.bind(config.getHost(), config.getPort()).sync();
        logger.info("Started TCP server using configuration: {}", config);
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

        nativeServerBootstrap.config().childGroup().shutdownGracefully(
                config.getShutdownQuitePeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS).sync();

        cpuHeavyTaskExecutor.shutdownGracefully().sync();
        lz4.close();

        logger.info("TCP server closed gracefully!");
    }
}
