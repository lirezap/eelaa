package software.openx.eelaa.ledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.lz4.LZ4;
import software.openx.eelaa.storage.AtomicFile;

import java.lang.foreign.Arena;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static software.openx.eelaa.memory.MemorySegmentUtil.*;

/**
 * WAL based crash-safe monetary ledger implementation.
 *
 * @author Alireza Pourtaghi
 */
final class WALBasedLedger extends Ledger {
    private static final Logger logger = LoggerFactory.getLogger(WALBasedLedger.class);

    private final AtomicFile transactionsFile;

    private WALBasedLedger(final ExecutorService executor, final Processor processor, final LZ4 lz4,
                           final AtomicFile transactionsFile) {

        super(executor, processor, lz4);
        this.transactionsFile = transactionsFile;
    }

    public static Ledger newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4) throws Exception {
        // Bounded queue executor with abort policy.
        final var queue = new ArrayBlockingQueue<Runnable>(ledgerConfig.getExecutorMaxWaitQueueSize());
        final var executor = new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue);
        final var processor = Processor.newInstance(ledgerConfig);
        final var transactionsFile = executor.submit(() -> AtomicFile.newInstance(
                ledgerConfig.getDataDirectoryPath().resolve("transactions.gl"))).get();

        return new WALBasedLedger(executor, processor, lz4, transactionsFile);
    }

    @Override
    boolean persist(final Transaction... transactions) {
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
            final var requiredCompressionSpace = getLz4().compressBound((int) memory.byteSize());
            final var compressionMemory = arena.allocate(headerSize + requiredCompressionSpace);

            position = putByteLE(compressionMemory, 0, (byte) 0b00000001);
            position = putByteLE(compressionMemory, position, (byte) 0b00000001);
            position = putIntLE(compressionMemory, position, Math.addExact(4, requiredCompressionSpace));
            putIntLE(compressionMemory, position, (int) memory.byteSize());

            getLz4().compressDefault(
                    memory,
                    compressionMemory.asSlice(headerSize),
                    (int) memory.byteSize(),
                    requiredCompressionSpace);

            transactionsFile.append(compressionMemory.asByteBuffer());
            return true;
        } catch (final Throwable cause) {
            // We must return back the transferred balances of in-memory wallets.
            getProcessor().reverseBalancesOfSucceededTransactions(transactions);

            logger.error("{}", cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        getExecutor().submit(() -> {
            try {
                transactionsFile.close();
            } catch (final Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        }).get();

        super.close();
    }
}
