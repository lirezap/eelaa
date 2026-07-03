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
 * The list of all frame numeric type values.
 *
 * @author Alireza Pourtaghi
 */
public enum FrameNumericType {
    ERROR(-1),
    NOTHING(0),

    PING(1),
    PONG(2),

    FETCH_ACCOUNT(100),
    ACCOUNT(101),

    FETCH_WALLET(200),
    WALLET(201),

    BATCH(300),
    ATOMIC_BATCH(301),
    FAILED_TRANSACTIONS(302);

    private static final FrameNumericType[] frameNumericTypes = FrameNumericType.values();
    private final int value;

    FrameNumericType(final int value) {
        this.value = value;
    }

    public static FrameNumericType of(final int value) {
        for (final var fnt : frameNumericTypes) {
            if (fnt.value == value) return fnt;
        }

        return null;
    }

    public int value() {
        return value;
    }
}
