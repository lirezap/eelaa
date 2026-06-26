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
    private final Arena memory;

    private LMDBConfig(final Builder builder) {
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
        return "LMDBConfig{" +
                "libraryPath=" + libraryPath +
                ", memory=" + memory +
                '}';
    }

    /**
     * LMDB native library configuration's builder class.
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

        public LMDBConfig build() {
            return new LMDBConfig(this);
        }
    }
}
