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
import software.openx.eelaa.storage.ThreadConfinedAtomicFile;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.util.concurrent.TimeUnit.SECONDS;
import static software.openx.eelaa.lmdb.LMDBFlags.*;
import static software.openx.eelaa.memory.MemorySegmentUtil.INT_LE;

/**
 * Synchronizer implementation that syncs transactions from GL file to LMDB storage engine.
 *
 * @author Alireza Pourtaghi
 */
final class GLFileSynchronizer implements Runnable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GLFileSynchronizer.class);

    private final ExecutorService executor;
    private final LZ4 lz4;
    private final ThreadConfinedAtomicFile transactionsFile;
    private final LMDBManager lmdbManager;
    private final int metadataDbi;
    private final int transactionsDbi;
    private final int walletsDbi;
    private final int ledgersDbi;

    private GLFileSynchronizer(final ExecutorService executor, final LZ4 lz4,
                               final ThreadConfinedAtomicFile transactionsFile, final LMDBManager lmdbManager,
                               final int metadataDbi, final int transactionsDbi, final int walletsDbi,
                               final int ledgersDbi) {

        this.executor = executor;
        this.lz4 = lz4;
        this.transactionsFile = transactionsFile;
        this.lmdbManager = lmdbManager;
        this.metadataDbi = metadataDbi;
        this.transactionsDbi = transactionsDbi;
        this.walletsDbi = walletsDbi;
        this.ledgersDbi = ledgersDbi;
    }

    public static GLFileSynchronizer newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4,
                                                 final ThreadConfinedAtomicFile transactionsFile,
                                                 final int databaseSizeGBs) throws Exception {

        final var executor = Executors.newSingleThreadExecutor();
        final var lmdbManager = executor.submit(() -> LMDBManager.newInstance(
                ledgerConfig.getDataDirectoryPath(),
                Math.max(1, databaseSizeGBs) * 1073741824L,
                4,
                MDB_NORDAHEAD,
                0644)).get();

        final var metadataDbi = executor.submit(() -> lmdbManager
                .openDb("metadata")).get();

        final var transactionsDbi = executor.submit(() -> lmdbManager
                .openDb("synced_transactions")).get();

        final var walletsDbi = executor.submit(() -> lmdbManager
                .openDb("synced_wallets")).get();

        final var ledgersDbi = executor.submit(() -> lmdbManager
                .openDb("synced_ledgers", MDB_CREATE | MDB_INTEGERKEY)).get();

        return new GLFileSynchronizer(
                executor, lz4, transactionsFile, lmdbManager, metadataDbi, transactionsDbi, walletsDbi, ledgersDbi);
    }

    public void start() {
        executor.execute(this);
    }

    @Override
    public void run() {
        try {
            if (transactionsFile.durabilitySize().get() > 256) {
                // We have some data to sync.
                try (final var arena = Arena.ofShared()) {
                    final var latestSyncedSizeSegment =
                            lmdbManager.get(metadataDbi, arena.allocateFrom("gl_file_latest_synced_size"), arena);

                    if (latestSyncedSizeSegment == NULL) {
                        syncFrom(256, arena);
                    } else {
                        final var latestSyncedSize = latestSyncedSizeSegment.get(JAVA_LONG, 0);
                        if (transactionsFile.durabilitySize().get() > latestSyncedSize) {
                            syncFrom(latestSyncedSize, arena);
                        }
                    }
                }
            }
        } catch (final Throwable cause) {
            logger.error("{}", cause.getMessage(), cause);
        } finally {
            try {
                executor.execute(this);
            } catch (final RejectedExecutionException _) {
                logger.warn("Rejected task because of closing executor!");
            }
        }
    }

    private void syncFrom(final long latestSyncedPosition, final Arena arena) throws Throwable {
        final var batchHeader = transactionsFile.read(arena, latestSyncedPosition, 10).get();

        final var length = batchHeader.get(INT_LE, 2);
        final var actualSize = batchHeader.get(INT_LE, 6);
        final var compressedSize = length - INT_LE.byteSize();

        final var compressedBatch =
                transactionsFile.read(arena, latestSyncedPosition + batchHeader.byteSize(), compressedSize).get();

        final var batch =
                arena.allocate(actualSize);

        lz4.decompressSafe(compressedBatch, batch, (int) compressedSize, actualSize);
        final var transactions = decodeBatch(batch);
        // TODO: Complete implementation.
    }

    private Transaction[] decodeBatch(final MemorySegment batch) {
        final var buffer = batch.asByteBuffer().order(ByteOrder.LITTLE_ENDIAN);

        var count = 0;
        // Counting number of items.
        while (buffer.remaining() > 0) {
            buffer.get();
            buffer.get();

            final var length = buffer.getInt();
            buffer.position(Math.addExact(buffer.position(), length));
            count++;
        }

        buffer.position(0);
        final var transactions = new Transaction[count];
        var index = 0;
        while (buffer.remaining() > 0) {
            final var version = buffer.get();
            final var flags = buffer.get();
            final var length = buffer.getInt();

            transactions[index++] = Transaction.decode(batch.asSlice(buffer.position(), length));
            buffer.position(Math.addExact(buffer.position(), length));
        }

        return transactions;
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
