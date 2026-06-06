package software.openx.eelaa.net;

/**
 * @author Alireza Pourtaghi
 */
final class BadTimestampException extends RuntimeException {
    public static final BadTimestampException INSTANCE = new BadTimestampException();

    private BadTimestampException() {
    }
}
