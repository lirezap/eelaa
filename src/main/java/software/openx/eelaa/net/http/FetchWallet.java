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
public final class FetchWallet extends Message<FetchWallet> {
    private int ledger;
    private long account;
    private int wallet;

    public FetchWallet() {
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

    @Override
    public String toString() {
        return "FetchWallet{" +
                "ledger=" + ledger +
                ", account=" + account +
                ", wallet=" + wallet +
                '}';
    }
}
