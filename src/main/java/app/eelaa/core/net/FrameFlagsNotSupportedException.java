package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class FrameFlagsNotSupportedException extends RuntimeException {
    public static final FrameFlagsNotSupportedException INSTANCE = new FrameFlagsNotSupportedException();

    private FrameFlagsNotSupportedException() {
    }
}
