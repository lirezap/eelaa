package software.openx.eelaa.net;

/**
 * @author Alireza Pourtaghi
 */
final class BadSequenceIdException extends RuntimeException {
    public static final BadSequenceIdException INSTANCE = new BadSequenceIdException();

    private BadSequenceIdException() {
    }
}
