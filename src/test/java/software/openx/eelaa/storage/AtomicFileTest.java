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

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.concurrent.Executors;

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
        var sleepTime = random.nextInt(1000, 2000 + 1);

        try (var executor = Executors.newSingleThreadExecutor();
             var arena = Arena.ofShared()) {

            var file = executor.submit(() -> AtomicFile.newInstance(path, 0x45454C414157414CL)).get();
            var segment = arena.allocate(bufferSize);
            for (int i = 1; i <= 50000; i++) {
                executor.submit(() -> {
                    try {
                        file.append(segment.asByteBuffer());
                    } catch (Exception _) {
                    }
                });
            }

            Thread.sleep(sleepTime);
            executor.shutdownNow();
        }

        try (var file = AtomicFile.newInstance(path, 0x45454C414157414CL)) {
            assertEquals(0, (file.size() - 256) % bufferSize);
        }
    }
}
