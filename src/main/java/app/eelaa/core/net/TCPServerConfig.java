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
    private final int cpuHeavyExecutorThreads;

    private TCPServerConfig(final Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.maxFrameSize = builder.maxFrameSize;
        this.logFrameHeader = builder.logFrameHeader;
        this.cpuHeavyExecutorThreads = builder.cpuHeavyExecutorThreads;
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

    public int getCpuHeavyExecutorThreads() {
        return cpuHeavyExecutorThreads;
    }

    @Override
    public String toString() {
        return "TCPServerConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", maxFrameSize=" + maxFrameSize +
                ", logFrameHeader=" + logFrameHeader +
                ", cpuHeavyExecutorThreads=" + cpuHeavyExecutorThreads +
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
        private int cpuHeavyExecutorThreads = 4;

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

        public Builder cpuHeavyExecutorThreads(final int cpuHeavyExecutorThreads) {
            // TODO: validate input.
            this.cpuHeavyExecutorThreads = cpuHeavyExecutorThreads;
            return this;
        }

        public TCPServerConfig build() {
            return new TCPServerConfig(this);
        }
    }
}
