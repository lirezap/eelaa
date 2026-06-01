package app.eelaa.core.storage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

/**
 * An atomic file implementation based on {@link FileChannel}. The implementation also holds the position value itself.
 *
 * @author Alireza Pourtaghi
 */
final class AtomicFile implements AutoCloseable {
    private final FileChannel file;
    private long position;

    private AtomicFile(final FileChannel fileChannel) {
        this.file = fileChannel;
        this.position = -1;
    }

    public static AtomicFile newInstance(final Path filePath) throws IOException {
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("provided path is not a regular file!");
        }

        final var instance = new AtomicFile(FileChannel.open(filePath, CREATE, READ, WRITE, SYNC));
        instance.adjustPosition();

        return instance;
    }

    public long size() throws IOException {
        return file.size();
    }

    private void adjustPosition() throws IOException {
        try (final var _ = file.lock()) {
            position = file.size();
        }
    }

    @Override
    public void close() throws Exception {
        if (file.isOpen()) file.close();
    }
}
