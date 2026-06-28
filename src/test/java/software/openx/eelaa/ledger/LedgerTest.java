package software.openx.eelaa.ledger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.openx.eelaa.lz4.LZ4;
import software.openx.eelaa.lz4.LZ4Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Alireza Pourtaghi
 */
public class LedgerTest {

    @Test
    public void testConcurrency() throws Exception {
        var tempDirectory = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        try (var lz4 = LZ4.newInstance(new LZ4Config.Builder(Path.of(System.getenv("NATIVE_LIBRARIES_LZ4_PATH"))).build());
             var ledger = Ledger.newFastInstance(new LedgerConfig.Builder().dataDirectoryPath(tempDirectory).build(), lz4)) {

            var t1 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:1", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t2 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:2", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t3 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:3", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t4 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:4", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t5 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:5", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t6 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:6", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t7 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:7", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t8 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:8", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t9 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:9", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t10 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:10", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t11 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:11", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t12 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:12", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t13 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:13", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t14 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:14", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");
            var t15 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:15", System.currentTimeMillis()),
                    "IRR",
                    1,
                    0,
                    "");
            var t16 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:16", System.currentTimeMillis()),
                    "IRR",
                    1,
                    0,
                    "");
            var t17 = new Transaction(
                    1,
                    1, 1,
                    2, 1,
                    String.format("%d:17", System.currentTimeMillis()),
                    "IRR",
                    1,
                    100,
                    "");

            var latch = new CountDownLatch(8);
            ForkJoinPool.commonPool().submit(() -> {
                ledger.process(t1).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.processAtomically(t2).thenAccept(Assertions::assertTrue).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.process(t3, t4).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.processAtomically(t5, t6).thenAccept(Assertions::assertTrue).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.process(t7, t8, t9).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.process(t10, t11, t12, t13).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.processAtomically(t14, t15).thenAccept(Assertions::assertTrue).thenAccept(_ -> latch.countDown());
            });
            ForkJoinPool.commonPool().submit(() -> {
                ledger.process(t16, t17).thenAccept(_ -> latch.countDown());
            });
            latch.await();

            assertNotNull(ledger.fetchTransaction(t1.getLedger(), t1.getId()).get());
            assertNotNull(ledger.fetchTransaction(t2.getLedger(), t2.getId()).get());
            assertNotNull(ledger.fetchTransaction(t3.getLedger(), t3.getId()).get());
            assertNotNull(ledger.fetchTransaction(t4.getLedger(), t4.getId()).get());
            assertNotNull(ledger.fetchTransaction(t5.getLedger(), t5.getId()).get());
            assertNotNull(ledger.fetchTransaction(t6.getLedger(), t6.getId()).get());
            assertNotNull(ledger.fetchTransaction(t7.getLedger(), t7.getId()).get());
            assertNotNull(ledger.fetchTransaction(t8.getLedger(), t8.getId()).get());
            assertNotNull(ledger.fetchTransaction(t9.getLedger(), t9.getId()).get());
            assertNotNull(ledger.fetchTransaction(t10.getLedger(), t10.getId()).get());
            assertNotNull(ledger.fetchTransaction(t11.getLedger(), t11.getId()).get());
            assertNotNull(ledger.fetchTransaction(t12.getLedger(), t12.getId()).get());
            assertNotNull(ledger.fetchTransaction(t13.getLedger(), t13.getId()).get());
            assertNotNull(ledger.fetchTransaction(t17.getLedger(), t17.getId()).get());

            var w1 = ledger.fetchWallet(1, 1, 1).get();
            var w2 = ledger.fetchWallet(1, 2, 1).get();
            assertEquals(-14, w1.getBalance());
            assertEquals(14, w2.getBalance());
        }
    }
}
