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
package software.openx.eelaa.lz4;

import java.lang.foreign.Arena;
import java.nio.file.Path;

/**
 * LZ4 native library configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class LZ4Config {
    private final Path libraryPath;
    private final Arena memory;

    private LZ4Config(final Builder builder) {
        this.libraryPath = builder.libraryPath;
        this.memory = builder.memory;
    }

    public Path getLibraryPath() {
        return libraryPath;
    }

    public Arena getMemory() {
        return memory;
    }

    @Override
    public String toString() {
        return "LZ4Config{" +
                "libraryPath=" + libraryPath +
                ", memory=" + memory +
                '}';
    }

    /**
     * LZ4 native library configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private Path libraryPath;
        private Arena memory = Arena.ofShared();

        public Builder(final Path libraryPath) {
            this.libraryPath = libraryPath;
        }

        public Builder libraryPath(final Path libraryPath) {
            this.libraryPath = libraryPath;
            return this;
        }

        public Builder memory(final Arena memory) {
            this.memory = memory;
            return this;
        }

        public LZ4Config build() {
            return new LZ4Config(this);
        }
    }
}
