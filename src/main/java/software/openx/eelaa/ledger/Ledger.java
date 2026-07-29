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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import software.openx.eelaa.lz4.LZ4;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Lock-free, high performance and in-memory monetary general ledger implementation.
 *
 * @author Alireza Pourtaghi
 */
public sealed abstract class Ledger implements AutoCloseable permits LMDBBasedLedger, WALBasedLedger {
    private final ExecutorService executor;
    private final Processor processor;
    private final LZ4 lz4;

    Ledger(final ExecutorService executor, final Processor processor, final LZ4 lz4) {
        this.executor = executor;
        this.processor = processor;
        this.lz4 = lz4;
    }

    public static Ledger newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4,
                                     final int databaseSizeGBs) throws Exception {

        final var ledger = LMDBBasedLedger.newInstance(ledgerConfig, lz4, databaseSizeGBs);
        ledger.getExecutor().submit(ledger::loadWallets).get();

        return ledger;
    }

    public static Ledger newFastInstance(final LedgerConfig ledgerConfig, final LZ4 lz4,
                                         final int databaseSizeGBs) throws Exception {

        final var ledger = WALBasedLedger.newInstance(ledgerConfig, lz4, databaseSizeGBs);
        ledger.getExecutor().submit(ledger::loadWallets).get();

        return ledger;
    }

    public final CompletableFuture<Boolean> process(final Transaction... transactions) {
        return CompletableFuture.supplyAsync(() -> {
            processor.process(transactions);
            return persist(transactions) ? Boolean.TRUE : Boolean.FALSE;
        }, executor);
    }

    public final CompletableFuture<Boolean> processAtomically(final Transaction... transactions) {
        return CompletableFuture.supplyAsync(() -> {
            if (processor.processAtomically(transactions)) {
                return persist(transactions) ? Boolean.TRUE : Boolean.FALSE;
            }

            return Boolean.TRUE;
        }, executor);
    }

    public final CompletableFuture<ObjectOpenHashSet<Wallet>> fetchAccount(final int ledger, final long account) {
        return CompletableFuture.supplyAsync(() -> processor.fetchAccount(ledger, account), executor);
    }

    public final CompletableFuture<Wallet> fetchWallet(final int ledger, final long account, final int wallet) {
        return CompletableFuture.supplyAsync(() -> processor.fetchWallet(ledger, account, wallet), executor);
    }

    public CompletableFuture<Transaction> fetchTransaction(final int ledger, final String id) {
        return CompletableFuture.supplyAsync(() -> processor.fetchTransaction(ledger, id), executor);
    }

    abstract void loadWallets();

    abstract boolean persist(final Transaction... transactions);

    final ExecutorService getExecutor() {
        return executor;
    }

    final Processor getProcessor() {
        return processor;
    }

    final LZ4 getLz4() {
        return lz4;
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(60, SECONDS)) {
            // Safe to ignore runnable list!
            executor.shutdownNow();
        }
    }
}
