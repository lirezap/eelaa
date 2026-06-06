package software.openx.eelaa.net;

/**
 * @author Alireza Pourtaghi
 */
final class FrameVersionNotSupportedException extends RuntimeException {
    public static final FrameVersionNotSupportedException INSTANCE = new FrameVersionNotSupportedException();

    private FrameVersionNotSupportedException() {
    }
}
