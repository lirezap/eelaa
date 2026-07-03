/*
 * Copyright 2026 lirezap@protonmail.com
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
package software.openx.eelaa.lmdb;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static java.lang.foreign.MemorySegment.NULL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alireza Pourtaghi
 */
public class LMDBManagerTest {

    @Test
    public void testPut() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
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
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
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
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
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
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var txn = manager.newTxn(arena, NULL, 0);

            for (int i = 1; i <= 1000000; i++) {
                var key = arena.allocateFrom("key" + i);
                var value = arena.allocateFrom("value" + i);

                assertTrue(manager.put(txn, dbi, key, value));
            }
            manager.commitTxn(txn);

            txn = manager.newTxn(arena, NULL, 0);
            for (int i = 1; i <= 1000000; i++) {
                var key = arena.allocateFrom("key" + i);
                assertEquals("value" + i, manager.get(txn, dbi, key, arena).getString(0));
            }
            manager.commitTxn(txn);
        }
    }

    @Test
    public void testPutOrReplace() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var key = arena.allocateFrom("key");
            var value = arena.allocateFrom("value");
            var secondValue = arena.allocateFrom("secondValue");

            manager.putOrReplace(dbi, key, value);
            manager.putOrReplace(dbi, key, secondValue);
            assertEquals("secondValue", manager.get(dbi, key, arena).getString(0));
        }
    }

    @Test
    public void testBatchPutOrReplace() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var txn = manager.newTxn(arena, NULL, 0);
            var key = arena.allocateFrom("key");

            for (int i = 1; i <= 1000000; i++) {
                var value = arena.allocateFrom("value" + i);
                manager.putOrReplace(txn, dbi, key, value);
            }
            manager.commitTxn(txn);

            assertEquals("value1000000", manager.get(dbi, key, arena).getString(0));
        }
    }

    @Test
    public void testIterateFromFirst() throws Exception {
        var dataDirectoryPath = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var manager = LMDBManager.newInstance(Path.of(System.getenv("LIBRARIES_NATIVE_LMDB_PATH")), dataDirectoryPath, 1024 * 1024, 1, 0, 0644);
             var arena = Arena.ofConfined()) {

            var dbi = manager.openDb("test");
            var txn = manager.newTxn(arena, NULL, 0);

            var values = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                var key = arena.allocateFrom("key" + i);
                var value = arena.allocateFrom("value" + i);
                manager.put(txn, dbi, key, value);

                values.add("value" + i);
            }
            manager.commitTxn(txn);

            txn = manager.newTxn(arena, NULL, 0);
            var cursor = manager.newCursor(txn, dbi, arena);
            var index = 0;
            var value = manager.iterateFromFirst(cursor, arena);
            while (value != NULL) {
                assertEquals(values.get(index++), value.getString(0));
                value = manager.iterateFromFirst(cursor, arena);
            }

            manager.closeCursor(cursor);
            manager.commitTxn(txn);
        }
    }
}
