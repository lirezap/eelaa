package software.openx.eelaa.storage;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alireza Pourtaghi
 */
public class ThreadConfinedAtomicFileTest {
    private static final SecureRandom random = new SecureRandom();

    @Test
    public void testAppend() throws Exception {
        var path = Path.of("/tmp/" + System.currentTimeMillis() + ".test");
        var bufferSize = random.nextInt(512, 1024 + 1);
        var sleepTime = random.nextInt(500, 1000 + 1);
        var succeeded = new AtomicInteger(0);

        try (var pool = Executors.newWorkStealingPool(8);
             var arena = Arena.ofShared();
             var file = ThreadConfinedAtomicFile.newInstance(path).get()) {

            var segment = arena.allocate(bufferSize);
            for (int i = 1; i <= 1000000; i++) {
                pool.submit(() ->
                        file.append(segment.asByteBuffer()).thenAccept(_ -> succeeded.incrementAndGet()));
            }

            Thread.sleep(sleepTime);
            pool.shutdownNow();
        } catch (TimeoutException _) {
        }

        try (var file = ThreadConfinedAtomicFile.newInstance(path).get()) {
            assertEquals(0, (file.size().get() - 256) % bufferSize);
            assertEquals(succeeded.get(), (file.size().get() - 256) / bufferSize);
        }
    }
}
