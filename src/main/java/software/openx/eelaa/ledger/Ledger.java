package software.openx.eelaa.ledger;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import software.openx.eelaa.lz4.LZ4;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Lock-free, high performance and in-memory monetary ledger implementation.
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

    public static Ledger newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4, final Path lmdbLibraryPath,
                                     final int databaseSizeGbs) throws Exception {

        return LMDBBasedLedger.newInstance(ledgerConfig, lz4, lmdbLibraryPath, databaseSizeGbs);
    }

    public static Ledger newFastInstance(final LedgerConfig ledgerConfig, final LZ4 lz4) throws Exception {
        return WALBasedLedger.newInstance(ledgerConfig, lz4);
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

    public final CompletableFuture<Transaction> fetchTransaction(final int ledger, final String id) {
        return CompletableFuture.supplyAsync(() -> processor.fetchTransaction(ledger, id), executor);
    }

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
