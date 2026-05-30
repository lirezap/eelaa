package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class InvalidHandlerException extends RuntimeException {
    public static final InvalidHandlerException INSTANCE = new InvalidHandlerException();

    private InvalidHandlerException() {
    }
}
