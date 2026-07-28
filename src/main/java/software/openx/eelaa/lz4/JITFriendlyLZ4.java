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

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

import static software.openx.eelaa.std.CString.strlen;

/**
 * JIT friendly java FFM wrapper of the LZ4 C library functions.
 *
 * @author Alireza Pourtaghi
 */
public final class JITFriendlyLZ4 {
    private static final MethodHandle versionNumberHandle;
    private static final MethodHandle versionStringHandle;
    private static final MethodHandle compressBoundHandle;
    private static final MethodHandle compressDefaultHandle;
    private static final MethodHandle decompressSafeHandle;

    static {
        final var libraryPath = Path.of(System.getenv("LIBRARIES_NATIVE_LZ4_PATH"));
        final var linker = Linker.nativeLinker();
        final var lib = SymbolLookup.libraryLookup(libraryPath, Arena.global());

        versionNumberHandle = linker.downcallHandle(lib.find(LZ4.FUNCTION.LZ4_versionNumber.name()).orElseThrow(), LZ4.FUNCTION.LZ4_versionNumber.fd);
        versionStringHandle = linker.downcallHandle(lib.find(LZ4.FUNCTION.LZ4_versionString.name()).orElseThrow(), LZ4.FUNCTION.LZ4_versionString.fd);
        compressBoundHandle = linker.downcallHandle(lib.find(LZ4.FUNCTION.LZ4_compressBound.name()).orElseThrow(), LZ4.FUNCTION.LZ4_compressBound.fd);
        compressDefaultHandle = linker.downcallHandle(lib.find(LZ4.FUNCTION.LZ4_compress_default.name()).orElseThrow(), LZ4.FUNCTION.LZ4_compress_default.fd);
        decompressSafeHandle = linker.downcallHandle(lib.find(LZ4.FUNCTION.LZ4_decompress_safe.name()).orElseThrow(), LZ4.FUNCTION.LZ4_decompress_safe.fd);
    }

    public static int versionNumber() throws Throwable {
        return (int) versionNumberHandle.invokeExact();
    }

    public static String versionString() throws Throwable {
        final var versionPtr = (MemorySegment) versionStringHandle.invokeExact();
        return versionPtr.reinterpret(Math.addExact(strlen(versionPtr), 1)).getString(0);
    }

    public static int compressBound(final int inputSize) throws Throwable {
        return (int) compressBoundHandle.invokeExact(inputSize);
    }

    public static int compressDefault(final MemorySegment src, final MemorySegment dst, final int srcSize,
                                      final int dstCapacity) throws Throwable {

        return (int) compressDefaultHandle.invokeExact(src, dst, srcSize, dstCapacity);
    }

    public static int decompressSafe(final MemorySegment src, final MemorySegment dst, final int compressedSize,
                                     final int dstCapacity) throws Throwable {

        return (int) decompressSafeHandle.invokeExact(src, dst, compressedSize, dstCapacity);
    }
}
