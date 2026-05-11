package app.eelaa.core.std;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * C string handling functions.
 *
 * @author Alireza Pourtaghi
 */
public final class CString {
    private static final SymbolLookup lib =
            Linker.nativeLinker().defaultLookup();

    private static final MethodHandle strlenHandle =
            Linker.nativeLinker().downcallHandle(lib.find(FUNCTION.strlen.name()).orElseThrow(), FUNCTION.strlen.fd);

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
