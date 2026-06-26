package software.openx.eelaa.lmdb;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static software.openx.eelaa.std.CString.strlen;

/**
 * Java FFM wrapper of the LMDB C library functions.
 *
 * @author Alireza Pourtaghi
 */
public final class LMDB implements AutoCloseable {
    private final Arena memory;
    private final MethodHandle mdbVersionHandle;

    private LMDB(final Arena memory, final MethodHandle mdbVersionHandle) {
        this.memory = memory;
        this.mdbVersionHandle = mdbVersionHandle;
    }

    public static LMDB newInstance(final LMDBConfig lmdbConfig) {
        final var memory = lmdbConfig.getMemory();
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(lmdbConfig.getLibraryPath(), memory);

        final var mdbVersionHandle =
                linker.downcallHandle(lib.find(FUNCTION.mdb_version.name()).orElseThrow(), FUNCTION.mdb_version.fd);

        return new LMDB(memory, mdbVersionHandle);
    }

    public String mdbVersion() throws Throwable {
        final var ptr = (MemorySegment) mdbVersionHandle.invokeExact(NULL, NULL, NULL);
        return ptr.reinterpret(Math.addExact(strlen(ptr), 1)).getString(0);
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
        mdb_version(FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS));

        public final FunctionDescriptor fd;

        FUNCTION(final FunctionDescriptor fd) {
            this.fd = fd;
        }
    }
}
