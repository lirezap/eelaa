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

import java.nio.file.Path;

/**
 * TLS context configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class TLSContextConfig {
    private final boolean useTls;
    private final Path serverCertPath;
    private final Path serverKeyPath;
    private final boolean useOcsp;
    private final int sessionCacheSize;
    private final int sessionTimeoutSeconds;
    private final int handshakeTimeoutSeconds;

    private TLSContextConfig(final Builder builder) {
        this.useTls = builder.useTls;
        this.serverCertPath = builder.serverCertPath;
        this.serverKeyPath = builder.serverKeyPath;
        this.useOcsp = builder.useOcsp;
        this.sessionCacheSize = builder.sessionCacheSize;
        this.sessionTimeoutSeconds = builder.sessionTimeoutSeconds;
        this.handshakeTimeoutSeconds = builder.handshakeTimeoutSeconds;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public Path getServerCertPath() {
        return serverCertPath;
    }

    public Path getServerKeyPath() {
        return serverKeyPath;
    }

    public boolean isUseOcsp() {
        return useOcsp;
    }

    public int getSessionCacheSize() {
        return sessionCacheSize;
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public int getHandshakeTimeoutSeconds() {
        return handshakeTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "TLSContextConfig{" +
                "useTls=" + useTls +
                ", serverCertPath=" + serverCertPath +
                ", serverKeyPath=" + serverKeyPath +
                ", useOcsp=" + useOcsp +
                ", sessionCacheSize=" + sessionCacheSize +
                ", sessionTimeoutSeconds=" + sessionTimeoutSeconds +
                ", handshakeTimeoutSeconds=" + handshakeTimeoutSeconds +
                '}';
    }

    /**
     * TLS configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private boolean useTls = false;
        private Path serverCertPath = Path.of("");
        private Path serverKeyPath = Path.of("");
        private boolean useOcsp = false;
        private int sessionCacheSize = 20000;
        private int sessionTimeoutSeconds = 300;
        private int handshakeTimeoutSeconds = 5;

        public Builder() {
        }

        public Builder useTls(final boolean useTls) {
            this.useTls = useTls;
            return this;
        }

        public Builder serverCertPath(final Path serverCertPath) {
            this.serverCertPath = serverCertPath;
            return this;
        }

        public Builder serverKeyPath(final Path serverKeyPath) {
            this.serverKeyPath = serverKeyPath;
            return this;
        }

        public Builder useOcsp(final boolean useOcsp) {
            this.useOcsp = useOcsp;
            return this;
        }

        public Builder sessionCacheSize(final int sessionCacheSize) {
            this.sessionCacheSize = Math.max(1024, sessionCacheSize);
            return this;
        }

        public Builder sessionTimeoutSeconds(final int sessionTimeoutSeconds) {
            this.sessionTimeoutSeconds = Math.max(1, sessionTimeoutSeconds);
            return this;
        }

        public Builder handshakeTimeoutSeconds(final int handshakeTimeoutSeconds) {
            this.handshakeTimeoutSeconds = Math.max(1, handshakeTimeoutSeconds);
            return this;
        }

        public TLSContextConfig build() {
            return new TLSContextConfig(this);
        }
    }
}
