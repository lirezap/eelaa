package software.openx.eelaa.ledger;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alireza Pourtaghi
 */
public class ProcessorTest {

    @Test
    public void testIdExistence() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                "",
                "IRR",
                0,
                0,
                "");

        var t2 = new Transaction(
                1,
                1, 1,
                1, 2,
                "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789123456789",
                "IRR",
                0,
                0,
                "");

        processor.process(t1, t2);
        assertTrue(t1.is_failed());
        assertEquals("id.not_valid", t1.get_failReason());

        assertTrue(t2.is_failed());
        assertEquals("id.not_valid", t2.get_failReason());
    }

    @Test
    public void testLedger() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                0,
                1, 1,
                1, 2,
                "ts:id1",
                "IRR",
                0,
                0,
                "");

        var t2 = new Transaction(
                -1,
                1, 1,
                1, 2,
                "ts:id2",
                "IRR",
                0,
                0,
                "");

        processor.process(t1, t2);
        assertTrue(t1.is_failed());
        assertEquals("ledger.not_valid", t1.get_failReason());

        assertTrue(t2.is_failed());
        assertEquals("ledger.not_valid", t2.get_failReason());
    }

    @Test
    public void testSourceAccount() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                0, 1,
                1, 2,
                "ts:id1",
                "IRR",
                0,
                0,
                "");

        var t2 = new Transaction(
                1,
                -1, 1,
                1, 2,
                "ts:id2",
                "IRR",
                0,
                0,
                "");

        processor.process(t1, t2);
        assertTrue(t1.is_failed());
        assertEquals("sourceAccount.not_valid", t1.get_failReason());

        assertTrue(t2.is_failed());
        assertEquals("sourceAccount.not_valid", t2.get_failReason());
    }

    @Test
    public void testSourceWallet() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 0,
                1, 2,
                "ts:id1",
                "IRR",
                0,
                0,
                "");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("sourceWallet.not_valid", t1.get_failReason());
    }

    @Test
    public void testDestinationAccount() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                0, 2,
                "ts:id1",
                "IRR",
                0,
                0,
                "");

        var t2 = new Transaction(
                1,
                1, 1,
                -1, 2,
                "ts:id2",
                "IRR",
                0,
                0,
                "");

        processor.process(t1, t2);
        assertTrue(t1.is_failed());
        assertEquals("destinationAccount.not_valid", t1.get_failReason());

        assertTrue(t2.is_failed());
        assertEquals("destinationAccount.not_valid", t2.get_failReason());
    }

    @Test
    public void testDestinationWallet() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 0,
                "ts:id1",
                "IRR",
                0,
                0,
                "");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("destinationWallet.not_valid", t1.get_failReason());
    }

    @Test
    public void testTransaction() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 1,
                "ts:id1",
                "IRR",
                0,
                0,
                "");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("transaction.not_valid", t1.get_failReason());
    }

    @Test
    public void testId() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                ":",
                "IRR",
                0,
                0,
                "");

        var t2 = new Transaction(
                1,
                1, 1,
                1, 2,
                "abc:",
                "IRR",
                0,
                0,
                "");

        var t3 = new Transaction(
                1,
                1, 1,
                1, 2,
                "1:",
                "IRR",
                0,
                0,
                "");

        var t4 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%s:", System.currentTimeMillis() + 1000),
                "IRR",
                0,
                0,
                "");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("id.not_valid", t1.get_failReason());

        processor.process(t2);
        assertTrue(t2.is_failed());
        assertEquals("id.ts_part_not_valid", t2.get_failReason());

        processor.process(t3);
        assertTrue(t3.is_failed());
        assertEquals("id.ts_part_not_valid", t3.get_failReason());

        processor.process(t4);
        assertTrue(t4.is_failed());
        assertEquals("id.ts_part_not_valid", t4.get_failReason());
    }

    @Test
    public void testCurrency() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:", System.currentTimeMillis()),
                "",
                0,
                0,
                "");

        var t2 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:", System.currentTimeMillis()),
                "012345678901234567890123456789123",
                0,
                0,
                "");

        processor.process(t1, t2);
        assertTrue(t1.is_failed());
        assertEquals("currency.not_valid", t1.get_failReason());

        assertTrue(t2.is_failed());
        assertEquals("currency.not_valid", t2.get_failReason());
    }

    @Test
    public void testAmount() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:", System.currentTimeMillis()),
                "IRR",
                -1,
                0,
                "");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("amount.not_valid", t1.get_failReason());
    }

    @Test
    public void testMaxOverdraftAmount() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:", System.currentTimeMillis()),
                "IRR",
                0,
                -1,
                "");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("maxOverdraftAmount.not_valid", t1.get_failReason());
    }

    @Test
    public void testMetadata() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:", System.currentTimeMillis()),
                "IRR",
                0,
                0,
                "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678912345678"
                        + "1");

        processor.process(t1);
        assertTrue(t1.is_failed());
        assertEquals("metadata.length_exceeded", t1.get_failReason());
    }

    @Test
    public void testTransactionNotAllowed() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        processor.process(t1);

        var t2 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:2", System.currentTimeMillis()),
                "USD",
                1,
                10,
                "");
        processor.process(t2);

        var t3 = new Transaction(
                1,
                1, 1,
                1, 3,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");

        var t4 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:4", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");

        processor.process(t3, t4);
        assertTrue(t3.is_failed());
        assertEquals("transaction.not_allowed", t3.get_failReason());

        assertTrue(t4.is_failed());
        assertEquals("transaction.not_allowed", t4.get_failReason());
    }

    @Test
    public void testBalanceNotEnough() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        processor.process(t1);

        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                2,
                0,
                "");

        var t3 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                3,
                1,
                "");

        processor.process(t2, t3);
        assertTrue(t2.is_failed());
        assertEquals("balance.not_enough", t2.get_failReason());

        assertTrue(t3.is_failed());
        assertEquals("balance.not_enough", t3.get_failReason());
    }

    @Test
    public void testTransactionAlreadyExists() {
        var ts = System.currentTimeMillis();
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", ts),
                "IRR",
                1,
                10,
                "");
        processor.process(t1);

        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:1", ts),
                "IRR",
                1,
                10,
                "");

        processor.process(t2);
        assertTrue(t2.is_failed());
        assertEquals("transaction.already_exists", t2.get_failReason());
    }

    @Test
    public void testSingleItemBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        processor.process(t1);
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        assertEquals(-1, w1.getBalance());
        assertEquals(1, w2.getBalance());
    }

    @Test
    public void testMultipleItemsBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        processor.process(t1, t2, t3);
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        var w4 = processor.fetchWallet(1, 1, 4).get();
        assertEquals(-1, w1.getBalance());
        assertEquals(0, w2.getBalance());
        assertEquals(0, w3.getBalance());
        assertEquals(1, w4.getBalance());
    }

    @Test
    public void testSingleFailedInBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                0,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                1,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                1,
                "");
        processor.process(t1, t2, t3);
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        var w4 = processor.fetchWallet(1, 1, 4).get();
        assertEquals(0, w1.getBalance());
        assertEquals(-1, w2.getBalance());
        assertEquals(0, w3.getBalance());
        assertEquals(1, w4.getBalance());
    }

    @Test
    public void testMultipleFailedInBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                0,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                0,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                1,
                "");
        processor.process(t1, t2, t3);
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        var w4 = processor.fetchWallet(1, 1, 4).get();
        assertEquals(0, w1.getBalance());
        assertEquals(0, w2.getBalance());
        assertEquals(-1, w3.getBalance());
        assertEquals(1, w4.getBalance());
    }

    @Test
    public void testSingleItemAtomicBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        assertTrue(processor.processAtomically(t1));
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        assertEquals(-1, w1.getBalance());
        assertEquals(1, w2.getBalance());
    }

    @Test
    public void testMultipleItemsAtomicBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        assertTrue(processor.processAtomically(t1, t2, t3));
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        var w4 = processor.fetchWallet(1, 1, 4).get();
        assertEquals(-1, w1.getBalance());
        assertEquals(0, w2.getBalance());
        assertEquals(0, w3.getBalance());
        assertEquals(1, w4.getBalance());
    }

    @Test
    public void testSingleFailedInAtomicBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                0,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                1,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                1,
                "");
        assertFalse(processor.processAtomically(t1, t2, t3));
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isEmpty());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        assertEquals(0, w1.getBalance());
        assertEquals(0, w2.getBalance());
        assertEquals(0, w3.getBalance());
    }

    @Test
    public void testMultipleFailedInAtomicBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                0,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                0,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                1,
                "");
        assertFalse(processor.processAtomically(t1, t2, t3));
        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isEmpty());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isEmpty());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        assertEquals(0, w1.getBalance());
        assertEquals(0, w2.getBalance());
        assertEquals(0, w3.getBalance());
    }

    @Test
    public void testConcurrency() throws Exception {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

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
            processor.process(t1);
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            assertTrue(processor.processAtomically(t2));
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            processor.process(t3, t4);
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            assertTrue(processor.processAtomically(t5, t6));
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            processor.process(t7, t8, t9);
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            processor.process(t10, t11, t12, t13);
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            assertFalse(processor.processAtomically(t14, t15));
            latch.countDown();
        });
        ForkJoinPool.commonPool().submit(() -> {
            processor.process(t16, t17);
            latch.countDown();
        });
        latch.await();

        assertTrue(processor.fetchTransaction(t1.getLedger(), t1.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t2.getLedger(), t2.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t3.getLedger(), t3.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t4.getLedger(), t4.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t5.getLedger(), t5.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t6.getLedger(), t6.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t7.getLedger(), t7.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t8.getLedger(), t8.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t9.getLedger(), t9.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t10.getLedger(), t10.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t11.getLedger(), t11.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t12.getLedger(), t12.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t13.getLedger(), t13.getId()).isPresent());
        assertTrue(processor.fetchTransaction(t17.getLedger(), t17.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 2, 1).get();
        assertEquals(-14, w1.getBalance());
        assertEquals(14, w2.getBalance());
    }

    @Test
    public void test10MAccountsScale() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(10_000_000)
                .initialWalletsPerAccountCap(5)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        for (int i = 1; i <= 10_000_000; i++) {
            var t = new Transaction(
                    1,
                    i, 1,
                    i, 2,
                    String.format("%d:%d-%d", System.currentTimeMillis(), 1, i),
                    "IRR",
                    1,
                    100,
                    "");

            processor.process(t);
            assertTrue(processor.fetchTransaction(t.getLedger(), t.getId()).isPresent());
        }

        for (int i = 1; i <= 10_000_000; i++) {
            var t = new Transaction(
                    1,
                    i, 3,
                    i, 4,
                    String.format("%d:%d-%d", System.currentTimeMillis(), 2, i),
                    "IRR",
                    1,
                    100,
                    "");

            processor.process(t);
            assertTrue(processor.fetchTransaction(t.getLedger(), t.getId()).isPresent());
        }
    }

    @Test
    public void testFetchAccount() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                2, 1,
                String.format("%d:t1", System.currentTimeMillis()),
                "IRR",
                1,
                100,
                "");

        processor.process(t1);

        var source = processor.fetchAccount(t1.getLedger(), t1.getSourceAccount());
        var destination = processor.fetchAccount(t1.getLedger(), t1.getDestinationAccount());
        assertTrue(source.isPresent());
        assertTrue(destination.isPresent());
        assertEquals(1, source.get().size());
        assertEquals(1, destination.get().size());

        var sourceWallet = source.get().stream().findAny().get();
        var destinationWallet = destination.get().stream().findAny().get();

        assertEquals(1, sourceWallet.getLedger());
        assertEquals(1, sourceWallet.getAccount());
        assertEquals(1, sourceWallet.getWallet());
        assertEquals("IRR", sourceWallet.getCurrency());
        assertEquals(-1, sourceWallet.getBalance());

        assertEquals(1, destinationWallet.getLedger());
        assertEquals(2, destinationWallet.getAccount());
        assertEquals(1, destinationWallet.getWallet());
        assertEquals("IRR", destinationWallet.getCurrency());
        assertEquals(1, destinationWallet.getBalance());
    }

    @Test
    public void testFetchWallet() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                2, 1,
                String.format("%d:t1", System.currentTimeMillis()),
                "IRR",
                1,
                100,
                "");

        processor.process(t1);

        var sourceWallet = processor.fetchWallet(t1.getLedger(), t1.getSourceAccount(), t1.getSourceWallet());
        var destinationWallet = processor.fetchWallet(t1.getLedger(), t1.getDestinationAccount(), t1.getDestinationWallet());
        assertTrue(sourceWallet.isPresent());
        assertTrue(destinationWallet.isPresent());

        assertEquals(1, sourceWallet.get().getLedger());
        assertEquals(1, sourceWallet.get().getAccount());
        assertEquals(1, sourceWallet.get().getWallet());
        assertEquals("IRR", sourceWallet.get().getCurrency());
        assertEquals(-1, sourceWallet.get().getBalance());
        assertEquals(0, sourceWallet.get().get_thisTurnAccumulatedOverdraft());

        assertEquals(1, destinationWallet.get().getLedger());
        assertEquals(2, destinationWallet.get().getAccount());
        assertEquals(1, destinationWallet.get().getWallet());
        assertEquals("IRR", destinationWallet.get().getCurrency());
        assertEquals(0, destinationWallet.get().get_thisTurnAccumulatedOverdraft());
    }

    @Test
    public void testFetchTransactionWithBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                2, 1,
                String.format("%d:t1", System.currentTimeMillis()),
                "IRR",
                1,
                100,
                "");

        processor.process(t1);

        var transaction = processor.fetchTransaction(t1.getLedger(), t1.getId());
        assertTrue(transaction.isPresent());

        assertEquals(1, transaction.get().getLedger());
        assertEquals(1, transaction.get().getSourceAccount());
        assertEquals(1, transaction.get().getSourceWallet());
        assertEquals(2, transaction.get().getDestinationAccount());
        assertEquals(1, transaction.get().getDestinationWallet());
        assertEquals(t1.getId(), transaction.get().getId());
        assertEquals("IRR", transaction.get().getCurrency());
        assertEquals(1, transaction.get().getAmount());
        assertEquals(100, transaction.get().getMaxOverdraftAmount());
        assertEquals(-1, transaction.get().getSourceWalletNewBalance());
        assertEquals(1, transaction.get().getDestinationWalletNewBalance());
        assertTrue(System.currentTimeMillis() >= transaction.get().getTs());
        assertFalse(transaction.get().is_failed());
        assertNull(transaction.get().get_failReason());
        assertNull(transaction.get().get_sourceWallet());
        assertNull(transaction.get().get_destinationWallet());
    }

    @Test
    public void testFetchTransactionWithAtomicBatch() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                2, 1,
                String.format("%d:t1", System.currentTimeMillis()),
                "IRR",
                1,
                100,
                "");

        processor.processAtomically(t1);

        var transaction = processor.fetchTransaction(t1.getLedger(), t1.getId());
        assertTrue(transaction.isPresent());

        assertEquals(1, transaction.get().getLedger());
        assertEquals(1, transaction.get().getSourceAccount());
        assertEquals(1, transaction.get().getSourceWallet());
        assertEquals(2, transaction.get().getDestinationAccount());
        assertEquals(1, transaction.get().getDestinationWallet());
        assertEquals(t1.getId(), transaction.get().getId());
        assertEquals("IRR", transaction.get().getCurrency());
        assertEquals(1, transaction.get().getAmount());
        assertEquals(100, transaction.get().getMaxOverdraftAmount());
        assertEquals(-1, transaction.get().getSourceWalletNewBalance());
        assertEquals(1, transaction.get().getDestinationWalletNewBalance());
        assertTrue(System.currentTimeMillis() >= transaction.get().getTs());
        assertFalse(transaction.get().is_failed());
        assertNull(transaction.get().get_failReason());
        assertNotNull(transaction.get().get_sourceWallet());
        assertNotNull(transaction.get().get_destinationWallet());
    }

    @Test
    public void testReverseBalancesOfSucceededTransactions() {
        var processor = Processor.newInstance(new LedgerConfig.Builder()
                .initialAccountsCap(1)
                .initialWalletsPerAccountCap(1)
                .succeededTransactionsCacheTTLSeconds(10)
                .build());

        var t1 = new Transaction(
                1,
                1, 1,
                1, 2,
                String.format("%d:1", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        var t2 = new Transaction(
                1,
                1, 2,
                1, 3,
                String.format("%d:2", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        var t3 = new Transaction(
                1,
                1, 3,
                1, 4,
                String.format("%d:3", System.currentTimeMillis()),
                "IRR",
                1,
                10,
                "");
        assertTrue(processor.processAtomically(t1, t2, t3));
        processor.reverseBalancesOfSucceededTransactions(t1, t2, t3);

        assertFalse(processor.fetchTransaction(t1.getLedger(), t1.getId()).isPresent());
        assertFalse(processor.fetchTransaction(t2.getLedger(), t2.getId()).isPresent());
        assertFalse(processor.fetchTransaction(t3.getLedger(), t3.getId()).isPresent());

        var w1 = processor.fetchWallet(1, 1, 1).get();
        var w2 = processor.fetchWallet(1, 1, 2).get();
        var w3 = processor.fetchWallet(1, 1, 3).get();
        var w4 = processor.fetchWallet(1, 1, 4).get();
        assertEquals(0, w1.getBalance());
        assertEquals(0, w2.getBalance());
        assertEquals(0, w3.getBalance());
        assertEquals(0, w4.getBalance());
    }
}
