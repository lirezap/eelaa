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
    private final int soBacklog;
    private final int maxFrameSize;
    private final int lowWriteBufferWaterMark;
    private final int highWriteBufferWaterMark;
    private final TLSContextConfig tlsContextConfig;
    private final boolean logFrameHeader;
    private final CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig;
    private final LZ4Config lz4Config;
    private final int shutdownQuitePeriodSeconds;
    private final int shutdownWaitTimeSeconds;
    private final boolean detectResourceLeak;

    private TCPServerConfig(final Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.soBacklog = builder.soBacklog;
        this.maxFrameSize = builder.maxFrameSize;
        this.lowWriteBufferWaterMark = builder.lowWriteBufferWaterMark;
        this.highWriteBufferWaterMark = builder.highWriteBufferWaterMark;
        this.tlsContextConfig = builder.tlsContextConfig;
        this.logFrameHeader = builder.logFrameHeader;
        this.cpuHeavyTaskExecutorConfig = builder.cpuHeavyTaskExecutorConfig;
        this.lz4Config = builder.lz4Config;
        this.shutdownQuitePeriodSeconds = builder.shutdownQuitePeriodSeconds;
        this.shutdownWaitTimeSeconds = builder.shutdownWaitTimeSeconds;
        this.detectResourceLeak = builder.detectResourceLeak;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public int getLowWriteBufferWaterMark() {
        return lowWriteBufferWaterMark;
    }

    public int getHighWriteBufferWaterMark() {
        return highWriteBufferWaterMark;
    }

    public TLSContextConfig getTlsContextConfig() {
        return tlsContextConfig;
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

    public boolean isDetectResourceLeak() {
        return detectResourceLeak;
    }

    @Override
    public String toString() {
        return "TCPServerConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", soBacklog=" + soBacklog +
                ", maxFrameSize=" + maxFrameSize +
                ", lowWriteBufferWaterMark=" + lowWriteBufferWaterMark +
                ", highWriteBufferWaterMark=" + highWriteBufferWaterMark +
                ", tlsContextConfig=" + tlsContextConfig +
                ", logFrameHeader=" + logFrameHeader +
                ", cpuHeavyTaskExecutorConfig=" + cpuHeavyTaskExecutorConfig +
                ", lz4Config=" + lz4Config +
                ", shutdownQuitePeriodSeconds=" + shutdownQuitePeriodSeconds +
                ", shutdownWaitTimeSeconds=" + shutdownWaitTimeSeconds +
                ", detectResourceLeak=" + detectResourceLeak +
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
        private int soBacklog = 8192;
        private int maxFrameSize = 5242880;
        private int lowWriteBufferWaterMark = 32 * 1024;
        private int highWriteBufferWaterMark = 128 * 1024;
        private TLSContextConfig tlsContextConfig = new TLSContextConfig.Builder().build();
        private boolean logFrameHeader = false;
        private CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig = new CPUHeavyTaskExecutorConfig.Builder().build();
        private LZ4Config lz4Config;
        private int shutdownQuitePeriodSeconds = 1;
        private int shutdownWaitTimeSeconds = 30;
        private boolean detectResourceLeak = false;

        public Builder(final LZ4Config lz4Config) {
            this.lz4Config = lz4Config;
        }

        public Builder host(final String host) {
            this.host = host;
            return this;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public Builder soBacklog(final int soBacklog) {
            this.soBacklog = Math.max(0, soBacklog);
            return this;
        }

        public Builder maxFrameSize(final int maxFrameSize) {
            this.maxFrameSize = Math.max(16, maxFrameSize);
            return this;
        }

        public Builder lowWriteBufferWaterMark(final int lowWriteBufferWaterMark) {
            this.lowWriteBufferWaterMark = Math.max(1024, lowWriteBufferWaterMark);
            return this;
        }

        public Builder highWriteBufferWaterMark(final int highWriteBufferWaterMark) {
            this.highWriteBufferWaterMark = Math.max(2 * 1024, highWriteBufferWaterMark);
            return this;
        }

        public Builder tlsContextConfig(final TLSContextConfig tlsContextConfig) {
            this.tlsContextConfig = tlsContextConfig;
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
            this.shutdownQuitePeriodSeconds = Math.max(1, shutdownQuitePeriodSeconds);
            return this;
        }

        public Builder shutdownWaitTimeSeconds(final int shutdownWaitTimeSeconds) {
            this.shutdownWaitTimeSeconds = Math.max(1, shutdownWaitTimeSeconds);
            return this;
        }

        public Builder detectResourceLeak(final boolean detectResourceLeak) {
            this.detectResourceLeak = detectResourceLeak;
            return this;
        }

        public TCPServerConfig build() {
            return new TCPServerConfig(this);
        }
    }
}
