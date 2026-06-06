package software.openx.eelaa.net;

/**
 * @author Alireza Pourtaghi
 */
final class InvalidHandlerException extends RuntimeException {
    public static final InvalidHandlerException INSTANCE = new InvalidHandlerException();

    private InvalidHandlerException() {
    }
}
