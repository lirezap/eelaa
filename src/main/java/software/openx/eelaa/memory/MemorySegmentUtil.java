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
package software.openx.eelaa.memory;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32C;

import static java.lang.foreign.ValueLayout.*;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Utility methods useful for working with off-heap memory.
 *
 * @author Alireza Pourtaghi
 */
public final class MemorySegmentUtil {
    public static final OfByte BYTE_LE = JAVA_BYTE.withOrder(LITTLE_ENDIAN);
    public static final OfByte BYTE_BE = JAVA_BYTE.withOrder(BIG_ENDIAN);

    public static final OfInt INT_LE = JAVA_INT_UNALIGNED.withOrder(LITTLE_ENDIAN);
    public static final OfInt INT_BE = JAVA_INT_UNALIGNED.withOrder(BIG_ENDIAN);

    public static final OfLong LONG_LE = JAVA_LONG_UNALIGNED.withOrder(LITTLE_ENDIAN);
    public static final OfLong LONG_BE = JAVA_LONG_UNALIGNED.withOrder(BIG_ENDIAN);

    public static long putByteLE(final MemorySegment memory, final long position, final byte value) {
        memory.set(BYTE_LE, position, value);
        return Math.addExact(position, BYTE_LE.byteSize());
    }

    public static long putByteBE(final MemorySegment memory, final long position, final byte value) {
        memory.set(BYTE_BE, position, value);
        return Math.addExact(position, BYTE_BE.byteSize());
    }

    public static byte getByteLE(final MemorySegment memory, final AtomicLong position) {
        return memory.get(BYTE_LE, position.getAndAdd(BYTE_LE.byteSize()));
    }

    public static byte getByteBE(final MemorySegment memory, final AtomicLong position) {
        return memory.get(BYTE_BE, position.getAndAdd(BYTE_BE.byteSize()));
    }

    public static long putIntLE(final MemorySegment memory, final long position, final int value) {
        memory.set(INT_LE, position, value);
        return Math.addExact(position, INT_LE.byteSize());
    }

    public static long putIntBE(final MemorySegment memory, final long position, final int value) {
        memory.set(INT_BE, position, value);
        return Math.addExact(position, INT_BE.byteSize());
    }

    public static int getIntLE(final MemorySegment memory, final AtomicLong position) {
        return memory.get(INT_LE, position.getAndAdd(INT_LE.byteSize()));
    }

    public static int getIntBE(final MemorySegment memory, final AtomicLong position) {
        return memory.get(INT_BE, position.getAndAdd(INT_BE.byteSize()));
    }

    public static long putLongLE(final MemorySegment memory, final long position, final long value) {
        memory.set(LONG_LE, position, value);
        return Math.addExact(position, LONG_LE.byteSize());
    }

    public static long putLongBE(final MemorySegment memory, final long position, final long value) {
        memory.set(LONG_BE, position, value);
        return Math.addExact(position, LONG_BE.byteSize());
    }

    public static long getLongLE(final MemorySegment memory, final AtomicLong position) {
        return memory.get(LONG_LE, position.getAndAdd(LONG_LE.byteSize()));
    }

    public static long getLongBE(final MemorySegment memory, final AtomicLong position) {
        return memory.get(LONG_BE, position.getAndAdd(LONG_BE.byteSize()));
    }

    public static long putString(final MemorySegment memory, final long position, final String value) {
        memory.setString(position, value);
        return Math.addExact(position, StringUtil.requiredNullTerminatedUTF8BytesLength(value));
    }

    public static String getString(final MemorySegment memory, final AtomicLong position) {
        final var value = memory.getString(position.get());
        position.addAndGet(StringUtil.requiredNullTerminatedUTF8BytesLength(value));

        return value;
    }

    public static long putMemory(final MemorySegment memory, final long position, final MemorySegment value) {
        MemorySegment.copy(value, 0, memory, position, value.byteSize());
        return Math.addExact(position, value.byteSize());
    }

    public static long computeCRC32C(final MemorySegment memory) {
        final var crc = new CRC32C();
        crc.update(memory.asByteBuffer());
        return crc.getValue();
    }

    public static boolean isComputedCRC32CValid(final MemorySegment memory, final long computedValue) {
        return computeCRC32C(memory) == computedValue;
    }
}
