package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class DecompressionFailedException extends RuntimeException {
    public static final DecompressionFailedException INSTANCE = new DecompressionFailedException();

    private DecompressionFailedException() {
    }
}
