package software.openx.eelaa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.ledger.Ledger;
import software.openx.eelaa.ledger.LedgerConfig;
import software.openx.eelaa.lz4.LZ4;
import software.openx.eelaa.lz4.LZ4Config;
import software.openx.eelaa.net.TCPServer;
import software.openx.eelaa.net.TCPServerConfig;

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

    public static void main(final String... args) throws Exception {
        logger.info("Starting eelaa version: {}", EelaaApplication.class.getPackage().getImplementationVersion());

        final var config = new Configuration();
        final var lz4 = lz4(config);
        final var ledger = ledger(config, lz4);
        final var tcpServer = tcpServer(config, lz4, ledger);

        tcpServer.start();
        addShutdownHook(new EelaaApplication(lz4, ledger, tcpServer));
    }

    private static LZ4 lz4(final Configuration config) {
        final var lz4Config = new LZ4Config.Builder(config.loadPath("libraries.native.lz4.path"))
                .build();

        return LZ4.newInstance(lz4Config);
    }

    private static Ledger ledger(final Configuration config, final LZ4 lz4) throws Exception {
        final var lmdbLibraryPath = config.loadPath("libraries.native.lmdb.path");
        final var databaseSizeGBs = config.loadInt("ledgers.default.databaseSizeGBs");
        final var ledgerConfig = new LedgerConfig.Builder()
                .dataDirectoryPath(config.loadPath("ledgers.default.dataDirectoryPath"))
                .executorMaxWaitQueueSize(config.loadInt("ledgers.default.executorMaxWaitQueueSize"))
                .succeededTransactionsCacheTTLSeconds(config.loadInt("ledgers.default.succeededTransactionsCacheTTLSeconds"))
                .initialAccountsCap(config.loadInt("ledgers.default.initialAccountsCap"))
                .initialWalletsPerAccountCap(config.loadInt("ledgers.default.initialWalletsPerAccountCap"))
                .build();

        return Ledger.newInstance(ledgerConfig, lz4, lmdbLibraryPath, databaseSizeGBs);
    }

    private static TCPServer tcpServer(final Configuration config, final LZ4 lz4, final Ledger ledger) {
        // TODO: Complete implementation.
        return TCPServer.newInstance(new TCPServerConfig.Builder().build(), lz4, ledger);
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
