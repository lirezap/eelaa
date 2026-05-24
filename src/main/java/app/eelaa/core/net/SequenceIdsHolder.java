package app.eelaa.core.net;

/**
 * Non thread-safe but fast sequence ids holder. Designed to be used in netty's server socket channel. The purpose of
 * this class is to prevent duplicate sequence id processing, provided by a single channel (TCP connection). It holds
 * last N sequence ids provided and checks incoming frame's sequence id with all previously provided ones, and then
 * informs the caller about duplication.
 *
 * @author Alireza pourtaghi
 */
final class SequenceIdsHolder {
    private final int[] sequenceIds;
    private int indexToAdd;

    public SequenceIdsHolder() {
        this.sequenceIds = new int[256];
        this.indexToAdd = 0;
    }

    public boolean addSequenceId(final int sequenceId) {
        // TODO: This design only works for single instance core, should we care about it?
        // Provided sequence id must be greater than zero.
        if (sequenceId <= 0) {
            return false;
        }

        var minimumStoredSequenceId = Integer.MAX_VALUE;
        for (int i = 1; i < sequenceIds.length; i++) {
            final var indexToCheck = indexToAdd - i;
            final var valueToCheck = sequenceIds[indexToCheck >= 0 ? indexToCheck : indexToCheck + sequenceIds.length];
            // Provided sequence id must not be duplicate in the last N provided sequence ids.
            if (valueToCheck == sequenceId) {
                return false;
            }

            minimumStoredSequenceId = Math.min(minimumStoredSequenceId, valueToCheck);
        }

        // Provided sequence id must not be less than or equal to the minimum value of the last N provided sequence ids.
        if (sequenceId <= minimumStoredSequenceId) {
            return false;
        }

        sequenceIds[indexToAdd++] = sequenceId;
        if (indexToAdd == sequenceIds.length) indexToAdd = 0;

        return true;
    }
}
