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
import static software.openx.eelaa.lmdb.CursorOperations.MDB_LAST;
import static software.openx.eelaa.lmdb.CursorOperations.MDB_NEXT;
import static software.openx.eelaa.lmdb.LMDBFlags.*;

/**
 * {@link LMDB} manager. Must be used by a single thread.
 *
 * @author Alireza Pourtaghi
 */
public final class LMDBManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(LMDBManager.class);

    private final Arena arena;
    private final MemorySegment env;

    private LMDBManager(final Arena arena, final MemorySegment env) {
        this.arena = arena;
        this.env = env;
    }

    public static LMDBManager newInstance(final Path dataDirectoryPath, final long mapSize, final int maxDbs,
                                          final int openFlags, final int openMode) {

        final var arena = Arena.ofConfined();
        try {
            if (!Files.isDirectory(dataDirectoryPath)) {
                throw new RuntimeException("invalid data directory path!");
            }

            logger.info("Using LMDB version: {}", JITFriendlyLMDB.mdbVersion());

            final var envPtr = arena.allocate(ADDRESS);
            var error = JITFriendlyLMDB.mdbEnvCreate(envPtr);
            if (error != 0) {
                throw new RuntimeException(String.format("LMDB env creation failed with error code: %s", error));
            }

            final var env = envPtr.get(ADDRESS, 0);
            error = JITFriendlyLMDB.mdbEnvSetMapSize(env, mapSize);
            if (error != 0) {
                JITFriendlyLMDB.mdbEnvClose(env);
                throw new RuntimeException(String.format("LMDB set map size failed with error code: %s", error));
            }

            error = JITFriendlyLMDB.mdbEnvSetMaxDbs(env, maxDbs);
            if (error != 0) {
                JITFriendlyLMDB.mdbEnvClose(env);
                throw new RuntimeException(String.format("LMDB set max dbs failed with error code: %s", error));
            }

            try (final var shortLivedMemory = Arena.ofConfined()) {
                final var path = shortLivedMemory.allocateFrom(dataDirectoryPath.toString());
                error = JITFriendlyLMDB.mdbEnvOpen(env, path, openFlags, openMode);
                if (error != 0) {
                    JITFriendlyLMDB.mdbEnvClose(env);
                    throw new RuntimeException(String.format("LMDB env open failed with error code: %s", error));
                }
            }

            return new LMDBManager(arena, env);
        } catch (final Throwable cause) {
            arena.close();
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment newTxn(final Arena arena, final MemorySegment parent, final int flags) {
        try {
            final var txnPtr = arena.allocate(ADDRESS);
            final var error = JITFriendlyLMDB.mdbTxnBegin(env, parent, flags, txnPtr);
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
            final var error = JITFriendlyLMDB.mdbTxnCommit(txn);
            if (error != 0) {
                throw new RuntimeException(String.format("LMDB txn commit failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void abortTxn(final MemorySegment txn) {
        try {
            JITFriendlyLMDB.mdbTxnAbort(txn);
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public int openDb(final String dbName) {
        try {
            try (final var arena = Arena.ofConfined()) {
                final var txn = newTxn(arena, NULL, 0);
                final var dbi = arena.allocate(ADDRESS);
                final var error = JITFriendlyLMDB.mdbDbiOpen(txn, arena.allocateFrom(dbName), MDB_CREATE, dbi);
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

    public int openDb(final String dbName, final int flags) {
        try {
            try (final var arena = Arena.ofConfined()) {
                final var txn = newTxn(arena, NULL, 0);
                final var dbi = arena.allocate(ADDRESS);
                final var error = JITFriendlyLMDB.mdbDbiOpen(txn, arena.allocateFrom(dbName), flags, dbi);
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
            try (final var arena = Arena.ofConfined()) {
                final var txn = newTxn(arena, NULL, 0);
                final var error = JITFriendlyLMDB.mdbPut(txn, dbi, asLMDBVal(arena, key), asLMDBVal(arena, value), MDB_NOOVERWRITE);
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
            try (final var arena = Arena.ofConfined()) {
                final var error = JITFriendlyLMDB.mdbPut(txn, dbi, asLMDBVal(arena, key), asLMDBVal(arena, value), MDB_NOOVERWRITE);
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
            try (final var arena = Arena.ofConfined()) {
                final var txn = newTxn(arena, NULL, 0);
                final var error = JITFriendlyLMDB.mdbPut(txn, dbi, asLMDBVal(arena, key), asLMDBVal(arena, value), 0);
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
            try (final var arena = Arena.ofConfined()) {
                final var error = JITFriendlyLMDB.mdbPut(txn, dbi, asLMDBVal(arena, key), asLMDBVal(arena, value), 0);
                if (error == 0) {
                    return;
                }

                throw new RuntimeException(String.format("LMDB put failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void append(final MemorySegment txn, final int dbi, final MemorySegment key, final MemorySegment value) {
        try {
            try (final var arena = Arena.ofConfined()) {
                final var error =
                        JITFriendlyLMDB.mdbPut(txn, dbi, asLMDBVal(arena, key), asLMDBVal(arena, value), MDB_APPEND | MDB_NOOVERWRITE);

                if (error == 0) {
                    return;
                }

                throw new RuntimeException(String.format("LMDB put failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment get(final int dbi, final MemorySegment key, final Arena valueArena) {
        try {
            try (final var arena = Arena.ofConfined()) {
                final var txn = newTxn(arena, NULL, MDB_RDONLY);
                final var lmdbVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
                final var error = JITFriendlyLMDB.mdbGet(txn, dbi, asLMDBVal(arena, key), lmdbVal);
                if (error == 0) {
                    final var size = lmdbVal.get(JAVA_LONG, 0);
                    final var value = valueArena.allocate(size);
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

    public MemorySegment get(final MemorySegment txn, final int dbi, final MemorySegment key, final Arena valueArena) {
        try {
            try (final var arena = Arena.ofConfined()) {
                final var lmdbVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
                final var error = JITFriendlyLMDB.mdbGet(txn, dbi, asLMDBVal(arena, key), lmdbVal);
                if (error == 0) {
                    final var size = lmdbVal.get(JAVA_LONG, 0);
                    final var value = valueArena.allocate(size);
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

    public MemorySegment newCursor(final MemorySegment txn, final int dbi, final Arena arena) {
        try {
            final var cursorPtr = arena.allocate(ADDRESS);
            final var error = JITFriendlyLMDB.mdbCursorOpen(txn, dbi, cursorPtr);
            if (error != 0) {
                throw new RuntimeException(String.format("LMDB cursor open failed with error code: %s", error));
            }

            return cursorPtr.get(ADDRESS, 0);
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public void closeCursor(final MemorySegment cursor) {
        try {
            JITFriendlyLMDB.mdbCursorClose(cursor);
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment iterateFromFirst(final MemorySegment cursor, final Arena valueArena) {
        try {
            try (final var arena = Arena.ofConfined()) {
                final var keyVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
                final var dataVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());

                final var error = JITFriendlyLMDB.mdbCursorGet(cursor, keyVal, dataVal, MDB_NEXT);
                if (error == 0) {
                    final var size = dataVal.get(JAVA_LONG, 0);
                    final var value = valueArena.allocate(size);
                    MemorySegmentUtil.putMemory(value, 0, dataVal.get(ADDRESS, JAVA_LONG.byteSize()).reinterpret(size));
                    return value;
                }

                if (error == -30798) {
                    return NULL;
                }

                throw new RuntimeException(String.format("LMDB cursor get failed with error code: %s", error));
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public MemorySegment lastIntegerKey(final MemorySegment txn, final int dbi, final Arena arena) {
        try {
            final var cursor = newCursor(txn, dbi, arena);
            try {
                final var keyVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
                final var dataVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());

                final var error = JITFriendlyLMDB.mdbCursorGet(cursor, keyVal, dataVal, MDB_LAST);
                if (error == 0) {
                    final var size = keyVal.get(JAVA_LONG, 0);
                    final var key = arena.allocate(size);
                    MemorySegmentUtil.putMemory(key, 0, keyVal.get(ADDRESS, JAVA_LONG.byteSize()).reinterpret(size));
                    return key;
                }

                if (error == -30798) {
                    return NULL;
                }

                throw new RuntimeException(String.format("LMDB cursor get failed with error code: %s", error));
            } finally {
                closeCursor(cursor);
            }
        } catch (final Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    private MemorySegment asLMDBVal(final Arena arena, final MemorySegment value) {
        final var lmdbVal = arena.allocate(JAVA_LONG.byteSize() + ADDRESS.byteSize());
        lmdbVal.set(JAVA_LONG, 0, value.byteSize());
        lmdbVal.set(ADDRESS, JAVA_LONG.byteSize(), value);

        return lmdbVal;
    }

    @Override
    public void close() throws Exception {
        try {
            JITFriendlyLMDB.mdbEnvClose(env);
            JITFriendlyLMDB.close();
            arena.close();
        } catch (final Throwable cause) {
            throw new Exception(cause);
        }
    }
}
