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
package software.openx.eelaa.net.http;

/**
 * @author Alireza Pourtaghi
 */
public final class Transaction {
    private int ledger;
    private long sourceAccount;
    private int sourceWallet;
    private long destinationAccount;
    private int destinationWallet;
    private String id;
    private String currency;
    private long amount;
    private long maxOverdraftAmount;
    private String metadata;
    private long sourceWalletNewBalance;
    private long destinationWalletNewBalance;
    private long ts;

    public Transaction() {
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
                '}';
    }
}
