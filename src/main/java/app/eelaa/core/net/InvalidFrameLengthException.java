package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class InvalidFrameLengthException extends RuntimeException {
    public static final InvalidFrameLengthException INSTANCE = new InvalidFrameLengthException();

    private InvalidFrameLengthException() {
    }
}
