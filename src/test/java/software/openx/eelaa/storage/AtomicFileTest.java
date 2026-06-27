package software.openx.eelaa.storage;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alireza Pourtaghi
 */
public class AtomicFileTest {
    private static final SecureRandom random = new SecureRandom();

    @Test
    public void testAppend() throws Exception {
        var path = Path.of("/tmp/" + System.currentTimeMillis() + ".test");
        var bufferSize = random.nextInt(512, 1024 + 1);
        var sleepTime = random.nextInt(1000, 5000 + 1);
        var succeeded = new AtomicInteger(0);

        try (var executor = Executors.newSingleThreadExecutor();
             var arena = Arena.ofShared()) {

            var file = executor.submit(() -> AtomicFile.newInstance(path)).get();
            var segment = arena.allocate(bufferSize);
            for (int i = 1; i <= 50000; i++) {
                executor.submit(() -> {
                    try {
                        file.append(segment.asByteBuffer());
                        succeeded.incrementAndGet();
                    } catch (Exception _) {
                    }
                });
            }

            Thread.sleep(sleepTime);
            executor.shutdownNow();
        }

        try (var file = AtomicFile.newInstance(path)) {
            assertEquals(0, (file.size() - 256) % bufferSize);
            assertEquals(succeeded.get(), (file.size() - 256) / bufferSize);
        }
    }
}
