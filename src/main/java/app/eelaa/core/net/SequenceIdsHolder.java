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
        // Provided sequence id must be greater than zero.
        if (sequenceId <= 0) return false;

        // Provided sequence id must not be duplicate in the last N provided sequence ids.
        for (int i = 1; i < sequenceIds.length; i++) {
            if (sequenceIds[Math.abs(indexToAdd - i)] == sequenceId) return false;
        }

        // Provided sequence id must not be less than or equal to the minimum value of the last N provided sequence ids.
        var minimumStoredSequenceId = Integer.MAX_VALUE;
        for (final int id : sequenceIds) minimumStoredSequenceId = Math.min(minimumStoredSequenceId, id);
        if (sequenceId <= minimumStoredSequenceId) return false;

        sequenceIds[indexToAdd++] = sequenceId;
        if (indexToAdd == sequenceIds.length) indexToAdd = 0;

        return true;
    }
}
