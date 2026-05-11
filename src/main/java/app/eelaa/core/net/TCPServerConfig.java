package app.eelaa.core.net;

/**
 * TCP server configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class TCPServerConfig {
    private final String host;
    private final int port;
    private final int maxFrameSize;
    private final boolean logFrameHeader;
    private final int cpuHeavyTaskExecutorThreads;
    private final int shutdownQuitePeriodSeconds;
    private final int shutdownWaitTimeSeconds;

    private TCPServerConfig(final Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.maxFrameSize = builder.maxFrameSize;
        this.logFrameHeader = builder.logFrameHeader;
        this.cpuHeavyTaskExecutorThreads = builder.cpuHeavyTaskExecutorThreads;
        this.shutdownQuitePeriodSeconds = builder.shutdownQuitePeriodSeconds;
        this.shutdownWaitTimeSeconds = builder.shutdownWaitTimeSeconds;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public boolean isLogFrameHeader() {
        return logFrameHeader;
    }

    public int getCpuHeavyTaskExecutorThreads() {
        return cpuHeavyTaskExecutorThreads;
    }

    public int getShutdownQuitePeriodSeconds() {
        return shutdownQuitePeriodSeconds;
    }

    public int getShutdownWaitTimeSeconds() {
        return shutdownWaitTimeSeconds;
    }

    @Override
    public String toString() {
        return "TCPServerConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", maxFrameSize=" + maxFrameSize +
                ", logFrameHeader=" + logFrameHeader +
                ", cpuHeavyTaskExecutorThreads=" + cpuHeavyTaskExecutorThreads +
                ", shutdownQuitePeriodSeconds=" + shutdownQuitePeriodSeconds +
                ", shutdownWaitTimeSeconds=" + shutdownWaitTimeSeconds +
                '}';
    }

    /**
     * TCP server configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private String host = "localhost";
        private int port = 7178;
        private int maxFrameSize = 5242880;
        private boolean logFrameHeader = false;
        private int cpuHeavyTaskExecutorThreads = 4;
        private int shutdownQuitePeriodSeconds = 1;
        private int shutdownWaitTimeSeconds = 30;

        public Builder() {
        }

        public Builder host(final String host) {
            // TODO: validate input.
            this.host = host;
            return this;
        }

        public Builder port(final int port) {
            // TODO: validate input.
            this.port = port;
            return this;
        }

        public Builder maxFrameSize(final int maxFrameSize) {
            // TODO: validate input.
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        public Builder logFrameHeader(final boolean logFrameHeader) {
            this.logFrameHeader = logFrameHeader;
            return this;
        }

        public Builder cpuHeavyTaskExecutorThreads(final int cpuHeavyTaskExecutorThreads) {
            // TODO: validate input.
            this.cpuHeavyTaskExecutorThreads = cpuHeavyTaskExecutorThreads;
            return this;
        }

        public Builder shutdownQuitePeriodSeconds(final int shutdownQuitePeriodSeconds) {
            // TODO: validate input.
            this.shutdownQuitePeriodSeconds = shutdownQuitePeriodSeconds;
            return this;
        }

        public Builder shutdownWaitTimeSeconds(final int shutdownWaitTimeSeconds) {
            // TODO: validate input.
            this.shutdownWaitTimeSeconds = shutdownWaitTimeSeconds;
            return this;
        }

        public TCPServerConfig build() {
            return new TCPServerConfig(this);
        }
    }
}
