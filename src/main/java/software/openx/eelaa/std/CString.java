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
package software.openx.eelaa.std;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * C string handling functions.
 *
 * @author Alireza Pourtaghi
 */
public final class CString {
    private static final MethodHandle strlenHandle;

    static {
        final var lib = Linker.nativeLinker().defaultLookup();
        strlenHandle = Linker.nativeLinker().downcallHandle(lib.find(FUNCTION.strlen.name()).orElseThrow(), FUNCTION.strlen.fd);
    }

    public static long strlen(final MemorySegment string) throws Throwable {
        return (long) strlenHandle.invokeExact(string);
    }

    /**
     * Name and descriptor of C string handling functions.
     *
     * @author Alireza Pourtaghi
     */
    private enum FUNCTION {
        strlen(FunctionDescriptor.of(JAVA_LONG, ADDRESS));

        public final FunctionDescriptor fd;

        FUNCTION(final FunctionDescriptor fd) {
            this.fd = fd;
        }
    }
}
