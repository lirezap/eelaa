package app.eelaa.core.storage;

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
    private final Supplier<Long> sizeMethodSupplier;
    private final Runnable closeMethodRunnable;

    private ThreadConfinedAtomicFile(final ExecutorService executor, final AtomicFile file) {
        this.executor = executor;
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
