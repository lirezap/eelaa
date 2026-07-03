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
package software.openx.eelaa.lz4;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static software.openx.eelaa.std.CString.strlen;

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

    private LZ4(final Arena memory, final MethodHandle versionNumberHandle, final MethodHandle versionStringHandle,
                final MethodHandle compressBoundHandle, final MethodHandle compressDefaultHandle,
                final MethodHandle decompressSafeHandle) {

        this.memory = memory;
        this.versionNumberHandle = versionNumberHandle;
        this.versionStringHandle = versionStringHandle;
        this.compressBoundHandle = compressBoundHandle;
        this.compressDefaultHandle = compressDefaultHandle;
        this.decompressSafeHandle = decompressSafeHandle;
    }

    public static LZ4 newInstance(final LZ4Config lz4Config) {
        final var memory = lz4Config.getMemory();
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(lz4Config.getLibraryPath(), memory);

        return new LZ4(
                memory,
                linker.downcallHandle(lib.find(FUNCTION.LZ4_versionNumber.name()).orElseThrow(), FUNCTION.LZ4_versionNumber.fd),
                linker.downcallHandle(lib.find(FUNCTION.LZ4_versionString.name()).orElseThrow(), FUNCTION.LZ4_versionString.fd),
                linker.downcallHandle(lib.find(FUNCTION.LZ4_compressBound.name()).orElseThrow(), FUNCTION.LZ4_compressBound.fd),
                linker.downcallHandle(lib.find(FUNCTION.LZ4_compress_default.name()).orElseThrow(), FUNCTION.LZ4_compress_default.fd),
                linker.downcallHandle(lib.find(FUNCTION.LZ4_decompress_safe.name()).orElseThrow(), FUNCTION.LZ4_decompress_safe.fd));
    }

    public int versionNumber() throws Throwable {
        return (int) versionNumberHandle.invokeExact();
    }

    public String versionString() throws Throwable {
        final var versionPtr = (MemorySegment) versionStringHandle.invokeExact();
        return versionPtr.reinterpret(Math.addExact(strlen(versionPtr), 1)).getString(0);
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
