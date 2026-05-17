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

    private TCPServer(final TCPServerConfig config) {
        this.config = config;
        this.nativeServerBootstrap = NativeServerBootstrap.newInstance(config);
        this.cpuHeavyTaskExecutor = CPUHeavyTaskExecutor.newInstance(config.getCpuHeavyTaskExecutorConfig());
        this.lz4 = LZ4.newInstance(config.getLz4Config());
    }

    public static TCPServer newInstance(final TCPServerConfig config) {
        return new TCPServer(config);
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

        cpuHeavyTaskExecutor.shutdownGracefully().sync();
        lz4.close();

        logger.info("TCP server closed gracefully!");
    }
}
