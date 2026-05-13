package app.eelaa.core.lz4;

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
        private Arena memory = Arena.global();

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
