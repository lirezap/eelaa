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

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

import static java.lang.foreign.MemorySegment.NULL;
import static software.openx.eelaa.std.CString.strlen;

/**
 * Global and JIT friendly java FFM wrapper of the LMDB C library functions.
 *
 * @author Alireza Pourtaghi
 */
final class GlobalLMDB {
    private static final MethodHandle mdbVersionHandle;
    private static final MethodHandle mdbEnvCreateHandle;
    private static final MethodHandle mdbEnvCloseHandle;
    private static final MethodHandle mdbEnvSetMapSizeHandle;
    private static final MethodHandle mdbEnvSetMaxDbsHandle;
    private static final MethodHandle mdbEnvOpenHandle;
    private static final MethodHandle mdbTxnBeginHandle;
    private static final MethodHandle mdbTxnCommitHandle;
    private static final MethodHandle mdbTxnAbortHandle;
    private static final MethodHandle mdbDbiOpenHandle;
    private static final MethodHandle mdbGetHandle;
    private static final MethodHandle mdbPutHandle;
    private static final MethodHandle mdbCursorOpenHandle;
    private static final MethodHandle mdbCursorCloseHandle;
    private static final MethodHandle mdbCursorGetHandle;

    static {
        final var libraryPath = Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH"));
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(libraryPath, Arena.global());

        mdbVersionHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_version.name()).orElseThrow(), LMDB.FUNCTION.mdb_version.fd);
        mdbEnvCreateHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_env_create.name()).orElseThrow(), LMDB.FUNCTION.mdb_env_create.fd);
        mdbEnvCloseHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_env_close.name()).orElseThrow(), LMDB.FUNCTION.mdb_env_close.fd);
        mdbEnvSetMapSizeHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_env_set_mapsize.name()).orElseThrow(), LMDB.FUNCTION.mdb_env_set_mapsize.fd);
        mdbEnvSetMaxDbsHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_env_set_maxdbs.name()).orElseThrow(), LMDB.FUNCTION.mdb_env_set_maxdbs.fd);
        mdbEnvOpenHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_env_open.name()).orElseThrow(), LMDB.FUNCTION.mdb_env_open.fd);
        mdbTxnBeginHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_txn_begin.name()).orElseThrow(), LMDB.FUNCTION.mdb_txn_begin.fd);
        mdbTxnCommitHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_txn_commit.name()).orElseThrow(), LMDB.FUNCTION.mdb_txn_commit.fd);
        mdbTxnAbortHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_txn_abort.name()).orElseThrow(), LMDB.FUNCTION.mdb_txn_abort.fd);
        mdbDbiOpenHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_dbi_open.name()).orElseThrow(), LMDB.FUNCTION.mdb_dbi_open.fd);
        mdbGetHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_get.name()).orElseThrow(), LMDB.FUNCTION.mdb_get.fd);
        mdbPutHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_put.name()).orElseThrow(), LMDB.FUNCTION.mdb_put.fd);
        mdbCursorOpenHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_cursor_open.name()).orElseThrow(), LMDB.FUNCTION.mdb_cursor_open.fd);
        mdbCursorCloseHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_cursor_close.name()).orElseThrow(), LMDB.FUNCTION.mdb_cursor_close.fd);
        mdbCursorGetHandle = linker.downcallHandle(lib.find(LMDB.FUNCTION.mdb_cursor_get.name()).orElseThrow(), LMDB.FUNCTION.mdb_cursor_get.fd);
    }

    public static String mdbVersion() throws Throwable {
        final var ptr = (MemorySegment) mdbVersionHandle.invokeExact(NULL, NULL, NULL);
        return ptr.reinterpret(Math.addExact(strlen(ptr), 1)).getString(0);
    }

    public static int mdbEnvCreate(final MemorySegment envPtr) throws Throwable {
        return (int) mdbEnvCreateHandle.invokeExact(envPtr);
    }

    public static void mdbEnvClose(final MemorySegment env) throws Throwable {
        mdbEnvCloseHandle.invokeExact(env);
    }

    public static int mdbEnvSetMapSize(final MemorySegment env, final long size) throws Throwable {
        return (int) mdbEnvSetMapSizeHandle.invokeExact(env, size);
    }

    public static int mdbEnvSetMaxDbs(final MemorySegment env, final int dbs) throws Throwable {
        return (int) mdbEnvSetMaxDbsHandle.invokeExact(env, dbs);
    }

    public static int mdbEnvOpen(final MemorySegment env, final MemorySegment path, final int flags,
                                 final int mode) throws Throwable {

        return (int) mdbEnvOpenHandle.invokeExact(env, path, flags, mode);
    }

    public static int mdbTxnBegin(final MemorySegment env, final MemorySegment parent, final int flags,
                                  final MemorySegment txnPtr) throws Throwable {

        return (int) mdbTxnBeginHandle.invokeExact(env, parent, flags, txnPtr);
    }

    public static int mdbTxnCommit(final MemorySegment txn) throws Throwable {
        return (int) mdbTxnCommitHandle.invokeExact(txn);
    }

    public static void mdbTxnAbort(final MemorySegment txn) throws Throwable {
        mdbTxnAbortHandle.invokeExact(txn);
    }

    public static int mdbDbiOpen(final MemorySegment txn, final MemorySegment name, final int flags,
                                 final MemorySegment dbi) throws Throwable {

        return (int) mdbDbiOpenHandle.invokeExact(txn, name, flags, dbi);
    }

    public static int mdbGet(final MemorySegment txn, final int dbi, final MemorySegment key,
                             final MemorySegment data) throws Throwable {

        return (int) mdbGetHandle.invokeExact(txn, dbi, key, data);
    }

    public static int mdbPut(final MemorySegment txn, final int dbi, final MemorySegment key,
                             final MemorySegment data, final int flags) throws Throwable {

        return (int) mdbPutHandle.invokeExact(txn, dbi, key, data, flags);
    }

    public static int mdbCursorOpen(final MemorySegment txn, final int dbi,
                                    final MemorySegment cursorPtr) throws Throwable {

        return (int) mdbCursorOpenHandle.invokeExact(txn, dbi, cursorPtr);
    }

    public static void mdbCursorClose(final MemorySegment cursor) throws Throwable {
        mdbCursorCloseHandle.invokeExact(cursor);
    }

    public static int mdbCursorGet(final MemorySegment cursor, final MemorySegment key, final MemorySegment data,
                                   final int op) throws Throwable {

        return (int) mdbCursorGetHandle.invokeExact(cursor, key, data, op);
    }
}
