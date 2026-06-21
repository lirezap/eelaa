package software.openx.eelaa.ledger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.openx.eelaa.ValueLayouts.*;
import static software.openx.eelaa.ledger.LedgerConfig.TRANSACTION_ID_REQUIRED_BACKOFF_MS;

/**
 * @author Alireza Pourtaghi
 */
public final class Transaction {
    private final int ledger;
    private final long sourceAccount;
    private final int sourceWallet;
    private final long destinationAccount;
    private final int destinationWallet;
    private final String id;
    private final String currency;
    private final long amount;
    private final long maxOverdraftAmount;
    private final String metadata;
    private long sourceWalletNewBalance;
    private long destinationWalletNewBalance;
    private long ts;
    private boolean _failed;
    private String _failReason;
    private Wallet _sourceWallet;
    private Wallet _destinationWallet;
    private MemorySegment _memoryPointer;

    Transaction(final int ledger, final String id) {
        this(ledger, 0, 0, 0, 0, id, "", 0, 0, "");
    }

    public Transaction(final int ledger, final long sourceAccount, final int sourceWallet,
                       final long destinationAccount, final int destinationWallet, final String id,
                       final String currency, final long amount, final long maxOverdraftAmount, final String metadata) {

        this.ledger = ledger;
        this.sourceAccount = sourceAccount;
        this.sourceWallet = sourceWallet;
        this.destinationAccount = destinationAccount;
        this.destinationWallet = destinationWallet;
        this.id = id == null ? "" : id;
        this.currency = currency == null ? "" : currency;
        this.amount = amount;
        this.maxOverdraftAmount = maxOverdraftAmount;
        this.metadata = metadata == null ? "" : metadata;
        this.sourceWalletNewBalance = 0;
        this.destinationWalletNewBalance = 0;
        this.ts = 0;
        this._failed = false;
        this._failReason = null;
        this._sourceWallet = null;
        this._destinationWallet = null;
        this._memoryPointer = null;
    }

    private int binarySize() {
        var sum = Math.addExact(71, id.getBytes(UTF_8).length);
        sum = Math.addExact(sum, currency.getBytes(UTF_8).length);
        sum = Math.addExact(sum, metadata.getBytes(UTF_8).length);

        return sum;
    }

    public int frameBinarySize() {
        return Math.addExact(6, binarySize());
    }

    public MemorySegment encodeV1(final Arena arena) {
        final var binarySize = binarySize();
        final var memory = arena.allocate(6 + binarySize);

        var position = putByteLE(memory, 0, (byte) 0b00000001);
        position = putByteLE(memory, position, (byte) 0b00000000);
        position = putIntLE(memory, position, binarySize);
        position = putIntLE(memory, position, getLedger());
        position = putLongLE(memory, position, getSourceAccount());
        position = putIntLE(memory, position, getSourceWallet());
        position = putLongLE(memory, position, getDestinationAccount());
        position = putIntLE(memory, position, getDestinationWallet());
        position = putString(memory, position, getId());
        position = putString(memory, position, getCurrency());
        position = putLongLE(memory, position, getAmount());
        position = putLongLE(memory, position, getMaxOverdraftAmount());
        position = putString(memory, position, getMetadata());
        position = putLongLE(memory, position, getSourceWalletNewBalance());
        position = putLongLE(memory, position, getDestinationWalletNewBalance());
        putLongLE(memory, position, getTs());

        return memory;
    }

    public ByteBuf encodeV1(final ByteBufAllocator allocator) {
        final var binarySize = binarySize();
        final var buffer = allocator.buffer(Math.addExact(6, binarySize));

        buffer.writeByte(0b00000001);
        buffer.writeByte(0b00000000);
        buffer.writeInt(binarySize);
        buffer.writeInt(getLedger());
        buffer.writeLong(getSourceAccount());
        buffer.writeInt(getSourceWallet());
        buffer.writeLong(getDestinationAccount());
        buffer.writeInt(getDestinationWallet());
        buffer.writeCharSequence(getId(), UTF_8);
        buffer.writeByte(0x00);
        buffer.writeCharSequence(getCurrency(), UTF_8);
        buffer.writeByte(0x00);
        buffer.writeLong(getAmount());
        buffer.writeLong(getMaxOverdraftAmount());
        buffer.writeCharSequence(getMetadata(), UTF_8);
        buffer.writeByte(0x00);
        buffer.writeLong(getSourceWalletNewBalance());
        buffer.writeLong(getDestinationWalletNewBalance());
        buffer.writeLong(getTs());

        return buffer;
    }

    public String validate(final long now) {
        // First check id existence.
        if (id.isBlank() || id.length() > 128) {
            return "id.not_valid";
        }

        if (ledger <= 0) {
            return "ledger.not_valid";
        }

        if (sourceAccount <= 0) {
            return "sourceAccount.not_valid";
        }

        if (sourceWallet == 0) {
            return "sourceWallet.not_valid";
        }

        if (destinationAccount <= 0) {
            return "destinationAccount.not_valid";
        }

        if (destinationWallet == 0) {
            return "destinationWallet.not_valid";
        }

        if (sourceAccount == destinationAccount && sourceWallet == destinationWallet) {
            return "transaction.not_valid";
        }

        try {
            final var ts = Long.parseLong(id.split(":")[0]);
            if (ts < now - TRANSACTION_ID_REQUIRED_BACKOFF_MS || ts > now) {
                return "id.ts_part_not_valid";
            }
        } catch (final IndexOutOfBoundsException ex) {
            return "id.not_valid";
        } catch (final NumberFormatException ex) {
            return "id.ts_part_not_valid";
        }

        if (currency.isBlank() || currency.length() > 32) {
            return "currency.not_valid";
        }

        if (amount < 0) {
            return "amount.not_valid";
        }

        if (maxOverdraftAmount < 0) {
            return "maxOverdraftAmount.not_valid";
        }

        if (metadata.length() > 2048) {
            return "metadata.length_exceeded";
        }

        return null;
    }

    public int getLedger() {
        return ledger;
    }

    public long getSourceAccount() {
        return sourceAccount;
    }

    public int getSourceWallet() {
        return sourceWallet;
    }

    public long getDestinationAccount() {
        return destinationAccount;
    }

    public int getDestinationWallet() {
        return destinationWallet;
    }

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public long getAmount() {
        return amount;
    }

    public long getMaxOverdraftAmount() {
        return maxOverdraftAmount;
    }

    public String getMetadata() {
        return metadata;
    }

    public long getSourceWalletNewBalance() {
        return sourceWalletNewBalance;
    }

    public long getDestinationWalletNewBalance() {
        return destinationWalletNewBalance;
    }

    public long getTs() {
        return ts;
    }

    public boolean is_failed() {
        return _failed;
    }

    public String get_failReason() {
        return _failReason;
    }

    public Wallet get_sourceWallet() {
        return _sourceWallet;
    }

    public Wallet get_destinationWallet() {
        return _destinationWallet;
    }

    public MemorySegment get_memoryPointer() {
        return _memoryPointer;
    }

    public void setSourceWalletNewBalance(final long sourceWalletNewBalance) {
        this.sourceWalletNewBalance = sourceWalletNewBalance;
    }

    public void setDestinationWalletNewBalance(final long destinationWalletNewBalance) {
        this.destinationWalletNewBalance = destinationWalletNewBalance;
    }

    public void setTs(final long ts) {
        this.ts = ts;
    }

    public void set_failed(final boolean _failed) {
        this._failed = _failed;
    }

    public void set_failReason(final String _failReason) {
        this._failReason = _failReason;
    }

    public void set_sourceWallet(final Wallet _sourceWallet) {
        this._sourceWallet = _sourceWallet;
    }

    public void set_destinationWallet(final Wallet _destinationWallet) {
        this._destinationWallet = _destinationWallet;
    }

    public void set_memoryPointer(final MemorySegment _memoryPointer) {
        this._memoryPointer = _memoryPointer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction transaction)) return false;

        return getLedger() == transaction.getLedger() && getId().equals(transaction.getId());
    }

    @Override
    public int hashCode() {
        int result = getLedger();
        result = 31 * result + getId().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "ledger=" + ledger +
                ", sourceAccount=" + sourceAccount +
                ", sourceWallet=" + sourceWallet +
                ", destinationAccount=" + destinationAccount +
                ", destinationWallet=" + destinationWallet +
                ", id='" + id + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", maxOverdraftAmount=" + maxOverdraftAmount +
                ", metadata='" + metadata + '\'' +
                ", sourceWalletNewBalance=" + sourceWalletNewBalance +
                ", destinationWalletNewBalance=" + destinationWalletNewBalance +
                ", ts=" + ts +
                ", _failed=" + _failed +
                ", _failReason='" + _failReason + '\'' +
                ", _sourceWallet=" + _sourceWallet +
                ", _destinationWallet=" + _destinationWallet +
                ", _memoryPointer=" + _memoryPointer +
                '}';
    }
}
