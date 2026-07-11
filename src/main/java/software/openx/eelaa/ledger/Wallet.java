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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import software.openx.eelaa.memory.StringUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.openx.eelaa.memory.MemorySegmentUtil.*;

/**
 * @author Alireza Pourtaghi
 */
public final class Wallet {
    private final int ledger;
    private final long account;
    private final int wallet;
    private final String currency;
    private long balance;
    private long _thisTurnAccumulatedOverdraft;

    Wallet(final int ledger, final long account, final int wallet) {
        this(ledger, account, wallet, "", 0);
    }

    public Wallet(final int ledger, final long account, final int wallet, final String currency, final long balance) {
        this.ledger = ledger;
        this.account = account;
        this.wallet = wallet;
        this.currency = currency == null ? "" : currency;
        this.balance = balance;
        this._thisTurnAccumulatedOverdraft = 0;
    }

    private int binarySize() {
        return Math.addExact(24, StringUtil.requiredNullTerminatedUTF8BytesLength(currency));
    }

    public int frameBinarySize() {
        return Math.addExact(6, binarySize());
    }

    public ByteBuf encodeV1(final ByteBufAllocator allocator) {
        final var binarySize = binarySize();
        final var buffer = allocator.buffer(Math.addExact(6, binarySize));

        buffer.writeByte(0b00000001);
        buffer.writeByte(0b00000000);
        buffer.writeInt(binarySize);
        buffer.writeInt(getLedger());
        buffer.writeLong(getAccount());
        buffer.writeInt(getWallet());
        buffer.writeCharSequence(getCurrency(), UTF_8);
        buffer.writeZero(1);
        buffer.writeLong(getBalance());

        return buffer;
    }

    public static Wallet decode(final ByteBuf buf) {
        return new Wallet(
                buf.readInt(),
                buf.readLong(),
                buf.readInt(),
                StringUtil.readNullTerminatedUTF8String(buf),
                buf.readLong());
    }

    public MemorySegment encodeV1(final Arena arena) {
        final var binarySize = binarySize();
        final var memory = arena.allocate(6 + binarySize);

        var position = putByteLE(memory, 0, (byte) 0b00000001);
        position = putByteLE(memory, position, (byte) 0b00000000);
        position = putIntLE(memory, position, binarySize);
        position = putIntLE(memory, position, getLedger());
        position = putLongLE(memory, position, getAccount());
        position = putIntLE(memory, position, getWallet());
        position = putString(memory, position, getCurrency());
        putLongLE(memory, position, getBalance());

        return memory;
    }

    public static Wallet decode(final MemorySegment memory) {
        final var position = new AtomicLong(0);
        return new Wallet(
                getIntLE(memory, position),
                getLongLE(memory, position),
                getIntLE(memory, position),
                getString(memory, position),
                getLongLE(memory, position));
    }

    public int getLedger() {
        return ledger;
    }

    public long getAccount() {
        return account;
    }

    public int getWallet() {
        return wallet;
    }

    public String getCurrency() {
        return currency;
    }

    public long getBalance() {
        return balance;
    }

    public long get_thisTurnAccumulatedOverdraft() {
        return _thisTurnAccumulatedOverdraft;
    }

    public void setBalance(final long balance) {
        this.balance = balance;
    }

    public void set_thisTurnAccumulatedOverdraft(final long _thisTurnAccumulatedOverdraft) {
        this._thisTurnAccumulatedOverdraft = _thisTurnAccumulatedOverdraft;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Wallet other)) return false;

        return getLedger() == other.getLedger() &&
                getAccount() == other.getAccount() &&
                getWallet() == other.getWallet();
    }

    @Override
    public int hashCode() {
        int result = getLedger();
        result = 31 * result + Long.hashCode(getAccount());
        result = 31 * result + getWallet();
        return result;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "ledger=" + ledger +
                ", account=" + account +
                ", wallet=" + wallet +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                ", _thisTurnAccumulatedOverdraft=" + _thisTurnAccumulatedOverdraft +
                '}';
    }
}
