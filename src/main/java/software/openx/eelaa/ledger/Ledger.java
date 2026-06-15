package software.openx.eelaa.ledger;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.lz4.LZ4;
import software.openx.eelaa.storage.AtomicFile;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Lock-free, high performance and crash-safe monetary ledger implementation.
 *
 * @author Alireza Pourtaghi
 */
public final class Ledger implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Ledger.class);

    private final ExecutorService executor;
    private final Processor processor;
    private final AtomicFile transactionsFile;
    private final LZ4 lz4;

    private Ledger(final ExecutorService executor, final Processor processor, final AtomicFile transactionsFile,
                   final LZ4 lz4) {

        this.executor = executor;
        this.processor = processor;
        this.transactionsFile = transactionsFile;
        this.lz4 = lz4;
    }

    public static Ledger newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4) throws Exception {
        // Bounded queue executor with abort policy.
        final var queue = new ArrayBlockingQueue<Runnable>(ledgerConfig.getExecutorMaxWaitQueueSize());
        final var executor = new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue);
        final var processor = Processor.newInstance(ledgerConfig);
        final var transactionsFile = AtomicFile.newInstance(
                ledgerConfig.getDataDirectoryPath().resolve("transactions.gl"));

        return new Ledger(executor, processor, transactionsFile, lz4);
    }

    static Ledger newTestInstance(final LedgerConfig ledgerConfig, final LZ4 lz4) throws Exception {
        // Bounded queue executor with abort policy.
        final var queue = new ArrayBlockingQueue<Runnable>(ledgerConfig.getExecutorMaxWaitQueueSize());
        final var executor = new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue);
        final var processor = Processor.newInstance(ledgerConfig);
        final var transactionsFile = AtomicFile.newInstance(
                ledgerConfig.getDataDirectoryPath().resolve(System.currentTimeMillis() + "-" + "transactions.gl"));

        return new Ledger(executor, processor, transactionsFile, lz4);
    }

    public CompletableFuture<Boolean> process(final Transaction... transactions) {
        return CompletableFuture.supplyAsync(() -> {
            processor.process(transactions);
            var succeededCount = 0;
            for (final var transaction : transactions) {
                if (transaction != null && !transaction.is_failed()) succeededCount++;
            }

            return persist(transactions, succeededCount) ? Boolean.TRUE : Boolean.FALSE;
        }, executor);
    }

    public CompletableFuture<Boolean> processAtomically(final Transaction... transactions) {
        return CompletableFuture.supplyAsync(() -> {
            if (processor.processAtomically(transactions)) {
                return persist(transactions, transactions.length) ? Boolean.TRUE : Boolean.FALSE;
            }

            return Boolean.TRUE;
        }, executor);
    }

    public CompletableFuture<ObjectOpenHashSet<Wallet>> fetchAccount(final int ledger, final long account) {
        return CompletableFuture.supplyAsync(() -> processor.fetchAccount(ledger, account), executor);
    }

    public CompletableFuture<Wallet> fetchWallet(final int ledger, final long account, final int wallet) {
        return CompletableFuture.supplyAsync(() -> processor.fetchWallet(ledger, account, wallet), executor);
    }

    public CompletableFuture<Transaction> fetchTransaction(final int ledger, final String id) {
        return CompletableFuture.supplyAsync(() -> processor.fetchTransaction(ledger, id), executor);
    }

    private boolean persist(final Transaction[] transactions, final int persistSize) {
        // TODO: Complete implementation.
        return true;
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(60, SECONDS)) {
            // Safe to ignore runnable list!
            executor.shutdownNow();
        }

        transactionsFile.close();
    }
}
