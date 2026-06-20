package software.openx.eelaa.ledger;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static software.openx.eelaa.ValueLayouts.*;

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

    public Wallet(final int ledger, final long account, final int wallet) {
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
        return Math.addExact(29, currency.getBytes(StandardCharsets.UTF_8).length);
    }

    public MemorySegment encodeV1ForStorage(final Arena arena) {
        final var binarySize = binarySize();
        final var memory = arena.allocate(6 + binarySize);

        var position = putByteLE(memory, 0, (byte) 0b00000001);
        position = putByteLE(memory, position, (byte) 0b00000000);
        position = putIntLE(memory, position, binarySize);
        position = putIntLE(memory, position, getLedger());
        position = putLongLE(memory, position, getAccount());
        position = putIntLE(memory, position, getWallet());
        position = putStringLE(memory, position, getCurrency());
        putLongLE(memory, position, getBalance());

        return memory;
    }

    public MemorySegment encodeV1ForNetwork(final Arena arena) {
        final var binarySize = binarySize();
        final var memory = arena.allocate(6 + binarySize);

        var position = putByteBE(memory, 0, (byte) 0b00000001);
        position = putByteBE(memory, position, (byte) 0b00000000);
        position = putIntBE(memory, position, binarySize);
        position = putIntBE(memory, position, getLedger());
        position = putLongBE(memory, position, getAccount());
        position = putIntBE(memory, position, getWallet());
        position = putStringBE(memory, position, getCurrency());
        putLongBE(memory, position, getBalance());

        return memory;
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

        return getLedger() == other.getLedger() && getAccount() == other.getAccount() && getWallet() == other.getWallet();
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
