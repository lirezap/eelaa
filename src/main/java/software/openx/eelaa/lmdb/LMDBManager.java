package software.openx.eelaa.lmdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.openx.eelaa.memory.MemorySegmentUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.*;
import static software.openx.eelaa.lmdb.LMDBFlags.*;

/**
 * {@link LMDB} manager. Must be used by a single thread.
 *
 * @author Alireza Pourtaghi
 */
public final class LMDBManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(LMDBManager.class);

    private final Arena memory;
    private final LMDB lmdb;
    private final MemorySegment env;

    private LMDBManager(final Arena memory, final LMDB lmdb, final MemorySegment env) {
        this.memory = memory;
        this.lmdb = lmdb;
        this.env = env;
    }

    public static LMDBManager newInstance(final Path lmdbLibraryPath, final Path dataDirectoryPath, final long mapSize,
                                          final int maxDbs, final int openFlags, final int openMode) {

        final var memory = Arena.ofConfined();
        try {
            if (!Files.isDirectory(dataDirectoryPath)) {
                throw new RuntimeException("invalid data directory path!");
            }

            final var lmdb = LMDB.newInstance(new LMDBConfig.Builder(lmdbLibraryPath).memory(memory).build());
            logger.info("Using LMDB version: {}", lmdb.mdbVersion());

            final var envPtr = memory.allocate(ADDRESS);
            var error = lmdb.mdbEnvCreate(envPtr);
            if (error != 0) {
                throw new RuntimeException(String.format("LMDB env creation failed with error code: %s", error));
            }

            final var env = envPtr.get(ADDRESS, 0);
            error = lmdb.mdbEnvSetMapSize(env, mapSize);
            if (error != 0) {
                lmdb.mdbEnvClose(env);
                throw new RuntimeException(String.format("LMDB set map size failed with error code: %s", error));
            }

            error = lmdb.mdbEnvSetMaxDbs(env, maxDbs);
            if (error != 0) {
                lmdb.mdbEnvClose(env);
                throw new RuntimeException(String.format("LMDB set max dbs failed with error code: %s", error));
            }

            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var path = shortLivedMemory.allocateFrom(dataDirectoryPath.toString());
                error = lmdb.mdbEnvOpen(env, path, openFlags, openMode);
                if (error != 0) {
                    lmdb.mdbEnvClose(env);
                    throw new RuntimeException(String.format("LMDB env open failed with error code: %s", error));
                }
            }

            return new LMDBManager(memory, lmdb, env);
        } catch (final Throwable cause) {
            memory.close();
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment newTxn(final Arena txnMemory, final MemorySegment parent, final int flags) {
        try {
            final var txnPtr = txnMemory.allocate(ADDRESS);
            var error = lmdb.mdbTxnBegin(env, parent, flags, txnPtr);
            if (error != 0) {
                throw new RuntimeException(String.format("LMDB txn begin failed with error code: %s", error));
            }

            return txnPtr.get(ADDRESS, 0);
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void commitTxn(final MemorySegment txn) {
        try {
            final var error = lmdb.mdbTxnCommit(txn);
            if (error != 0) {
                throw new RuntimeException(String.format("LMDB txn commit failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void abortTxn(final MemorySegment txn) {
        try {
            lmdb.mdbTxnAbort(txn);
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public int openDb(final String dbName) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var txn = newTxn(shortLivedMemory, NULL, 0);
                final var dbi = shortLivedMemory.allocate(ADDRESS);
                final var error = lmdb.mdbDbiOpen(txn, shortLivedMemory.allocateFrom(dbName), MDB_CREATE, dbi);
                if (error != 0) {
                    abortTxn(txn);
                    throw new RuntimeException(String.format("LMDB dbi open failed with error code: %s", error));
                }

                commitTxn(txn);
                return dbi.get(JAVA_INT, 0);
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public boolean put(final int dbi, final MemorySegment key, final MemorySegment value) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var txn = newTxn(shortLivedMemory, NULL, 0);
                var error = lmdb.mdbPut(txn, dbi, asLMDBVal(shortLivedMemory, key), asLMDBVal(shortLivedMemory, value), MDB_NOOVERWRITE);
                if (error == 0) {
                    commitTxn(txn);
                    return true;
                }

                if (error == -30799) {
                    abortTxn(txn);
                    return false;
                }

                abortTxn(txn);
                throw new RuntimeException(String.format("LMDB put failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public boolean put(final MemorySegment txn, final int dbi, final MemorySegment key, final MemorySegment value) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                var error = lmdb.mdbPut(txn, dbi, asLMDBVal(shortLivedMemory, key), asLMDBVal(shortLivedMemory, value), MDB_NOOVERWRITE);
                if (error == 0) {
                    return true;
                }

                if (error == -30799) {
                    return false;
                }

                throw new RuntimeException(String.format("LMDB put failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void putOrReplace(final int dbi, final MemorySegment key, final MemorySegment value) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var txn = newTxn(shortLivedMemory, NULL, 0);
                var error = lmdb.mdbPut(txn, dbi, asLMDBVal(shortLivedMemory, key), asLMDBVal(shortLivedMemory, value), 0);
                if (error == 0) {
                    commitTxn(txn);
                    return;
                }

                abortTxn(txn);
                throw new RuntimeException(String.format("LMDB put failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void putOrReplace(final MemorySegment txn, final int dbi, final MemorySegment key, final MemorySegment value) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                var error = lmdb.mdbPut(txn, dbi, asLMDBVal(shortLivedMemory, key), asLMDBVal(shortLivedMemory, value), 0);
                if (error == 0) {
                    return;
                }

                throw new RuntimeException(String.format("LMDB put failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment get(final int dbi, final MemorySegment key, final Arena valueMemory) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var txn = newTxn(shortLivedMemory, NULL, MDB_RDONLY);
                final var lmdbVal = shortLivedMemory.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
                var error = lmdb.mdbGet(txn, dbi, asLMDBVal(shortLivedMemory, key), lmdbVal);
                if (error == 0) {
                    final var size = lmdbVal.get(JAVA_LONG, 0);
                    final var value = valueMemory.allocate(size);
                    MemorySegmentUtil.putMemory(value, 0, lmdbVal.get(ADDRESS, JAVA_LONG.byteSize()).reinterpret(size));

                    commitTxn(txn);
                    return value;
                }

                if (error == -30798) {
                    abortTxn(txn);
                    return NULL;
                }

                abortTxn(txn);
                throw new RuntimeException(String.format("LMDB get failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment get(final MemorySegment txn, final int dbi, final MemorySegment key, final Arena valueMemory) {
        try {
            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var lmdbVal = shortLivedMemory.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
                var error = lmdb.mdbGet(txn, dbi, asLMDBVal(shortLivedMemory, key), lmdbVal);
                if (error == 0) {
                    final var size = lmdbVal.get(JAVA_LONG, 0);
                    final var value = valueMemory.allocate(size);
                    MemorySegmentUtil.putMemory(value, 0, lmdbVal.get(ADDRESS, JAVA_LONG.byteSize()).reinterpret(size));
                    return value;
                }

                if (error == -30798) {
                    return NULL;
                }

                throw new RuntimeException(String.format("LMDB get failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    private MemorySegment asLMDBVal(final Arena memory, final MemorySegment value) {
        final var lmdbVal = memory.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
        lmdbVal.set(JAVA_LONG, 0, value.byteSize());
        lmdbVal.set(ADDRESS, JAVA_LONG.byteSize(), value);

        return lmdbVal;
    }

    @Override
    public void close() throws Exception {
        try {
            lmdb.mdbEnvClose(env);
            memory.close();
        } catch (final Throwable cause) {
            throw new Exception(cause);
        }
    }
}
