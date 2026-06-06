package software.openx.eelaa.net;

/**
 * CPU heavy task executor configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class CPUHeavyTaskExecutorConfig {
    private final int nThreads;
    private final int shutdownQuitePeriodSeconds;
    private final int shutdownWaitTimeSeconds;

    private CPUHeavyTaskExecutorConfig(final Builder builder) {
        this.nThreads = builder.nThreads;
        this.shutdownQuitePeriodSeconds = builder.shutdownQuitePeriodSeconds;
        this.shutdownWaitTimeSeconds = builder.shutdownWaitTimeSeconds;
    }

    public int getNThreads() {
        return nThreads;
    }

    public int getShutdownQuitePeriodSeconds() {
        return shutdownQuitePeriodSeconds;
    }

    public int getShutdownWaitTimeSeconds() {
        return shutdownWaitTimeSeconds;
    }

    @Override
    public String toString() {
        return "CPUHeavyTaskExecutorConfig{" +
                "nThreads=" + nThreads +
                ", shutdownQuitePeriodSeconds=" + shutdownQuitePeriodSeconds +
                ", shutdownWaitTimeSeconds=" + shutdownWaitTimeSeconds +
                '}';
    }

    /**
     * CPU heavy task executor configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private int nThreads = 4;
        private int shutdownQuitePeriodSeconds = 1;
        private int shutdownWaitTimeSeconds = 30;

        public Builder() {
        }

        public Builder nThreads(final int nThreads) {
            this.nThreads = Math.max(1, nThreads);
            return this;
        }

        public Builder shutdownQuitePeriodSeconds(final int shutdownQuitePeriodSeconds) {
            this.shutdownQuitePeriodSeconds = Math.max(1, shutdownQuitePeriodSeconds);
            return this;
        }

        public Builder shutdownWaitTimeSeconds(final int shutdownWaitTimeSeconds) {
            this.shutdownWaitTimeSeconds = Math.max(1, shutdownWaitTimeSeconds);
            return this;
        }

        public CPUHeavyTaskExecutorConfig build() {
            return new CPUHeavyTaskExecutorConfig(this);
        }
    }
}
