/*
 * Copyright 2026 Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.openx.eelaa.net;

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
        // TODO: This design only works for single instance eelaa, should we care about it?
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
