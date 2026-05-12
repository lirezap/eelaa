package app.eelaa.core.net;

import app.eelaa.core.lz4.LZ4Config;

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
    private final CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig;
    private final LZ4Config lz4Config;
    private final int shutdownQuitePeriodSeconds;
    private final int shutdownWaitTimeSeconds;

    private TCPServerConfig(final Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.maxFrameSize = builder.maxFrameSize;
        this.logFrameHeader = builder.logFrameHeader;
        this.cpuHeavyTaskExecutorConfig = builder.cpuHeavyTaskExecutorConfig;
        this.lz4Config = builder.lz4Config;
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

    public CPUHeavyTaskExecutorConfig getCpuHeavyTaskExecutorConfig() {
        return cpuHeavyTaskExecutorConfig;
    }

    public LZ4Config getLz4Config() {
        return lz4Config;
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
                ", cpuHeavyTaskExecutorConfig=" + cpuHeavyTaskExecutorConfig +
                ", lz4Config=" + lz4Config +
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
        private CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig = new CPUHeavyTaskExecutorConfig.Builder().build();
        private LZ4Config lz4Config;
        private int shutdownQuitePeriodSeconds = 1;
        private int shutdownWaitTimeSeconds = 30;

        public Builder(final LZ4Config lz4Config) {
            this.lz4Config = lz4Config;
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

        public Builder cpuHeavyTaskExecutorConfig(final CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig) {
            this.cpuHeavyTaskExecutorConfig = cpuHeavyTaskExecutorConfig;
            return this;
        }

        public Builder lz4Config(final LZ4Config lz4Config) {
            this.lz4Config = lz4Config;
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
