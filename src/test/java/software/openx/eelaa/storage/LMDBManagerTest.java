package software.openx.eelaa.storage;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.foreign.MemorySegment.NULL;
import static org.junit.jupiter.api.Assertions.*;
import static software.openx.eelaa.lmdb.LMDBFlags.MDB_NOOVERWRITE;

/**
 * @author Alireza Pourtaghi
 */
public class LMDBManagerTest {

    @Test
    public void testPut() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("NATIVE_LIBRARIES_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var key = arena.allocateFrom("key");
            var value = arena.allocateFrom("value");

            assertTrue(manager.put(dbi, key, value));
            assertEquals("value", manager.get(dbi, key, arena).getString(0));
        }
    }

    @Test
    public void testAlreadyExists() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("NATIVE_LIBRARIES_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var key = arena.allocateFrom("key");
            var value = arena.allocateFrom("value");

            assertTrue(manager.put(dbi, key, value));
            assertFalse(manager.put(dbi, key, value));
            assertEquals("value", manager.get(dbi, key, arena).getString(0));
        }
    }

    @Test
    public void testNotFound() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("NATIVE_LIBRARIES_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var key = arena.allocateFrom("key");
            var secondKey = arena.allocateFrom("secondKey");
            var value = arena.allocateFrom("value");

            assertTrue(manager.put(dbi, key, value));
            assertEquals(NULL, manager.get(dbi, secondKey, arena));
        }
    }

    @Test
    public void testBatchPut() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("NATIVE_LIBRARIES_LMDB_PATH")), dataDirectoryPath, 1024 * 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var txn = manager.newTxn(arena, NULL, MDB_NOOVERWRITE);

            for (int i = 1; i <= 1000000; i++) {
                var key = arena.allocateFrom("key" + i);
                var value = arena.allocateFrom("value" + i);

                assertTrue(manager.put(txn, dbi, key, value));
            }
            manager.commitTxn(txn);

            txn = manager.newTxn(arena, NULL, MDB_NOOVERWRITE);
            for (int i = 1; i <= 1000000; i++) {
                var key = arena.allocateFrom("key" + i);
                assertEquals("value" + i, manager.get(txn, dbi, key, arena).getString(0));
            }
            manager.commitTxn(txn);
        }
    }
}
