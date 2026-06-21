package software.openx.eelaa;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Alireza Pourtaghi
 */
public final class ValueLayouts {
    public static final OfByte BYTE_LE = JAVA_BYTE.withOrder(LITTLE_ENDIAN);
    public static final OfByte BYTE_BE = JAVA_BYTE.withOrder(BIG_ENDIAN);

    public static final OfInt INT_LE = JAVA_INT_UNALIGNED.withOrder(LITTLE_ENDIAN);
    public static final OfInt INT_BE = JAVA_INT_UNALIGNED.withOrder(BIG_ENDIAN);

    public static final OfLong LONG_LE = JAVA_LONG_UNALIGNED.withOrder(LITTLE_ENDIAN);
    public static final OfLong LONG_BE = JAVA_LONG_UNALIGNED.withOrder(BIG_ENDIAN);

    public static long putByteLE(final MemorySegment memory, final long position, final byte value) {
        memory.set(BYTE_LE, position, value);
        return position + BYTE_LE.byteSize();
    }

    public static long putByteBE(final MemorySegment memory, final long position, final byte value) {
        memory.set(BYTE_BE, position, value);
        return position + BYTE_BE.byteSize();
    }

    public static long putIntLE(final MemorySegment memory, final long position, final int value) {
        memory.set(INT_LE, position, value);
        return position + INT_LE.byteSize();
    }

    public static long putIntBE(final MemorySegment memory, final long position, final int value) {
        memory.set(INT_BE, position, value);
        return position + INT_BE.byteSize();
    }

    public static long putLongLE(final MemorySegment memory, final long position, final long value) {
        memory.set(LONG_LE, position, value);
        return position + LONG_LE.byteSize();
    }

    public static long putLongBE(final MemorySegment memory, final long position, final long value) {
        memory.set(LONG_BE, position, value);
        return position + LONG_BE.byteSize();
    }

    public static long putString(final MemorySegment memory, final long position, final String value) {
        // Null terminated
        final var length = value.getBytes(UTF_8).length + 1;
        memory.setString(position, value);

        return position + length;
    }

    public static long putMemory(final MemorySegment memory, final long position, final MemorySegment value) {
        MemorySegment.copy(value, 0, memory, position, value.byteSize());
        return position + value.byteSize();
    }
}
