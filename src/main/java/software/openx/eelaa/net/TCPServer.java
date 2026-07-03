/*
 * Copyright 2026 lirezap@protonmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.openx.eelaa.net;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.lz4.LZ4;

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
    private final Ledger ledger;

    private TCPServer(final TCPServerConfig config, final NativeServerBootstrap nativeServerBootstrap,
                      final CPUHeavyTaskExecutor cpuHeavyTaskExecutor, final LZ4 lz4, final Ledger ledger) {

        this.config = config;
        this.nativeServerBootstrap = nativeServerBootstrap;
        this.cpuHeavyTaskExecutor = cpuHeavyTaskExecutor;
        this.lz4 = lz4;
        this.ledger = ledger;
    }

    public static TCPServer newInstance(final TCPServerConfig config, final LZ4 lz4, final Ledger ledger) {
        final var nativeServerBootstrap = NativeServerBootstrap.newInstance(config);
        final var cpuHeavyTaskExecutor = CPUHeavyTaskExecutor.newInstance(config.getCpuHeavyTaskExecutorConfig());

        return new TCPServer(config, nativeServerBootstrap, cpuHeavyTaskExecutor, lz4, ledger);
    }

    public void start() throws Exception {
        if (config.isDetectResourceLeak()) {
            ResourceLeakDetector.setLevel(Level.ADVANCED);
        } else {
            ResourceLeakDetector.setLevel(Level.DISABLED);
        }

        nativeServerBootstrap.configure();
        nativeServerBootstrap.childHandler(new ClientSocketChannelInitializer(config, tlsContext(), cpuHeavyTaskExecutor, lz4, ledger));
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
                config.getShutdownQuietPeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS).sync();

        nativeServerBootstrap.config().childGroup().shutdownGracefully(
                config.getShutdownQuietPeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS).sync();

        cpuHeavyTaskExecutor.shutdownGracefully().sync();
        logger.info("TCP server closed gracefully!");
    }
}
