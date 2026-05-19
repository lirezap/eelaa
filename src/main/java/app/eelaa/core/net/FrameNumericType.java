package app.eelaa.core.net;

/**
 * The list of all frame numeric type values.
 *
 * @author Alireza Pourtaghi
 */
public enum FrameNumericType {
    ERROR(-1),
    NOTHING(0),

    PING(1),
    PONG(1001);

    private static final FrameNumericType[] frameNumericTypes = FrameNumericType.values();
    private final int value;

    FrameNumericType(final int value) {
        this.value = value;
    }

    public static FrameNumericType of(final int value) {
        for (var fnt : frameNumericTypes) {
            if (fnt.value == value) return fnt;
        }

        return null;
    }

    public int value() {
        return value;
    }
}
