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
package software.openx.eelaa.ledger;

import java.nio.file.Path;

/**
 * Ledger configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class LedgerConfig {
    static final long TRANSACTION_ID_REQUIRED_BACKOFF_MS = 10000;

    private final Path dataDirectoryPath;
    private final int executorMaxWaitQueueSize;
    private final int succeededTransactionsCacheTTLSeconds;
    private final int initialAccountsCap;
    private final int initialWalletsPerAccountCap;

    private LedgerConfig(final Builder builder) {
        this.dataDirectoryPath = builder.dataDirectoryPath;
        this.executorMaxWaitQueueSize = builder.executorMaxWaitQueueSize;
        this.succeededTransactionsCacheTTLSeconds = builder.succeededTransactionsCacheTTLSeconds;
        this.initialAccountsCap = builder.initialAccountsCap;
        this.initialWalletsPerAccountCap = builder.initialWalletsPerAccountCap;
    }

    public Path getDataDirectoryPath() {
        return dataDirectoryPath;
    }

    public int getExecutorMaxWaitQueueSize() {
        return executorMaxWaitQueueSize;
    }

    public int getSucceededTransactionsCacheTTLSeconds() {
        return succeededTransactionsCacheTTLSeconds;
    }

    public int getInitialAccountsCap() {
        return initialAccountsCap;
    }

    public int getInitialWalletsPerAccountCap() {
        return initialWalletsPerAccountCap;
    }

    @Override
    public String toString() {
        return "LedgerConfig{" +
                "dataDirectoryPath=" + dataDirectoryPath +
                ", executorMaxWaitQueueSize=" + executorMaxWaitQueueSize +
                ", succeededTransactionsCacheTTLSeconds=" + succeededTransactionsCacheTTLSeconds +
                ", initialAccountsCap=" + initialAccountsCap +
                ", initialWalletsPerAccountCap=" + initialWalletsPerAccountCap +
                '}';
    }

    /**
     * Ledger configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private Path dataDirectoryPath = Path.of("./");
        private int executorMaxWaitQueueSize = 64000;
        private int succeededTransactionsCacheTTLSeconds = (int) (TRANSACTION_ID_REQUIRED_BACKOFF_MS / 1000 * 2);
        private int initialAccountsCap = 64000;
        private int initialWalletsPerAccountCap = 4;

        public Builder() {
        }

        public Builder dataDirectoryPath(final Path dataDirectoryPath) {
            this.dataDirectoryPath = dataDirectoryPath;
            return this;
        }

        public Builder executorMaxWaitQueueSize(final int executorMaxWaitQueueSize) {
            this.executorMaxWaitQueueSize = Math.max(256, executorMaxWaitQueueSize);
            return this;
        }

        public Builder succeededTransactionsCacheTTLSeconds(final int succeededTransactionsCacheTTLSeconds) {
            if (succeededTransactionsCacheTTLSeconds > 60 * 60) {
                throw new IllegalArgumentException("invalid succeededTransactionsCacheTTLSeconds value provided");
            }

            this.succeededTransactionsCacheTTLSeconds =
                    Math.max((int) (TRANSACTION_ID_REQUIRED_BACKOFF_MS / 1000 * 2), succeededTransactionsCacheTTLSeconds);

            return this;
        }

        public Builder initialAccountsCap(final int initialAccountsCap) {
            this.initialAccountsCap = Math.max(1, initialAccountsCap);
            return this;
        }

        public Builder initialWalletsPerAccountCap(final int initialWalletsPerAccountCap) {
            this.initialWalletsPerAccountCap = Math.max(1, initialWalletsPerAccountCap);
            return this;
        }

        public LedgerConfig build() {
            return new LedgerConfig(this);
        }
    }
}
