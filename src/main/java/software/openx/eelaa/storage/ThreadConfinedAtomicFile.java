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
package software.openx.eelaa.storage;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
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

    private ThreadConfinedAtomicFile(final ExecutorService executor, final AtomicFile file,
                                     final Supplier<Long> sizeMethodSupplier, final Runnable closeMethodRunnable) {

        this.executor = executor;
        this.file = file;
        this.sizeMethodSupplier = sizeMethodSupplier;
        this.closeMethodRunnable = closeMethodRunnable;
    }

    public static CompletableFuture<ThreadConfinedAtomicFile> newInstance(final Path filePath, final long magic,
                                                                          final int maxWaitQueueSize) {

        // Bounded queue executor with abort policy.
        final var queue = new ArrayBlockingQueue<Runnable>(maxWaitQueueSize);
        final var executor = new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue);

        return CompletableFuture.supplyAsync(() -> {
            try {
                final var file = AtomicFile.newInstance(filePath, magic);
                final var sizeMethodSupplier = sizeMethodSupplier(file);
                final var closeMethodRunnable = closeMethodRunnable(file);

                return new ThreadConfinedAtomicFile(executor, file, sizeMethodSupplier, closeMethodRunnable);
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

    public CompletableFuture<MemorySegment> read(final Arena arena, final long position, final long size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return file.read(arena, position, size);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, executor);
    }

    public CompletableFuture<Void> read(final MemorySegment segment, final long position) {
        return CompletableFuture.runAsync(() -> {
            try {
                file.read(segment, position);
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
