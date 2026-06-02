package app.eelaa.core.storage;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A thread confined holder of {@link AtomicFile} instance for handling its method calls on specified thread.
 *
 * @author Alireza Pourtaghi
 */
public final class ThreadConfinedAtomicFile implements AutoCloseable {
    private final ExecutorService executor;
    private final AtomicFile file;
    private final Supplier<Long> sizeMethodSupplier;
    private final Runnable closeMethodRunnable;

    private ThreadConfinedAtomicFile(final ExecutorService executor, final AtomicFile file) {
        this.executor = executor;
        this.file = file;
        this.sizeMethodSupplier = sizeMethodSupplier(file);
        this.closeMethodRunnable = closeMethodRunnable(file);
    }

    public static CompletableFuture<ThreadConfinedAtomicFile> newInstance(final Path filePath) {
        final var executor = Executors.newSingleThreadExecutor();
        return CompletableFuture.supplyAsync(() -> {
            try {
                final var file = AtomicFile.newInstance(filePath);
                return new ThreadConfinedAtomicFile(executor, file);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, executor);
    }

    public CompletableFuture<Void> write(final ByteBuffer buffer, final long position) {
        return CompletableFuture.runAsync(() -> {
            try {
                file.write(buffer, position);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, executor);
    }

    public CompletableFuture<Void> write(final MemorySegment segment, final long position) {
        return CompletableFuture.runAsync(() -> {
            try {
                file.write(segment, position);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, executor);
    }

    public CompletableFuture<Void> append(final ByteBuffer buffer) {
        return CompletableFuture.runAsync(() -> {
            try {
                file.append(buffer);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, executor);
    }

    public CompletableFuture<Void> append(final MemorySegment segment) {
        return CompletableFuture.runAsync(() -> {
            try {
                file.append(segment);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, executor);
    }

    public MemorySegment read(final Arena arena, final long position, final long size) throws IOException {
        return file.read(arena, position, size);
    }

    public CompletableFuture<Long> size() {
        return CompletableFuture.supplyAsync(sizeMethodSupplier, executor);
    }

    private static Supplier<Long> sizeMethodSupplier(final AtomicFile file) {
        return () -> {
            try {
                return file.size();
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    private static Runnable closeMethodRunnable(final AtomicFile file) {
        return () -> {
            try {
                file.close();
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @Override
    public void close() throws Exception {
        CompletableFuture.runAsync(closeMethodRunnable, executor).get(10, SECONDS);

        executor.shutdown();
        if (!executor.awaitTermination(60, SECONDS)) {
            // Safe to ignore runnable list!
            executor.shutdownNow();
        }
    }
}
