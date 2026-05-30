package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class InvalidActualSizeException extends RuntimeException {
    public static final InvalidActualSizeException INSTANCE = new InvalidActualSizeException();

    private InvalidActualSizeException() {
    }
}
