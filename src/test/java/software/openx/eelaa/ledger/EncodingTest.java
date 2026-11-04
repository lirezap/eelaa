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
package software.openx.eelaa.ledger;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alireza Pourtaghi
 */
public class EncodingTest {

    @Test
    public void testTransaction1() {
        var transaction = new Transaction(1, 2, 3, 4, 5, String.format("%s:%s", System.currentTimeMillis(), 1), "IRR", 6, 7, "\"{\"a\":\"b\"}\"");
        var encoded = transaction.serialize(ByteBufAllocator.DEFAULT);
        var decoded = Transaction.DESERIALIZER.deserialize(encoded);

        assertEquals(transaction.getLedger(), decoded.getLedger());
        assertEquals(transaction.getSourceAccount(), decoded.getSourceAccount());
        assertEquals(transaction.getSourceWallet(), decoded.getSourceWallet());
        assertEquals(transaction.getDestinationAccount(), decoded.getDestinationAccount());
        assertEquals(transaction.getDestinationWallet(), decoded.getDestinationWallet());
        assertEquals(transaction.getId(), decoded.getId());
        assertEquals(transaction.getCurrency(), decoded.getCurrency());
        assertEquals(transaction.getAmount(), decoded.getAmount());
        assertEquals(transaction.getMaxOverdraftAmount(), decoded.getMaxOverdraftAmount());
        assertEquals(transaction.getMetadata(), decoded.getMetadata());
        assertEquals(transaction.getSourceWalletNewBalance(), decoded.getSourceWalletNewBalance());
        assertEquals(transaction.getDestinationWalletNewBalance(), decoded.getDestinationWalletNewBalance());
        assertEquals(transaction.getTs(), decoded.getTs());
        ReferenceCountUtil.release(encoded);
    }

    @Test
    public void testTransaction2() {
        var transaction = new Transaction(1, 2, 3, 4, 5, null, null, 6, 7, null);
        var encoded = transaction.serialize(ByteBufAllocator.DEFAULT);
        var decoded = Transaction.DESERIALIZER.deserialize(encoded);

        assertEquals(transaction.getLedger(), decoded.getLedger());
        assertEquals(transaction.getSourceAccount(), decoded.getSourceAccount());
        assertEquals(transaction.getSourceWallet(), decoded.getSourceWallet());
        assertEquals(transaction.getDestinationAccount(), decoded.getDestinationAccount());
        assertEquals(transaction.getDestinationWallet(), decoded.getDestinationWallet());
        assertEquals("", decoded.getId());
        assertEquals("", decoded.getCurrency());
        assertEquals(transaction.getAmount(), decoded.getAmount());
        assertEquals(transaction.getMaxOverdraftAmount(), decoded.getMaxOverdraftAmount());
        assertEquals("", decoded.getMetadata());
        assertEquals(transaction.getSourceWalletNewBalance(), decoded.getSourceWalletNewBalance());
        assertEquals(transaction.getDestinationWalletNewBalance(), decoded.getDestinationWalletNewBalance());
        assertEquals(transaction.getTs(), decoded.getTs());
        ReferenceCountUtil.release(encoded);
    }

    @Test
    public void testTransaction3() {
        var transaction = new Transaction(1, 2, 3, 4, 5, String.format("%s:%s", System.currentTimeMillis(), 1), "IRR", 6, 7, "\"{\"a\":\"b\"}\"");
        try (var arena = Arena.ofConfined()) {
            var encoded = transaction.serialize(arena);
            var decoded = Transaction.DESERIALIZER.deserialize(encoded);

            assertEquals(transaction.getLedger(), decoded.getLedger());
            assertEquals(transaction.getSourceAccount(), decoded.getSourceAccount());
            assertEquals(transaction.getSourceWallet(), decoded.getSourceWallet());
            assertEquals(transaction.getDestinationAccount(), decoded.getDestinationAccount());
            assertEquals(transaction.getDestinationWallet(), decoded.getDestinationWallet());
            assertEquals(transaction.getId(), decoded.getId());
            assertEquals(transaction.getCurrency(), decoded.getCurrency());
            assertEquals(transaction.getAmount(), decoded.getAmount());
            assertEquals(transaction.getMaxOverdraftAmount(), decoded.getMaxOverdraftAmount());
            assertEquals(transaction.getMetadata(), decoded.getMetadata());
            assertEquals(transaction.getSourceWalletNewBalance(), decoded.getSourceWalletNewBalance());
            assertEquals(transaction.getDestinationWalletNewBalance(), decoded.getDestinationWalletNewBalance());
            assertEquals(transaction.getTs(), decoded.getTs());
        }
    }

    @Test
    public void testTransaction4() {
        var transaction = new Transaction(1, 2, 3, 4, 5, null, null, 6, 7, null);
        try (var arena = Arena.ofConfined()) {
            var encoded = transaction.serialize(arena);
            var decoded = Transaction.DESERIALIZER.deserialize(encoded);

            assertEquals(transaction.getLedger(), decoded.getLedger());
            assertEquals(transaction.getSourceAccount(), decoded.getSourceAccount());
            assertEquals(transaction.getSourceWallet(), decoded.getSourceWallet());
            assertEquals(transaction.getDestinationAccount(), decoded.getDestinationAccount());
            assertEquals(transaction.getDestinationWallet(), decoded.getDestinationWallet());
            assertEquals("", decoded.getId());
            assertEquals("", decoded.getCurrency());
            assertEquals(transaction.getAmount(), decoded.getAmount());
            assertEquals(transaction.getMaxOverdraftAmount(), decoded.getMaxOverdraftAmount());
            assertEquals("", decoded.getMetadata());
            assertEquals(transaction.getSourceWalletNewBalance(), decoded.getSourceWalletNewBalance());
            assertEquals(transaction.getDestinationWalletNewBalance(), decoded.getDestinationWalletNewBalance());
            assertEquals(transaction.getTs(), decoded.getTs());
        }
    }

    @Test
    public void testWallet1() {
        var wallet = new Wallet(1, 2, 3, "IRR", 4);
        var encoded = wallet.serialize(ByteBufAllocator.DEFAULT);
        var decoded = Wallet.DESERIALIZER.deserialize(encoded);

        assertEquals(wallet.getLedger(), decoded.getLedger());
        assertEquals(wallet.getAccount(), decoded.getAccount());
        assertEquals(wallet.getWallet(), decoded.getWallet());
        assertEquals(wallet.getCurrency(), decoded.getCurrency());
        assertEquals(wallet.getBalance(), decoded.getBalance());
        ReferenceCountUtil.release(encoded);
    }

    @Test
    public void testWallet2() {
        var wallet = new Wallet(1, 2, 3, null, 4);
        var encoded = wallet.serialize(ByteBufAllocator.DEFAULT);
        var decoded = Wallet.DESERIALIZER.deserialize(encoded);

        assertEquals(wallet.getLedger(), decoded.getLedger());
        assertEquals(wallet.getAccount(), decoded.getAccount());
        assertEquals(wallet.getWallet(), decoded.getWallet());
        assertEquals("", decoded.getCurrency());
        assertEquals(wallet.getBalance(), decoded.getBalance());
        ReferenceCountUtil.release(encoded);
    }

    @Test
    public void testWallet3() {
        var wallet = new Wallet(1, 2, 3, "IRR", 4);
        try (var arena = Arena.ofConfined()) {
            var encoded = wallet.serialize(arena);
            var decoded = Wallet.DESERIALIZER.deserialize(encoded);

            assertEquals(wallet.getLedger(), decoded.getLedger());
            assertEquals(wallet.getAccount(), decoded.getAccount());
            assertEquals(wallet.getWallet(), decoded.getWallet());
            assertEquals(wallet.getCurrency(), decoded.getCurrency());
            assertEquals(wallet.getBalance(), decoded.getBalance());
        }
    }

    @Test
    public void testWallet4() {
        var wallet = new Wallet(1, 2, 3, null, 4);
        try (var arena = Arena.ofConfined()) {
            var encoded = wallet.serialize(arena);
            var decoded = Wallet.DESERIALIZER.deserialize(encoded);

            assertEquals(wallet.getLedger(), decoded.getLedger());
            assertEquals(wallet.getAccount(), decoded.getAccount());
            assertEquals(wallet.getWallet(), decoded.getWallet());
            assertEquals("", decoded.getCurrency());
            assertEquals(wallet.getBalance(), decoded.getBalance());
        }
    }

    @Test
    public void testFailedTransaction1() {
        var failedTransaction = new FailedTransaction(String.format("%s:%s", System.currentTimeMillis(), 1), "id.not_valid");
        var encoded = failedTransaction.serialize(ByteBufAllocator.DEFAULT);
        var decoded = FailedTransaction.DESERIALIZER.deserialize(encoded);

        assertEquals(failedTransaction.getId(), decoded.getId());
        assertEquals(failedTransaction.getReason(), decoded.getReason());
        ReferenceCountUtil.release(encoded);
    }

    @Test
    public void testFailedTransaction2() {
        var failedTransaction = new FailedTransaction(null, null);
        var encoded = failedTransaction.serialize(ByteBufAllocator.DEFAULT);
        var decoded = FailedTransaction.DESERIALIZER.deserialize(encoded);

        assertEquals("", decoded.getId());
        assertEquals("", decoded.getReason());
        ReferenceCountUtil.release(encoded);
    }
}
