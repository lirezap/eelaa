package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class BadTimestampException extends RuntimeException {
    public static final BadTimestampException INSTANCE = new BadTimestampException();

    private BadTimestampException() {
    }
}
