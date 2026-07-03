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
package software.openx.eelaa.ledger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Ledger's processor to process incoming batch of transactions.
 *
 * @author Alireza Pourtaghi
 */
final class Processor {
    private final Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<ObjectOpenHashSet<Wallet>>> ledgers;
    private final Cache<Transaction, Transaction> succeededTransactionsCache;
    private final int initialAccountsCap;
    private final int initialWalletsPerAccountCap;

    private Processor(final Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<ObjectOpenHashSet<Wallet>>> ledgers,
                      final Cache<Transaction, Transaction> succeededTransactionsCache,
                      final int initialAccountsCap,
                      final int initialWalletsPerAccountCap) {

        this.ledgers = ledgers;
        this.succeededTransactionsCache = succeededTransactionsCache;
        this.initialAccountsCap = initialAccountsCap;
        this.initialWalletsPerAccountCap = initialWalletsPerAccountCap;
    }

    public static Processor newInstance(final LedgerConfig ledgerConfig) {
        final Cache<Transaction, Transaction> succeededTransactionsCache = Caffeine
                .newBuilder()
                .expireAfterWrite(ledgerConfig.getSucceededTransactionsCacheTTLSeconds(), SECONDS)
                .build();

        return new Processor(
                new Int2ObjectOpenHashMap<>(),
                succeededTransactionsCache,
                ledgerConfig.getInitialAccountsCap(),
                ledgerConfig.getInitialWalletsPerAccountCap());
    }

    public void process(final Transaction... transactions) {
        final var now = System.currentTimeMillis();
        for (final var transaction : transactions) {
            if (transaction != null && isValidTransaction(transaction, now)) {
                final var accounts = getAccounts(transaction.getLedger());
                final var sourceAccountWallets = getWallets(accounts, transaction.getSourceAccount());
                final var destinationAccountWallets = getWallets(accounts, transaction.getDestinationAccount());
                final var sourceWallet = putAndGetSourceAccountWallet(sourceAccountWallets, transaction);
                final var destinationWallet = putAndGetDestinationAccountWallet(destinationAccountWallets, transaction);

                // Good for later use of references.
                transaction.set_sourceWallet(sourceWallet);
                transaction.set_destinationWallet(destinationWallet);
                if (isAllowedTransaction(sourceWallet, destinationWallet, transaction)) {
                    doTransaction(sourceWallet, destinationWallet, transaction, now);
                }
            }
        }
    }

    public boolean processAtomically(final Transaction... transactions) {
        final var now = System.currentTimeMillis();
        for (final var transaction : transactions) {
            if (transaction != null) {
                if (!isValidTransaction(transaction, now)) {
                    return false;
                }

                final var accounts = getAccounts(transaction.getLedger());
                final var sourceAccountWallets = getWallets(accounts, transaction.getSourceAccount());
                final var destinationAccountWallets = getWallets(accounts, transaction.getDestinationAccount());
                final var sourceWallet = putAndGetSourceAccountWallet(sourceAccountWallets, transaction);
                final var destinationWallet = putAndGetDestinationAccountWallet(destinationAccountWallets, transaction);

                if (isAllowedTransaction(sourceWallet, destinationWallet, transaction)) {
                    final var accumulation = sourceWallet.get_thisTurnAccumulatedOverdraft() + transaction.getAmount();
                    if (isSafeToAdd(sourceWallet.get_thisTurnAccumulatedOverdraft(), transaction.getAmount(), accumulation)) {
                        // Update cache here, because we will do the transaction in later time.
                        succeededTransactionsCache.put(transaction, transaction);
                        sourceWallet.set_thisTurnAccumulatedOverdraft(accumulation);
                        transaction.set_sourceWallet(sourceWallet);
                        transaction.set_destinationWallet(destinationWallet);
                    } else {
                        transaction.set_failed(true);
                        transaction.set_failReason("source_wallet_accumulated_overdraft.overflow");

                        resetThisTurnAccumulatedOverdraftValues(transactions);
                        return false;
                    }
                } else {
                    resetThisTurnAccumulatedOverdraftValues(transactions);
                    return false;
                }
            }
        }

        for (final var transaction : transactions) {
            doTransaction(transaction.get_sourceWallet(), transaction.get_destinationWallet(), transaction, now);
        }

        return true;
    }

    public ObjectOpenHashSet<Wallet> fetchAccount(final int ledger, final long account) {
        final var accounts = ledgers.get(ledger);
        if (accounts != null) {
            return accounts.get(account);
        }

        return null;
    }

    public Wallet fetchWallet(final int ledger, final long account, final int wallet) {
        final var wallets = fetchAccount(ledger, account);
        if (wallets != null) {
            final var builtForEqualityCheck = new Wallet(ledger, account, wallet);
            for (final var element : wallets) {
                if (element.equals(builtForEqualityCheck)) {
                    return element;
                }
            }
        }

        return null;
    }

    public Transaction fetchTransaction(final int ledger, final String id) {
        return succeededTransactionsCache.getIfPresent(new Transaction(ledger, id));
    }

    public void reverseBalancesOfSucceededTransactions(final Transaction... transactions) {
        try {
            for (final var transaction : transactions) {
                if (transaction != null && !transaction.is_failed()) {
                    final var accounts = getAccounts(transaction.getLedger());
                    final var sourceAccountWallets = getWallets(accounts, transaction.getSourceAccount());
                    final var destinationAccountWallets = getWallets(accounts, transaction.getDestinationAccount());
                    final var sourceWallet = putAndGetSourceAccountWallet(sourceAccountWallets, transaction);
                    final var destinationWallet = putAndGetDestinationAccountWallet(destinationAccountWallets, transaction);

                    // No need for addition/subtraction safety check for reversal.
                    sourceWallet.setBalance(sourceWallet.getBalance() + transaction.getAmount());
                    destinationWallet.setBalance(destinationWallet.getBalance() - transaction.getAmount());
                    succeededTransactionsCache.invalidate(transaction);
                }
            }
        } catch (final Throwable cause) {
            // Must never reach line of code!
            System.exit(-1);
        }
    }

    public void loadWallets(final List<Wallet> ws) {
        for (final var w : ws) {
            var accounts = ledgers.get(w.getLedger());
            if (accounts == null) {
                ledgers.put(w.getLedger(), new Long2ObjectOpenHashMap<>(initialAccountsCap));
            }
            accounts = ledgers.get(w.getLedger());

            var wallets = accounts.get(w.getAccount());
            if (wallets == null) {
                accounts.put(w.getAccount(), new ObjectOpenHashSet<>(initialWalletsPerAccountCap));
            }
            wallets = accounts.get(w.getAccount());

            wallets.add(new Wallet(w.getLedger(), w.getAccount(), w.getWallet(), w.getCurrency(), w.getBalance()));
        }
    }

    private boolean isValidTransaction(final Transaction transaction, final long now) {
        final var validationError = transaction.validate(now);
        if (validationError != null) {
            transaction.set_failed(true);
            transaction.set_failReason(validationError);
            return false;
        }

        return true;
    }

    private boolean isAllowedTransaction(final Wallet sourceWallet, final Wallet destinationWallet,
                                         final Transaction transaction) {

        if (!sourceWallet.getCurrency().equals(destinationWallet.getCurrency()) ||
                !destinationWallet.getCurrency().equals(transaction.getCurrency())) {

            transaction.set_failed(true);
            transaction.set_failReason("transaction.not_allowed");
            return false;
        }

        final var currentBalance = sourceWallet.getBalance() - sourceWallet.get_thisTurnAccumulatedOverdraft();
        if (!isSafeToSubtract(sourceWallet.getBalance(), sourceWallet.get_thisTurnAccumulatedOverdraft(), currentBalance)) {
            transaction.set_failed(true);
            transaction.set_failReason("source_wallet_current_balance.underflow");
            return false;
        }

        final var finalBalance = currentBalance - transaction.getAmount();
        if (!isSafeToSubtract(currentBalance, transaction.getAmount(), finalBalance)) {
            transaction.set_failed(true);
            transaction.set_failReason("source_wallet_final_balance.underflow");
            return false;
        }

        if (finalBalance < 0 && -finalBalance > transaction.getMaxOverdraftAmount()) {
            transaction.set_failed(true);
            transaction.set_failReason("balance.not_enough");
            return false;
        }

        if (!isSafeToAdd(destinationWallet.getBalance(), transaction.getAmount())) {
            transaction.set_failed(true);
            transaction.set_failReason("destination_wallet_final_balance.overflow");
            return false;
        }

        if (succeededTransactionsCache.getIfPresent(transaction) != null) {
            transaction.set_failed(true);
            transaction.set_failReason("transaction.already_exists");
            return false;
        }

        return true;
    }

    private void doTransaction(final Wallet sourceWallet, final Wallet destinationWallet, final Transaction transaction,
                               final long now) {

        // No need for addition/subtraction safety check here; Already checked before this step.
        succeededTransactionsCache.put(transaction, transaction);
        sourceWallet.set_thisTurnAccumulatedOverdraft(0);
        sourceWallet.setBalance(sourceWallet.getBalance() - transaction.getAmount());
        destinationWallet.setBalance(destinationWallet.getBalance() + transaction.getAmount());

        transaction.setSourceWalletNewBalance(sourceWallet.getBalance());
        transaction.setDestinationWalletNewBalance(destinationWallet.getBalance());
        transaction.setTs(now);
    }

    private void resetThisTurnAccumulatedOverdraftValues(final Transaction... transactions) {
        for (final var transaction : transactions) {
            if (transaction != null && !transaction.is_failed()) {
                final var accounts = getAccounts(transaction.getLedger());
                final var sourceAccountWallets = getWallets(accounts, transaction.getSourceAccount());
                final var sourceWallet = putAndGetSourceAccountWallet(sourceAccountWallets, transaction);

                succeededTransactionsCache.invalidate(transaction);
                sourceWallet.set_thisTurnAccumulatedOverdraft(0);
            }
        }
    }

    private Long2ObjectOpenHashMap<ObjectOpenHashSet<Wallet>> getAccounts(final int ledger) {
        if (ledgers.containsKey(ledger)) {
            return ledgers.get(ledger);
        } else {
            final var accounts = new Long2ObjectOpenHashMap<ObjectOpenHashSet<Wallet>>(initialAccountsCap);
            ledgers.put(ledger, accounts);
            return accounts;
        }
    }

    private ObjectOpenHashSet<Wallet> getWallets(final Long2ObjectOpenHashMap<ObjectOpenHashSet<Wallet>> accounts,
                                                 final long account) {

        if (accounts.containsKey(account)) {
            return accounts.get(account);
        } else {
            final var wallets = new ObjectOpenHashSet<Wallet>(initialWalletsPerAccountCap);
            accounts.put(account, wallets);
            return wallets;
        }
    }

    private Wallet putAndGetSourceAccountWallet(final ObjectOpenHashSet<Wallet> wallets,
                                                final Transaction transaction) {

        final var wallet = sourceWalletOf(transaction);
        for (final var element : wallets) {
            if (element.equals(wallet))
                return element;
        }

        wallets.add(wallet);
        return wallet;
    }

    private Wallet putAndGetDestinationAccountWallet(final ObjectOpenHashSet<Wallet> wallets,
                                                     final Transaction transaction) {

        final var wallet = destinationWalletOf(transaction);
        for (final var element : wallets) {
            if (element.equals(wallet))
                return element;
        }

        wallets.add(wallet);
        return wallet;
    }

    private Wallet sourceWalletOf(final Transaction t) {
        return new Wallet(t.getLedger(), t.getSourceAccount(), t.getSourceWallet(), t.getCurrency(), 0);
    }

    private Wallet destinationWalletOf(final Transaction t) {
        return new Wallet(t.getLedger(), t.getDestinationAccount(), t.getDestinationWallet(), t.getCurrency(), 0);
    }

    private boolean isSafeToAdd(final long first, final long second) {
        final var r = first + second;
        return ((first ^ r) & (second ^ r)) >= 0;
    }

    private boolean isSafeToAdd(final long first, final long second, final long result) {
        return ((first ^ result) & (second ^ result)) >= 0;
    }

    private boolean isSafeToSubtract(final long first, final long second) {
        final var r = first - second;
        return ((first ^ second) & (first ^ r)) >= 0;
    }

    private boolean isSafeToSubtract(final long first, final long second, final long result) {
        return ((first ^ second) & (first ^ result)) >= 0;
    }
}
