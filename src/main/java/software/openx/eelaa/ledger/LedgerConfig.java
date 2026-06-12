package software.openx.eelaa.ledger;

import java.nio.file.Path;

/**
 * Ledger configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class LedgerConfig {
    static final long TRANSACTION_ID_REQUIRED_BACKOFF_MS = 5000;

    private final Path dataDirectoryPath;
    private final int succeededTransactionsCacheTTLSeconds;
    private final int initialAccountsCap;
    private final int initialWalletsPerAccountCap;

    private LedgerConfig(final Builder builder) {
        this.dataDirectoryPath = builder.dataDirectoryPath;
        this.succeededTransactionsCacheTTLSeconds = builder.succeededTransactionsCacheTTLSeconds;
        this.initialAccountsCap = builder.initialAccountsCap;
        this.initialWalletsPerAccountCap = builder.initialWalletsPerAccountCap;
    }

    public Path getDataDirectoryPath() {
        return dataDirectoryPath;
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
        private int succeededTransactionsCacheTTLSeconds = (int) (TRANSACTION_ID_REQUIRED_BACKOFF_MS / 1000 * 2);
        private int initialAccountsCap = 64000;
        private int initialWalletsPerAccountCap = 4;

        public Builder() {
        }

        public Builder dataDirectoryPath(final Path dataDirectoryPath) {
            this.dataDirectoryPath = dataDirectoryPath;
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
