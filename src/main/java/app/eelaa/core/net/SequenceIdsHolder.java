package app.eelaa.core.net;

/**
 * Non thread-safe but fast sequence ids holder. Designed to be used in netty's server socket channels. The purpose of
 * this class is to prevent duplicated sequence id processing, provided by a single channel (TCP connection). It holds
 * last N sequence ids provided and checks incoming frame's sequence id with all previously provided ones, and then
 * informs the caller about duplication.
 *
 * @author Alireza pourtaghi
 */
final class SequenceIdsHolder {
    private final int[] sequenceIds;
    private int indexToAdd;

    public SequenceIdsHolder() {
        this.sequenceIds = new int[512];
        this.indexToAdd = 0;
    }

    public boolean addSequenceId(final int sequenceId) {
        if (sequenceId <= 0) return false;

        var minimumStoredSequenceId = Integer.MAX_VALUE;
        for (int i = 1; i < sequenceIds.length; i++) {
            final var fetchedSequenceId = sequenceIds[Math.abs(indexToAdd - i)];
            if (fetchedSequenceId == sequenceId) return false;

            minimumStoredSequenceId = Math.min(minimumStoredSequenceId, fetchedSequenceId);
        }

        if (sequenceId <= minimumStoredSequenceId) return false;

        sequenceIds[indexToAdd++] = sequenceId;
        if (indexToAdd == sequenceIds.length) indexToAdd = 0;

        return true;
    }
}
