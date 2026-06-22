package software.openx.eelaa.net;

/**
 * The list of all frame numeric type values.
 *
 * @author Alireza Pourtaghi
 */
public enum FrameNumericType {
    ERROR(-1),
    NOTHING(0),

    PING(1),
    PONG(2),

    FETCH_ACCOUNT(100),
    ACCOUNT(101),

    FETCH_WALLET(200),
    WALLET(201),

    BATCH(300),
    ATOMIC_BATCH(301),
    FAILED_TRANSACTIONS(302);

    private static final FrameNumericType[] frameNumericTypes = FrameNumericType.values();
    private final int value;

    FrameNumericType(final int value) {
        this.value = value;
    }

    public static FrameNumericType of(final int value) {
        for (final var fnt : frameNumericTypes) {
            if (fnt.value == value) return fnt;
        }

        return null;
    }

    public int value() {
        return value;
    }
}
