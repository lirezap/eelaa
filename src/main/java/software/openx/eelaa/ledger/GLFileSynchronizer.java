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
package software.openx.eelaa.ledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.lmdb.LMDBManager;
import software.openx.eelaa.lz4.LZ4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static software.openx.eelaa.lmdb.LMDBFlags.*;

/**
 * Synchronizer implementation class that transfers transactions from GL file to LMDB storage engine.
 *
 * @author Alireza Pourtaghi
 */
final class GLFileSynchronizer implements Runnable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GLFileSynchronizer.class);

    private final ExecutorService executor;
    private final LZ4 lz4;
    private final LMDBManager lmdbManager;
    private final int transactionsDbi;
    private final int walletsDbi;
    private final int ledgersDbi;

    private GLFileSynchronizer(final ExecutorService executor, final LZ4 lz4, final LMDBManager lmdbManager,
                               final int transactionsDbi, final int walletsDbi, final int ledgersDbi) {

        this.executor = executor;
        this.lz4 = lz4;
        this.lmdbManager = lmdbManager;
        this.transactionsDbi = transactionsDbi;
        this.walletsDbi = walletsDbi;
        this.ledgersDbi = ledgersDbi;
    }

    public static GLFileSynchronizer newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4,
                                                 final int databaseSizeGbs) throws Exception {

        final var executor = Executors.newSingleThreadExecutor();
        final var lmdbManager = executor.submit(() -> LMDBManager.newInstance(
                ledgerConfig.getDataDirectoryPath(),
                Math.max(1, databaseSizeGbs) * 1073741824L,
                4,
                MDB_NORDAHEAD,
                0644)).get();

        final var transactionsDbi = executor.submit(() -> lmdbManager
                .openDb("synced_transactions")).get();

        final var walletsDbi = executor.submit(() -> lmdbManager
                .openDb("synced_wallets")).get();

        final var ledgersDbi = executor.submit(() -> lmdbManager
                .openDb("synced_ledgers", MDB_CREATE | MDB_INTEGERKEY)).get();

        return new GLFileSynchronizer(executor, lz4, lmdbManager, transactionsDbi, walletsDbi, ledgersDbi);
    }

    public void start() {
        executor.submit(this);
    }

    @Override
    public void run() {
        try {
            // TODO: Complete implementation.
        } catch (final Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        } finally {
            try {
                executor.execute(this);
            } catch (final RejectedExecutionException _) {
                logger.warn("Rejected task because of closing executor!");
            }
        }
    }

    @Override
    public void close() throws Exception {
        executor.submit(() -> {
            try {
                lmdbManager.close();
            } catch (final Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        }).get();

        executor.shutdown();
        if (!executor.awaitTermination(60, SECONDS)) {
            // Safe to ignore runnable list!
            executor.shutdownNow();
        }
    }
}
