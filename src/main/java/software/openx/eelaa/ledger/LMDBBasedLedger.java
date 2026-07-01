package software.openx.eelaa.ledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.lmdb.LMDBManager;
import software.openx.eelaa.lz4.LZ4;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.foreign.MemorySegment.NULL;
import static java.util.concurrent.TimeUnit.SECONDS;
import static software.openx.eelaa.lmdb.LMDBFlags.MDB_NORDAHEAD;

/**
 * LMDB based crash-safe monetary ledger implementation.
 *
 * @author Alireza Pourtaghi
 */
final class LMDBBasedLedger extends Ledger {
    private static final Logger logger = LoggerFactory.getLogger(LMDBBasedLedger.class);

    private final LMDBManager lmdbManager;
    private final int transactionsDbi;
    private final int walletsDbi;

    private LMDBBasedLedger(final ExecutorService executor, final Processor processor, final LZ4 lz4,
                            final LMDBManager lmdbManager, final int transactionsDbi, final int walletsDbi) {

        super(executor, processor, lz4);
        this.lmdbManager = lmdbManager;
        this.transactionsDbi = transactionsDbi;
        this.walletsDbi = walletsDbi;
    }

    public static Ledger newInstance(final LedgerConfig ledgerConfig, final LZ4 lz4, final Path lmdbLibraryPath,
                                     final int databaseSizeGbs) throws Exception {

        // Bounded queue executor with abort policy.
        final var queue = new ArrayBlockingQueue<Runnable>(ledgerConfig.getExecutorMaxWaitQueueSize());
        final var executor = new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue);
        final var processor = Processor.newInstance(ledgerConfig);
        final var lmdbManager = executor.submit(() -> LMDBManager.newInstance(
                lmdbLibraryPath,
                ledgerConfig.getDataDirectoryPath(),
                Math.max(1, databaseSizeGbs) * 1073741824L,
                2,
                MDB_NORDAHEAD,
                0644)).get();

        final var transactionsDbi = executor.submit(() -> lmdbManager.openDb("transactions")).get();
        final var walletsDbi = executor.submit(() -> lmdbManager.openDb("wallets")).get();

        return new LMDBBasedLedger(executor, processor, lz4, lmdbManager, transactionsDbi, walletsDbi);
    }

    @Override
    public CompletableFuture<Transaction> fetchTransaction(final int ledger, final String id) {
        return super.fetchTransaction(ledger, id).thenApplyAsync(fetchedTransaction -> {
            if (fetchedTransaction == null) {
                try (final var arena = Arena.ofConfined()) {
                    final var storedTransaction = lmdbManager.get(transactionsDbi, arena.allocateFrom(ledger + ":" + id), arena);
                    if (storedTransaction != NULL) {
                        return Transaction.decode(storedTransaction.asSlice(6));
                    }
                }
            }

            return fetchedTransaction;
        }, getExecutor());
    }

    @Override
    boolean persist(final Transaction... transactions) {
        try (final var arena = Arena.ofConfined()) {
            var commit = false;

            final var txn = lmdbManager.newTxn(arena, NULL, 0);
            try { // Atomic or exception code block!
                for (int i = 0; i < transactions.length; i++) {
                    final var transaction = transactions[i];
                    if (transaction != null && !transaction.is_failed()) {
                        final var sourceWallet = getProcessor()
                                .fetchWallet(transaction.getLedger(), transaction.getSourceAccount(), transaction.getSourceWallet())
                                .encodeV1(arena);

                        final var destinationWallet = getProcessor()
                                .fetchWallet(transaction.getLedger(), transaction.getDestinationAccount(), transaction.getDestinationWallet())
                                .encodeV1(arena);

                        final var key = arena.allocateFrom(transaction.getLedger() + ":" + transaction.getId());
                        if (lmdbManager.put(txn, transactionsDbi, key, transaction.encodeV1(arena))) {
                            lmdbManager.putOrReplace(txn, walletsDbi, sourceWallet.asSlice(6, 16), sourceWallet);
                            lmdbManager.putOrReplace(txn, walletsDbi, destinationWallet.asSlice(6, 16), destinationWallet);
                        } else {
                            throw new RuntimeException("duplicate transaction key provided!");
                        }
                    }
                }

                commit = true;
            } finally {
                finalize(txn, commit);
            }

            return commit;
        } catch (final Throwable cause) {
            // We must return back the transferred balances of in-memory wallets.
            getProcessor().reverseBalancesOfSucceededTransactions(transactions);

            logger.error("{}", cause.getMessage(), cause);
            return false;
        }
    }

    private void finalize(final MemorySegment txn, final boolean commit) {
        if (commit) {
            lmdbManager.commitTxn(txn);
        } else {
            lmdbManager.abortTxn(txn);
        }
    }

    @Override
    public void close() throws Exception {
        getExecutor().submit(() -> {
            try {
                lmdbManager.close();
            } catch (final Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        }).get();

        super.close();
    }
}
