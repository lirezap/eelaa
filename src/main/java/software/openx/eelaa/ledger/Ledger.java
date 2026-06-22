package software.openx.eelaa.ledger;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.lz4.LZ4;
import software.openx.eelaa.storage.AtomicFile;

import java.lang.foreign.Arena;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static software.openx.eelaa.binary.MemorySegmentUtil.*;

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
        final var transactionsFile = executor.submit(() -> AtomicFile.newInstance(
                ledgerConfig.getDataDirectoryPath().resolve("transactions.gl"))).get();

        return new Ledger(executor, processor, transactionsFile, lz4);
    }

    static Ledger newTestInstance(final LedgerConfig ledgerConfig, final LZ4 lz4) throws Exception {
        // Bounded queue executor with abort policy.
        final var queue = new ArrayBlockingQueue<Runnable>(ledgerConfig.getExecutorMaxWaitQueueSize());
        final var executor = new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue);
        final var processor = Processor.newInstance(ledgerConfig);
        final var transactionsFile = executor.submit(() -> AtomicFile.newInstance(
                ledgerConfig.getDataDirectoryPath().resolve(System.currentTimeMillis() + "-" + "transactions.gl"))).get();

        return new Ledger(executor, processor, transactionsFile, lz4);
    }

    public CompletableFuture<Boolean> process(final Transaction... transactions) {
        return CompletableFuture.supplyAsync(() -> {
            processor.process(transactions);
            return persist(transactions) ? Boolean.TRUE : Boolean.FALSE;
        }, executor);
    }

    public CompletableFuture<Boolean> processAtomically(final Transaction... transactions) {
        return CompletableFuture.supplyAsync(() -> {
            if (processor.processAtomically(transactions)) {
                return persist(transactions) ? Boolean.TRUE : Boolean.FALSE;
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

    private boolean persist(final Transaction... transactions) {
        try (final var arena = Arena.ofConfined()) {
            var allocationSize = 0L;
            for (final var transaction : transactions) {
                if (transaction != null && !transaction.is_failed()) {
                    final var encoded = transaction.encodeV1(arena);
                    transaction.set_memoryPointer(encoded);
                    allocationSize += encoded.byteSize();
                }
            }

            if (allocationSize == 0) {
                // Nothing to append.
                return true;
            }

            final var memory = arena.allocate(allocationSize);
            var position = 0L;
            for (final var transaction : transactions) {
                if (transaction != null && !transaction.is_failed()) {
                    position = putMemory(memory, position, transaction.get_memoryPointer());
                }
            }

            final var headerSize = 10;
            final var requiredCompressionSpace = lz4.compressBound((int) memory.byteSize());
            final var compressionMemory = arena.allocate(headerSize + requiredCompressionSpace);

            position = putByteLE(compressionMemory, 0, (byte) 0b00000001);
            position = putByteLE(compressionMemory, position, (byte) 0b00000001);
            position = putIntLE(compressionMemory, position, 4 + requiredCompressionSpace);
            putIntLE(compressionMemory, position, (int) memory.byteSize());

            lz4.compressDefault(
                    memory,
                    compressionMemory.asSlice(headerSize),
                    (int) memory.byteSize(),
                    requiredCompressionSpace);

            transactionsFile.append(compressionMemory.asByteBuffer());
            return true;
        } catch (final Throwable cause) {
            // We must return back the transferred balances of in-memory wallets.
            processor.reverseBalancesOfSucceededTransactions(transactions);

            logger.error("{}", cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        executor.submit(() -> {
            try {
                transactionsFile.close();
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
