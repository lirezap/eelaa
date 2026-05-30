package app.eelaa.core.net;

/**
 * @author Alireza Pourtaghi
 */
final class BadSequenceIdException extends RuntimeException {
    public static final BadSequenceIdException INSTANCE = new BadSequenceIdException();

    private BadSequenceIdException() {
    }
}
