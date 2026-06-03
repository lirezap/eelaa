package app.eelaa.core.storage;

import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;

import static java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_LONG_UNALIGNED;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * @author Alireza Pourtaghi
 */
final class ValueLayouts {
    public static final OfInt INT = JAVA_INT_UNALIGNED.withOrder(LITTLE_ENDIAN);
    public static final OfLong LONG = JAVA_LONG_UNALIGNED.withOrder(LITTLE_ENDIAN);
}
