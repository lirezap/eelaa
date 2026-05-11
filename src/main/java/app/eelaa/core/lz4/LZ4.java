package app.eelaa.core.lz4;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static app.eelaa.core.std.CString.strlen;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * Java FFM wrapper of the LZ4 C library functions.
 *
 * @author Alireza Pourtaghi
 */
public final class LZ4 implements AutoCloseable {
    private final Arena memory;
    private final MethodHandle versionNumberHandle;
    private final MethodHandle versionStringHandle;
    private final MethodHandle compressBoundHandle;
    private final MethodHandle compressDefaultHandle;
    private final MethodHandle decompressSafeHandle;

    /**
     * Creates native linker and library lookup instance to load the shared object or dynamic LZ4 C library from
     * provided path.
     *
     * @param lz4Config the configuration instance
     */
    public LZ4(final LZ4Config lz4Config) {
        this.memory = lz4Config.getMemory();
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(lz4Config.getLibraryPath(), memory);

        this.versionNumberHandle =
                linker.downcallHandle(lib.find(FUNCTION.LZ4_versionNumber.name()).orElseThrow(), FUNCTION.LZ4_versionNumber.fd);

        this.versionStringHandle =
                linker.downcallHandle(lib.find(FUNCTION.LZ4_versionString.name()).orElseThrow(), FUNCTION.LZ4_versionString.fd);

        this.compressBoundHandle =
                linker.downcallHandle(lib.find(FUNCTION.LZ4_compressBound.name()).orElseThrow(), FUNCTION.LZ4_compressBound.fd);

        this.compressDefaultHandle =
                linker.downcallHandle(lib.find(FUNCTION.LZ4_compress_default.name()).orElseThrow(), FUNCTION.LZ4_compress_default.fd);

        this.decompressSafeHandle =
                linker.downcallHandle(lib.find(FUNCTION.LZ4_decompress_safe.name()).orElseThrow(), FUNCTION.LZ4_decompress_safe.fd);
    }

    public int versionNumber() throws Throwable {
        return (int) versionNumberHandle.invokeExact();
    }

    public String versionString() throws Throwable {
        final var versionPtr = (MemorySegment) versionStringHandle.invokeExact();
        return versionPtr.reinterpret(strlen(versionPtr) + 1).getString(0);
    }

    public int compressBound(final int inputSize) throws Throwable {
        return (int) compressBoundHandle.invokeExact(inputSize);
    }

    public int compressDefault(final MemorySegment src, final MemorySegment dst, final int srcSize,
                               final int dstCapacity) throws Throwable {

        return (int) compressDefaultHandle.invokeExact(src, dst, srcSize, dstCapacity);
    }

    public int decompressSafe(final MemorySegment src, final MemorySegment dst, final int compressedSize,
                              final int dstCapacity) throws Throwable {

        return (int) decompressSafeHandle.invokeExact(src, dst, compressedSize, dstCapacity);
    }

    @Override
    public void close() throws Exception {
        memory.close();
    }

    /**
     * Name and descriptor of loaded C functions.
     *
     * @author Alireza Pourtaghi
     */
    private enum FUNCTION {
        LZ4_versionNumber(FunctionDescriptor.of(JAVA_INT)),
        LZ4_versionString(FunctionDescriptor.of(ADDRESS)),
        LZ4_compressBound(FunctionDescriptor.of(JAVA_INT, JAVA_INT)),
        LZ4_compress_default(FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT)),
        LZ4_decompress_safe(FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));

        public final FunctionDescriptor fd;

        FUNCTION(final FunctionDescriptor fd) {
            this.fd = fd;
        }
    }
}
