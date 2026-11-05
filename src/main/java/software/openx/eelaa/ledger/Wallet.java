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
import software.openx.eelaa.protocol.V1BinaryFrameDeserializable;
import software.openx.eelaa.protocol.V1BinaryFrameSerializable;
import software.openx.eelaa.util.StringUtil;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.openx.eelaa.util.ByteBufUtil.readNullTerminatedUTF8String;
import static software.openx.eelaa.util.MemorySegmentUtil.*;

/**
 * @author Alireza Pourtaghi
 */
public final class Wallet implements V1BinaryFrameSerializable, V1BinaryFrameDeserializable<Wallet> {
    public static final Wallet DESERIALIZER = new Wallet(0, 0, 0);

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

    @Override
    public int dataLength() {
        return Math.addExact(24, StringUtil.requiredNullTerminatedUTF8BytesLength(currency));
    }

    @Override
    public void serialize(final MemorySegment memory, long position) {
        position = putIntLE(memory, position, getLedger());
        position = putLongLE(memory, position, getAccount());
        position = putIntLE(memory, position, getWallet());
        position = putString(memory, position, getCurrency());
        putLongLE(memory, position, getBalance());
    }

    @Override
    public void serialize(final ByteBuf buffer) {
        buffer.writeInt(getLedger());
        buffer.writeLong(getAccount());
        buffer.writeInt(getWallet());
        buffer.writeCharSequence(getCurrency(), UTF_8);
        buffer.writeZero(1);
        buffer.writeLong(getBalance());
    }

    @Override
    public Wallet deserialize(final MemorySegment serialized, final AtomicLong position, final byte version,
                              final byte flags, final int dataLength) {

        return new Wallet(
                getIntLE(serialized, position),
                getLongLE(serialized, position),
                getIntLE(serialized, position),
                getString(serialized, position),
                getLongLE(serialized, position));
    }

    @Override
    public Wallet deserialize(final ByteBuf serialized, final byte version, final byte flags, final int dataLength) {
        return new Wallet(
                serialized.readInt(),
                serialized.readLong(),
                serialized.readInt(),
                readNullTerminatedUTF8String(serialized),
                serialized.readLong());
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
