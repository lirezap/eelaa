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
 * HTTP server compression configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class HTTPServerCompressionConfig {
    private final int contentSizeThreshold;
    private final int compressionLevel;
    private final int windowBits;
    private final int memLevel;

    private HTTPServerCompressionConfig(final Builder builder) {
        this.contentSizeThreshold = builder.contentSizeThreshold;
        this.compressionLevel = builder.compressionLevel;
        this.windowBits = builder.windowBits;
        this.memLevel = builder.memLevel;
    }

    public int getContentSizeThreshold() {
        return contentSizeThreshold;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public int getWindowBits() {
        return windowBits;
    }

    public int getMemLevel() {
        return memLevel;
    }

    @Override
    public String toString() {
        return "HTTPServerCompressionConfig{" +
                "contentSizeThreshold=" + contentSizeThreshold +
                ", compressionLevel=" + compressionLevel +
                ", windowBits=" + windowBits +
                ", memLevel=" + memLevel +
                '}';
    }

    /**
     * HTTP server compression configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private int contentSizeThreshold = 512;
        private int compressionLevel = 6;
        private int windowBits = 15;
        private int memLevel = 8;

        public Builder() {
        }

        public Builder contentSizeThreshold(final int contentSizeThreshold) {
            this.contentSizeThreshold = Math.max(0, contentSizeThreshold);
            return this;
        }

        public Builder compressionLevel(final int compressionLevel) {
            this.compressionLevel = Math.min(Math.max(0, compressionLevel), 9);
            return this;
        }

        public Builder windowBits(final int windowBits) {
            this.windowBits = Math.min(Math.max(9, windowBits), 15);
            return this;
        }

        public Builder memLevel(final int memLevel) {
            this.memLevel = Math.min(Math.max(1, memLevel), 9);
            return this;
        }

        public HTTPServerCompressionConfig build() {
            return new HTTPServerCompressionConfig(this);
        }
    }
}
