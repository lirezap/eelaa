package software.openx.eelaa.lmdb;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.*;
import static software.openx.eelaa.std.CString.strlen;

/**
 * Java FFM wrapper of the LMDB C library functions.
 *
 * @author Alireza Pourtaghi
 */
public final class LMDB implements AutoCloseable {
    private final Arena memory;
    private final MethodHandle mdbVersionHandle;
    private final MethodHandle mdbEnvCreateHandle;
    private final MethodHandle mdbEnvCloseHandle;
    private final MethodHandle mdbEnvSetMapSizeHandle;
    private final MethodHandle mdbEnvSetMaxDbsHandle;
    private final MethodHandle mdbEnvOpenHandle;
    private final MethodHandle mdbTxnBeginHandle;
    private final MethodHandle mdbTxnCommitHandle;
    private final MethodHandle mdbTxnAbortHandle;
    private final MethodHandle mdbDbiOpenHandle;
    private final MethodHandle mdbGetHandle;
    private final MethodHandle mdbPutHandle;

    private LMDB(final Arena memory, final MethodHandle mdbVersionHandle, final MethodHandle mdbEnvCreateHandle,
                 final MethodHandle mdbEnvCloseHandle, final MethodHandle mdbEnvSetMapSizeHandle,
                 final MethodHandle mdbEnvSetMaxDbsHandle, final MethodHandle mdbEnvOpenHandle,
                 final MethodHandle mdbTxnBeginHandle, final MethodHandle mdbTxnCommitHandle,
                 final MethodHandle mdbTxnAbortHandle, final MethodHandle mdbDbiOpenHandle,
                 final MethodHandle mdbGetHandle, final MethodHandle mdbPutHandle) {

        this.memory = memory;
        this.mdbVersionHandle = mdbVersionHandle;
        this.mdbEnvCreateHandle = mdbEnvCreateHandle;
        this.mdbEnvCloseHandle = mdbEnvCloseHandle;
        this.mdbEnvSetMapSizeHandle = mdbEnvSetMapSizeHandle;
        this.mdbEnvSetMaxDbsHandle = mdbEnvSetMaxDbsHandle;
        this.mdbEnvOpenHandle = mdbEnvOpenHandle;
        this.mdbTxnBeginHandle = mdbTxnBeginHandle;
        this.mdbTxnCommitHandle = mdbTxnCommitHandle;
        this.mdbTxnAbortHandle = mdbTxnAbortHandle;
        this.mdbDbiOpenHandle = mdbDbiOpenHandle;
        this.mdbGetHandle = mdbGetHandle;
        this.mdbPutHandle = mdbPutHandle;
    }

    public static LMDB newInstance(final LMDBConfig lmdbConfig) {
        final var memory = lmdbConfig.getMemory();
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(lmdbConfig.getLibraryPath(), memory);

        return new LMDB(
                memory,
                linker.downcallHandle(lib.find(FUNCTION.mdb_version.name()).orElseThrow(), FUNCTION.mdb_version.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_create.name()).orElseThrow(), FUNCTION.mdb_env_create.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_close.name()).orElseThrow(), FUNCTION.mdb_env_close.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_set_mapsize.name()).orElseThrow(), FUNCTION.mdb_env_set_mapsize.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_set_maxdbs.name()).orElseThrow(), FUNCTION.mdb_env_set_maxdbs.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_open.name()).orElseThrow(), FUNCTION.mdb_env_open.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_txn_begin.name()).orElseThrow(), FUNCTION.mdb_txn_begin.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_txn_commit.name()).orElseThrow(), FUNCTION.mdb_txn_commit.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_txn_abort.name()).orElseThrow(), FUNCTION.mdb_txn_abort.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_dbi_open.name()).orElseThrow(), FUNCTION.mdb_dbi_open.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_get.name()).orElseThrow(), FUNCTION.mdb_get.fd),
                linker.downcallHandle(lib.find(FUNCTION.mdb_put.name()).orElseThrow(), FUNCTION.mdb_put.fd));
    }

    public String mdbVersion() throws Throwable {
        final var ptr = (MemorySegment) mdbVersionHandle.invokeExact(NULL, NULL, NULL);
        return ptr.reinterpret(Math.addExact(strlen(ptr), 1)).getString(0);
    }

    public int mdbEnvCreate(final MemorySegment envPtr) throws Throwable {
        return (int) mdbEnvCreateHandle.invokeExact(envPtr);
    }

    public void mdbEnvClose(final MemorySegment env) throws Throwable {
        mdbEnvCloseHandle.invokeExact(env);
    }

    public int mdbEnvSetMapSize(final MemorySegment env, final long size) throws Throwable {
        return (int) mdbEnvSetMapSizeHandle.invokeExact(env, size);
    }

    public int mdbEnvSetMaxDbs(final MemorySegment env, final int dbs) throws Throwable {
        return (int) mdbEnvSetMaxDbsHandle.invokeExact(env, dbs);
    }

    public int mdbEnvOpen(final MemorySegment env, final MemorySegment path, final int flags,
                          final int mode) throws Throwable {

        return (int) mdbEnvOpenHandle.invokeExact(env, path, flags, mode);
    }

    public int mdbTxnBegin(final MemorySegment env, final MemorySegment parent, final int flags,
                           final MemorySegment txnPtr) throws Throwable {

        return (int) mdbTxnBeginHandle.invokeExact(env, parent, flags, txnPtr);
    }

    public int mdbTxnCommit(final MemorySegment txn) throws Throwable {
        return (int) mdbTxnCommitHandle.invokeExact(txn);
    }

    public void mdbTxnAbort(final MemorySegment txn) throws Throwable {
        mdbTxnAbortHandle.invokeExact(txn);
    }

    public int mdbDbiOpen(final MemorySegment txn, final MemorySegment name, final int flags,
                          final MemorySegment dbi) throws Throwable {

        return (int) mdbDbiOpenHandle.invokeExact(txn, name, flags, dbi);
    }

    public int mdbGet(final MemorySegment txn, final int dbi, final MemorySegment key,
                      final MemorySegment data) throws Throwable {

        return (int) mdbGetHandle.invokeExact(txn, dbi, key, data);
    }

    public int mdbPut(final MemorySegment txn, final int dbi, final MemorySegment key,
                      final MemorySegment data, final int flags) throws Throwable {

        return (int) mdbPutHandle.invokeExact(txn, dbi, key, data, flags);
    }

    @Override
    public void close() throws Exception {
        try {
            memory.close();
        } catch (final UnsupportedOperationException _) {
            // Just ignore close operation failure.
        }
    }

    /**
     * Name and descriptor of loaded C functions.
     *
     * @author Alireza Pourtaghi
     */
    private enum FUNCTION {
        mdb_version(FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS)),
        mdb_env_create(FunctionDescriptor.of(JAVA_INT, ADDRESS)),
        mdb_env_close(FunctionDescriptor.ofVoid(ADDRESS)),
        mdb_env_set_mapsize(FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG)),
        mdb_env_set_maxdbs(FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)),
        mdb_env_open(FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT)),
        mdb_txn_begin(FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS)),
        mdb_txn_commit(FunctionDescriptor.of(JAVA_INT, ADDRESS)),
        mdb_txn_abort(FunctionDescriptor.ofVoid(ADDRESS)),
        mdb_dbi_open(FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS)),
        mdb_get(FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)),
        mdb_put(FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT));

        public final FunctionDescriptor fd;

        FUNCTION(final FunctionDescriptor fd) {
            this.fd = fd;
        }
    }
}
