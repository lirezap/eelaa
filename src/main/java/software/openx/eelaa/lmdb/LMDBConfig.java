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
package software.openx.eelaa.lmdb;

import java.lang.foreign.Arena;
import java.nio.file.Path;

/**
 * LMDB native library configuration fields.
 *
 * @author Alireza Pourtaghi
 */
public final class LMDBConfig {
    private final Path libraryPath;
    private final Arena arena;

    private LMDBConfig(final Builder builder) {
        this.libraryPath = builder.libraryPath;
        this.arena = builder.arena;
    }

    public Path getLibraryPath() {
        return libraryPath;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public String toString() {
        return "LMDBConfig{" +
                "libraryPath=" + libraryPath +
                ", arena=" + arena +
                '}';
    }

    /**
     * LMDB native library configuration's builder class.
     *
     * @author Alireza Pourtaghi
     */
    public static final class Builder {
        private Path libraryPath;
        private Arena arena = Arena.ofShared();

        public Builder(final Path libraryPath) {
            this.libraryPath = libraryPath;
        }

        public Builder libraryPath(final Path libraryPath) {
            this.libraryPath = libraryPath;
            return this;
        }

        public Builder arena(final Arena arena) {
            this.arena = arena;
            return this;
        }

        public LMDBConfig build() {
            return new LMDBConfig(this);
        }
    }
}
