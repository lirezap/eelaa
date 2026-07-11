/*
 * Copyright 2026 Alireza Pourtaghi <lirezap@protonmail.com>
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
package software.openx.eelaa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.ledger.LedgerConfig;
import software.openx.eelaa.lz4.LZ4;
import software.openx.eelaa.lz4.LZ4Config;
import software.openx.eelaa.net.CPUHeavyTaskExecutorConfig;
import software.openx.eelaa.net.TCPServer;
import software.openx.eelaa.net.TCPServerConfig;
import software.openx.eelaa.net.TLSContextConfig;
import software.openx.eelaa.net.http.HTTPServerCompressionConfig;
import software.openx.eelaa.net.http.HTTPServerConfig;

/**
 * Main application class to be executed.
 *
 * @author Alireza Pourtaghi
 */
public final class EelaaApplication implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(EelaaApplication.class);

    private final LZ4 lz4;
    private final Ledger ledger;
    private final TCPServer tcpServer;

    private EelaaApplication(final LZ4 lz4, final Ledger ledger, final TCPServer tcpServer) {
        this.lz4 = lz4;
        this.ledger = ledger;
        this.tcpServer = tcpServer;
    }

    public static void main(final String... args) {
        logger.info("Starting eelaa version: {}", EelaaApplication.class.getPackage().getImplementationVersion());

        try {
            final var config = new Configuration();
            final var lz4 = lz4(config);
            final var ledger = ledger(config, lz4);
            final var tcpServer = tcpServer(config, lz4, ledger);
            addShutdownHook(new EelaaApplication(lz4, ledger, tcpServer));

            tcpServer.start();
        } catch (final Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
            System.exit(-1);
        }
    }

    private static LZ4 lz4(final Configuration config) {
        final var lz4Config = new LZ4Config.Builder(config.loadPath("libraries.native.lz4.path"))
                .build();

        return LZ4.newInstance(lz4Config);
    }

    private static Ledger ledger(final Configuration config, final LZ4 lz4) throws Exception {
        final var databaseSizeGBs = config.loadInt("ledgers.default.databaseSizeGBs");
        final var ledgerConfig = new LedgerConfig.Builder()
                .dataDirectoryPath(config.loadPath("ledgers.default.dataDirectoryPath"))
                .executorMaxWaitQueueSize(config.loadInt("ledgers.default.executorMaxWaitQueueSize"))
                .succeededTransactionsCacheTTLSeconds(config.loadInt("ledgers.default.succeededTransactionsCacheTTLSeconds"))
                .initialAccountsCap(config.loadInt("ledgers.default.initialAccountsCap"))
                .initialWalletsPerAccountCap(config.loadInt("ledgers.default.initialWalletsPerAccountCap"))
                .build();

        return Ledger.newInstance(ledgerConfig, lz4, databaseSizeGBs);
    }

    private static TCPServer tcpServer(final Configuration config, final LZ4 lz4, final Ledger ledger) {
        final var tlsContextConfig = new TLSContextConfig.Builder()
                .useTls(config.loadBoolean("servers.tcp.tlsContextConfig.useTls"))
                .serverCertPath(config.loadPath("servers.tcp.tlsContextConfig.serverCertPath"))
                .serverKeyPath(config.loadPath("servers.tcp.tlsContextConfig.serverKeyPath"))
                .useOcsp(config.loadBoolean("servers.tcp.tlsContextConfig.useOcsp"))
                .sessionCacheSize(config.loadInt("servers.tcp.tlsContextConfig.sessionCacheSize"))
                .sessionTimeoutSeconds(config.loadInt("servers.tcp.tlsContextConfig.sessionTimeoutSeconds"))
                .handshakeTimeoutSeconds(config.loadInt("servers.tcp.tlsContextConfig.handshakeTimeoutSeconds"))
                .build();

        final var cpuHeavyTaskExecutorConfig = new CPUHeavyTaskExecutorConfig.Builder()
                .nThreads(config.loadInt("servers.tcp.cpuHeavyTaskExecutorConfig.nThreads"))
                .shutdownQuietPeriodSeconds(config.loadInt("servers.tcp.cpuHeavyTaskExecutorConfig.shutdownQuietPeriodSeconds"))
                .shutdownWaitTimeSeconds(config.loadInt("servers.tcp.cpuHeavyTaskExecutorConfig.shutdownWaitTimeSeconds"))
                .build();

        final var httpServerCompressionConfig = new HTTPServerCompressionConfig.Builder()
                .contentSizeThreshold(config.loadInt("servers.tcp.httpServerConfig.httpServerCompressionConfig.contentSizeThreshold"))
                .compressionLevel(config.loadInt("servers.tcp.httpServerConfig.httpServerCompressionConfig.compressionLevel"))
                .windowBits(config.loadInt("servers.tcp.httpServerConfig.httpServerCompressionConfig.windowBits"))
                .memLevel(config.loadInt("servers.tcp.httpServerConfig.httpServerCompressionConfig.memLevel"))
                .build();

        final var httpServerConfig = new HTTPServerConfig.Builder()
                .enabled(config.loadBoolean("servers.tcp.httpServerConfig.enabled"))
                .maxInitialLineLength(config.loadInt("servers.tcp.httpServerConfig.maxInitialLineLength"))
                .maxHeaderSize(config.loadInt("servers.tcp.httpServerConfig.maxHeaderSize"))
                .maxChunkSize(config.loadInt("servers.tcp.httpServerConfig.maxChunkSize"))
                .maxContentLength(config.loadInt("servers.tcp.httpServerConfig.maxContentLength"))
                .httpServerCompressionConfig(httpServerCompressionConfig)
                .build();

        final var tcpServerConfig = new TCPServerConfig.Builder()
                .host(config.loadString("servers.tcp.host"))
                .port(config.loadInt("servers.tcp.port"))
                .eventLoopGroupNThreads(config.loadInt("servers.tcp.eventLoopGroupNThreads"))
                .soBacklog(config.loadInt("servers.tcp.soBacklog"))
                .maxFrameSize(config.loadInt("servers.tcp.maxFrameSize"))
                .lowWriteBufferWaterMark(config.loadInt("servers.tcp.lowWriteBufferWaterMark"))
                .highWriteBufferWaterMark(config.loadInt("servers.tcp.highWriteBufferWaterMark"))
                .tlsContextConfig(tlsContextConfig)
                .allIdleTimeoutSeconds(config.loadInt("servers.tcp.allIdleTimeoutSeconds"))
                .logFrameHeader(config.loadBoolean("servers.tcp.logFrameHeader"))
                .cpuHeavyTaskExecutorConfig(cpuHeavyTaskExecutorConfig)
                .shutdownQuietPeriodSeconds(config.loadInt("servers.tcp.shutdownQuietPeriodSeconds"))
                .shutdownWaitTimeSeconds(config.loadInt("servers.tcp.shutdownWaitTimeSeconds"))
                .detectResourceLeak(config.loadBoolean("servers.tcp.detectResourceLeak"))
                .httpServerConfig(httpServerConfig)
                .build();

        return TCPServer.newInstance(tcpServerConfig, lz4, ledger);
    }

    private static void addShutdownHook(final EelaaApplication application) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down eelaa ...");

            try {
                application.close();
            } catch (final Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        }));
    }

    @Override
    public void close() throws Exception {
        tcpServer.close();
        ledger.close();
        lz4.close();
    }
}
