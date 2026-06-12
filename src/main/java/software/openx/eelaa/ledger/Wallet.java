package software.openx.eelaa.ledger;

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
