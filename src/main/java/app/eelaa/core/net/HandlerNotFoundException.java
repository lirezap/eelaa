package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class HandlerNotFoundException extends RuntimeException {
    public static final HandlerNotFoundException INSTANCE = new HandlerNotFoundException();

    private HandlerNotFoundException() {
    }
}
