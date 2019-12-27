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
package org.storm3j.protocol.geth;

import java.util.Arrays;
import java.util.Collections;

import io.reactivex.Flowable;

import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.admin.JsonRpc2_0Admin;
import org.storm3j.protocol.admin.methods.response.BooleanResponse;
import org.storm3j.protocol.admin.methods.response.PersonalSign;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.FstSubscribe;
import org.storm3j.protocol.core.methods.response.MinerStartResponse;
import org.storm3j.protocol.geth.response.PersonalEcRecover;
import org.storm3j.protocol.geth.response.PersonalImportRawKey;
import org.storm3j.protocol.websocket.events.PendingTransactionNotification;
import org.storm3j.protocol.websocket.events.SyncingNotfication;

/** JSON-RPC 2.0 factory implementation for Geth. */
public class JsonRpc2_0Geth extends JsonRpc2_0Admin implements Geth {

    public JsonRpc2_0Geth(Storm3jService storm3jService) {
        super(storm3jService);
    }

    @Override
    public Request<?, PersonalImportRawKey> personalImportRawKey(String keydata, String password) {
        return new Request<>(
                "personal_importRawKey",
                Arrays.asList(keydata, password),
                storm3jService,
                PersonalImportRawKey.class);
    }

    @Override
    public Request<?, BooleanResponse> personalLockAccount(String accountId) {
        return new Request<>(
                "personal_lockAccount",
                Arrays.asList(accountId),
                storm3jService,
                BooleanResponse.class);
    }

    @Override
    public Request<?, PersonalSign> personalSign(
            String message, String accountId, String password) {
        return new Request<>(
                "personal_sign",
                Arrays.asList(message, accountId, password),
                storm3jService,
                PersonalSign.class);
    }

    @Override
    public Request<?, PersonalEcRecover> personalEcRecover(
            String hexMessage, String signedMessage) {
        return new Request<>(
                "personal_ecRecover",
                Arrays.asList(hexMessage, signedMessage),
                storm3jService,
                PersonalEcRecover.class);
    }

    @Override
    public Request<?, MinerStartResponse> minerStart(int threadCount) {
        return new Request<>(
                "miner_start", Arrays.asList(threadCount), storm3jService, MinerStartResponse.class);
    }

    @Override
    public Request<?, BooleanResponse> minerStop() {
        return new Request<>(
                "miner_stop", Collections.<String>emptyList(), storm3jService, BooleanResponse.class);
    }

    public Flowable<PendingTransactionNotification> newPendingTransactionsNotifications() {
        return storm3jService.subscribe(
                new Request<>(
                        "eth_subscribe",
                        Arrays.asList("newPendingTransactions"),
                        storm3jService,
                        FstSubscribe.class),
                "eth_unsubscribe",
                PendingTransactionNotification.class);
    }

    @Override
    public Flowable<SyncingNotfication> syncingStatusNotifications() {
        return storm3jService.subscribe(
                new Request<>(
                        "eth_subscribe",
                        Arrays.asList("syncing"),
                        storm3jService,
                        FstSubscribe.class),
                "eth_unsubscribe",
                SyncingNotfication.class);
    }
}
