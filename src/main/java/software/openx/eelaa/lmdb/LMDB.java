package software.openx.eelaa.lmdb;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
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

    private LMDB(final Arena memory, final MethodHandle mdbVersionHandle, final MethodHandle mdbEnvCreateHandle,
                 final MethodHandle mdbEnvCloseHandle) {

        this.memory = memory;
        this.mdbVersionHandle = mdbVersionHandle;
        this.mdbEnvCreateHandle = mdbEnvCreateHandle;
        this.mdbEnvCloseHandle = mdbEnvCloseHandle;
    }

    public static LMDB newInstance(final LMDBConfig lmdbConfig) {
        final var memory = lmdbConfig.getMemory();
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(lmdbConfig.getLibraryPath(), memory);

        final var mdbVersionHandle =
                linker.downcallHandle(lib.find(FUNCTION.mdb_version.name()).orElseThrow(), FUNCTION.mdb_version.fd);

        final var mdbEnvCreateHandle =
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_create.name()).orElseThrow(), FUNCTION.mdb_env_create.fd);

        final var mdbEnvCloseHandle =
                linker.downcallHandle(lib.find(FUNCTION.mdb_env_close.name()).orElseThrow(), FUNCTION.mdb_env_close.fd);

        return new LMDB(memory, mdbVersionHandle, mdbEnvCreateHandle, mdbEnvCloseHandle);
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
        mdb_env_close(FunctionDescriptor.ofVoid(ADDRESS));

        public final FunctionDescriptor fd;

        FUNCTION(final FunctionDescriptor fd) {
            this.fd = fd;
        }
    }
}
