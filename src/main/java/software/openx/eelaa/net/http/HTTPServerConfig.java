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
package software.openx.eelaa.net.http;

/**
 * HTTP server configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class HTTPServerConfig {
    private final boolean enabled;
    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;
    private final int maxContentLength;
    private final HTTPServerCompressionConfig httpServerCompressionConfig;

    private HTTPServerConfig(final Builder builder) {
        this.enabled = builder.enabled;
        this.maxInitialLineLength = builder.maxInitialLineLength;
        this.maxHeaderSize = builder.maxHeaderSize;
        this.maxChunkSize = builder.maxChunkSize;
        this.maxContentLength = builder.maxContentLength;
        this.httpServerCompressionConfig = builder.httpServerCompressionConfig;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public HTTPServerCompressionConfig getHttpServerCompressionConfig() {
        return httpServerCompressionConfig;
    }

    @Override
    public String toString() {
        return "HTTPServerConfig{" +
                "enabled=" + enabled +
                ", maxInitialLineLength=" + maxInitialLineLength +
                ", maxHeaderSize=" + maxHeaderSize +
                ", maxChunkSize=" + maxChunkSize +
                ", maxContentLength=" + maxContentLength +
                ", httpServerCompressionConfig=" + httpServerCompressionConfig +
                '}';
    }

    /**
     * HTTP server configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private boolean enabled = false;
        private int maxInitialLineLength = 4096;
        private int maxHeaderSize = 8192;
        private int maxChunkSize = 8192;
        private int maxContentLength = 5242880;
        private HTTPServerCompressionConfig httpServerCompressionConfig = new HTTPServerCompressionConfig.Builder().build();

        public Builder() {
        }

        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder maxInitialLineLength(final int maxInitialLineLength) {
            this.maxInitialLineLength = Math.max(32, maxInitialLineLength);
            return this;
        }

        public Builder maxHeaderSize(final int maxHeaderSize) {
            this.maxHeaderSize = Math.max(32, maxHeaderSize);
            return this;
        }

        public Builder maxChunkSize(final int maxChunkSize) {
            this.maxChunkSize = Math.max(32, maxChunkSize);
            return this;
        }

        public Builder maxContentLength(final int maxContentLength) {
            this.maxContentLength = Math.max(32, maxContentLength);
            return this;
        }

        public Builder httpServerCompressionConfig(final HTTPServerCompressionConfig httpServerCompressionConfig) {
            this.httpServerCompressionConfig = httpServerCompressionConfig;
            return this;
        }

        public HTTPServerConfig build() {
            return new HTTPServerConfig(this);
        }
    }
}
