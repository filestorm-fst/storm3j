/*
 * Copyright 2019 Web3 Labs LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.storm3j.protocol.admin;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.storm3j.protocol.admin.methods.response.PersonalListAccounts;
import org.storm3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;

/** JSON-RPC Request object building factory for common Parity and Geth. */
public interface Admin extends Storm3j {

    static Admin build(Storm3jService storm3jService) {
        return new JsonRpc2_0Admin(storm3jService);
    }

    static Admin build(
            Storm3jService storm3jService,
            long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Admin(storm3jService, pollingInterval, scheduledExecutorService);
    }

    public Request<?, PersonalListAccounts> personalListAccounts();

    public Request<?, NewAccountIdentifier> personalNewAccount(String password);

    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase, BigInteger duration);

    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase);

    public Request<?, FstSendTransaction> personalSendTransaction(
            Transaction transaction, String password);
}
