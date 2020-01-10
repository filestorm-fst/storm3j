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
package org.storm3j.protocol.besu;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.storm3j.protocol.besu.Besu;
import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.admin.methods.response.BooleanResponse;
import org.storm3j.protocol.besu.request.CreatePrivacyGroupRequest;
import org.storm3j.protocol.besu.response.BesuEthAccountsMapResponse;
import org.storm3j.protocol.besu.response.BesuFullDebugTraceResponse;
import org.storm3j.protocol.besu.response.privacy.PrivCreatePrivacyGroup;
import org.storm3j.protocol.besu.response.privacy.PrivFindPrivacyGroup;
import org.storm3j.protocol.besu.response.privacy.PrivGetPrivacyPrecompileAddress;
import org.storm3j.protocol.besu.response.privacy.PrivGetPrivateTransaction;
import org.storm3j.protocol.besu.response.privacy.PrivGetTransactionReceipt;
import org.storm3j.protocol.core.DefaultBlockParameter;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.FstAccounts;
import org.storm3j.protocol.core.methods.response.FstGetTransactionCount;
import org.storm3j.protocol.core.methods.response.MinerStartResponse;
import org.storm3j.protocol.eea.JsonRpc2_0Eea;
import org.storm3j.utils.Base64String;

import static java.util.Objects.requireNonNull;

public class JsonRpc2_0Besu extends JsonRpc2_0Eea implements Besu {
    public JsonRpc2_0Besu(Storm3jService storm3jService) {
        super(storm3jService);
    }

    @Override
    public Request<?, MinerStartResponse> minerStart() {
        return new Request<>(
                "miner_start",
                Collections.<String>emptyList(),
                storm3jService,
                MinerStartResponse.class);
    }

    @Override
    public Request<?, BooleanResponse> minerStop() {
        return new Request<>(
                "miner_stop", Collections.<String>emptyList(), storm3jService, BooleanResponse.class);
    }

    @Override
    public Request<?, BooleanResponse> cliqueDiscard(String address) {
        return new Request<>(
                "clique_discard", Arrays.asList(address), storm3jService, BooleanResponse.class);
    }

    @Override
    public Request<?, FstAccounts> cliqueGetSigners(DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "clique_getSigners",
                Arrays.asList(defaultBlockParameter.getValue()),
                storm3jService,
                FstAccounts.class);
    }

    @Override
    public Request<?, FstAccounts> cliqueGetSignersAtHash(String blockHash) {
        return new Request<>(
                "clique_getSignersAtHash",
                Arrays.asList(blockHash),
                storm3jService,
                FstAccounts.class);
    }

    @Override
    public Request<?, BooleanResponse> cliquePropose(String address, Boolean signerAddition) {
        return new Request<>(
                "clique_propose",
                Arrays.asList(address, signerAddition),
                storm3jService,
                BooleanResponse.class);
    }

    @Override
    public Request<?, BesuEthAccountsMapResponse> cliqueProposals() {
        return new Request<>(
                "clique_proposals",
                Collections.<String>emptyList(),
                storm3jService,
                BesuEthAccountsMapResponse.class);
    }

    @Override
    public Request<?, BesuFullDebugTraceResponse> debugTraceTransaction(
            String transactionHash, Map<String, Boolean> options) {
        return new Request<>(
                "debug_traceTransaction",
                Arrays.asList(transactionHash, options),
                storm3jService,
                BesuFullDebugTraceResponse.class);
    }

    @Override
    public Request<?, FstGetTransactionCount> privGetTransactionCount(
            final String address, final Base64String privacyGroupId) {
        return new Request<>(
                "priv_getTransactionCount",
                Arrays.asList(address, privacyGroupId.toString()),
                storm3jService,
                FstGetTransactionCount.class);
    }

    @Override
    public Request<?, PrivGetPrivateTransaction> privGetPrivateTransaction(
            final String transactionHash) {
        return new Request<>(
                "priv_getPrivateTransaction",
                Collections.singletonList(transactionHash),
                storm3jService,
                PrivGetPrivateTransaction.class);
    }

    @Override
    public Request<?, PrivGetPrivacyPrecompileAddress> privGetPrivacyPrecompileAddress() {
        return new Request<>(
                "priv_getPrivacyPrecompileAddress",
                Collections.emptyList(),
                storm3jService,
                PrivGetPrivacyPrecompileAddress.class);
    }

    @Override
    public Request<?, PrivCreatePrivacyGroup> privCreatePrivacyGroup(
            final List<Base64String> addresses, final String name, final String description) {
        requireNonNull(addresses);
        return new Request<>(
                "priv_createPrivacyGroup",
                Collections.singletonList(
                        new CreatePrivacyGroupRequest(addresses, name, description)),
                storm3jService,
                PrivCreatePrivacyGroup.class);
    }

    @Override
    public Request<?, PrivFindPrivacyGroup> privFindPrivacyGroup(
            final List<Base64String> addresses) {
        return new Request<>(
                "priv_findPrivacyGroup",
                Collections.singletonList(addresses),
                storm3jService,
                PrivFindPrivacyGroup.class);
    }

    @Override
    public Request<?, BooleanResponse> privDeletePrivacyGroup(final Base64String privacyGroupId) {
        return new Request<>(
                "priv_deletePrivacyGroup",
                Collections.singletonList(privacyGroupId.toString()),
                storm3jService,
                BooleanResponse.class);
    }

    @Override
    public Request<?, PrivGetTransactionReceipt> privGetTransactionReceipt(
            final String transactionHash) {
        return new Request<>(
                "priv_getTransactionReceipt",
                Collections.singletonList(transactionHash),
                storm3jService,
                PrivGetTransactionReceipt.class);
    }
}
