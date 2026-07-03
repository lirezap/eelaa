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
 * TCP server configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class TCPServerConfig {
    private final String host;
    private final int port;
    private final int eventLoopGroupNThreads;
    private final int soBacklog;
    private final int maxFrameSize;
    private final int lowWriteBufferWaterMark;
    private final int highWriteBufferWaterMark;
    private final TLSContextConfig tlsContextConfig;
    private final int allIdleTimeoutSeconds;
    private final boolean logFrameHeader;
    private final CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig;
    private final int shutdownQuietPeriodSeconds;
    private final int shutdownWaitTimeSeconds;
    private final boolean detectResourceLeak;

    private TCPServerConfig(final Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.eventLoopGroupNThreads = builder.eventLoopGroupNThreads;
        this.soBacklog = builder.soBacklog;
        this.maxFrameSize = builder.maxFrameSize;
        this.lowWriteBufferWaterMark = builder.lowWriteBufferWaterMark;
        this.highWriteBufferWaterMark = builder.highWriteBufferWaterMark;
        this.tlsContextConfig = builder.tlsContextConfig;
        this.allIdleTimeoutSeconds = builder.allIdleTimeoutSeconds;
        this.logFrameHeader = builder.logFrameHeader;
        this.cpuHeavyTaskExecutorConfig = builder.cpuHeavyTaskExecutorConfig;
        this.shutdownQuietPeriodSeconds = builder.shutdownQuietPeriodSeconds;
        this.shutdownWaitTimeSeconds = builder.shutdownWaitTimeSeconds;
        this.detectResourceLeak = builder.detectResourceLeak;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getEventLoopGroupNThreads() {
        return eventLoopGroupNThreads;
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

    public int getAllIdleTimeoutSeconds() {
        return allIdleTimeoutSeconds;
    }

    public boolean isLogFrameHeader() {
        return logFrameHeader;
    }

    public CPUHeavyTaskExecutorConfig getCpuHeavyTaskExecutorConfig() {
        return cpuHeavyTaskExecutorConfig;
    }

    public int getShutdownQuietPeriodSeconds() {
        return shutdownQuietPeriodSeconds;
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
                ", eventLoopGroupNThreads=" + eventLoopGroupNThreads +
                ", soBacklog=" + soBacklog +
                ", maxFrameSize=" + maxFrameSize +
                ", lowWriteBufferWaterMark=" + lowWriteBufferWaterMark +
                ", highWriteBufferWaterMark=" + highWriteBufferWaterMark +
                ", tlsContextConfig=" + tlsContextConfig +
                ", allIdleTimeoutSeconds=" + allIdleTimeoutSeconds +
                ", logFrameHeader=" + logFrameHeader +
                ", cpuHeavyTaskExecutorConfig=" + cpuHeavyTaskExecutorConfig +
                ", shutdownQuietPeriodSeconds=" + shutdownQuietPeriodSeconds +
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
        private int eventLoopGroupNThreads = Runtime.getRuntime().availableProcessors();
        private int soBacklog = 8192;
        private int maxFrameSize = 5242880;
        private int lowWriteBufferWaterMark = 32 * 1024;
        private int highWriteBufferWaterMark = 128 * 1024;
        private TLSContextConfig tlsContextConfig = new TLSContextConfig.Builder().build();
        private int allIdleTimeoutSeconds = 120;
        private boolean logFrameHeader = false;
        private CPUHeavyTaskExecutorConfig cpuHeavyTaskExecutorConfig = new CPUHeavyTaskExecutorConfig.Builder().build();
        private int shutdownQuietPeriodSeconds = 1;
        private int shutdownWaitTimeSeconds = 30;
        private boolean detectResourceLeak = false;

        public Builder() {
        }

        public Builder host(final String host) {
            this.host = host;
            return this;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public Builder eventLoopGroupNThreads(final int eventLoopGroupNThreads) {
            this.eventLoopGroupNThreads = Math.max(1, eventLoopGroupNThreads);
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

        public Builder allIdleTimeoutSeconds(final int allIdleTimeoutSeconds) {
            this.allIdleTimeoutSeconds = Math.max(10, allIdleTimeoutSeconds);
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

        public Builder shutdownQuietPeriodSeconds(final int shutdownQuietPeriodSeconds) {
            this.shutdownQuietPeriodSeconds = Math.max(1, shutdownQuietPeriodSeconds);
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
