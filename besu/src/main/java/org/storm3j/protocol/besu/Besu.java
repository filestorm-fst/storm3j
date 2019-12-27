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

import java.util.List;
import java.util.Map;

import org.storm3j.protocol.storm3jService;
import org.storm3j.protocol.admin.methods.response.BooleanResponse;
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
import org.storm3j.protocol.eea.Eea;
import org.storm3j.utils.Base64String;

public interface Besu extends Eea {
    static Besu build(storm3jService storm3jService) {
        return new org.storm3j.protocol.besu.JsonRpc2_0Besu(storm3jService);
    }

    Request<?, MinerStartResponse> minerStart();

    Request<?, BooleanResponse> minerStop();

    /** @deprecated This is deprecated as the method name is wrong. */
    default Request<?, BooleanResponse> clicqueDiscard(String address) {
        return cliqueDiscard(address);
    }

    /** @deprecated This is deprecated as the method name is wrong. */
    default Request<?, FstAccounts> clicqueGetSigners(DefaultBlockParameter defaultBlockParameter) {
        return cliqueGetSigners(defaultBlockParameter);
    }

    /** @deprecated This is deprecated as the method name is wrong. */
    default Request<?, FstAccounts> clicqueGetSignersAtHash(String blockHash) {
        return cliqueGetSignersAtHash(blockHash);
    }

    Request<?, BooleanResponse> cliqueDiscard(String address);

    Request<?, FstAccounts> cliqueGetSigners(DefaultBlockParameter defaultBlockParameter);

    Request<?, FstAccounts> cliqueGetSignersAtHash(String blockHash);

    Request<?, BooleanResponse> cliquePropose(String address, Boolean signerAddition);

    Request<?, BesuEthAccountsMapResponse> cliqueProposals();

    Request<?, BesuFullDebugTraceResponse> debugTraceTransaction(
            String transactionHash, Map<String, Boolean> options);

    Request<?, FstGetTransactionCount> privGetTransactionCount(
            final String address, final Base64String privacyGroupId);

    Request<?, PrivGetPrivateTransaction> privGetPrivateTransaction(final String transactionHash);

    Request<?, PrivGetPrivacyPrecompileAddress> privGetPrivacyPrecompileAddress();

    Request<?, PrivCreatePrivacyGroup> privCreatePrivacyGroup(
            final List<Base64String> addresses, final String name, final String description);

    Request<?, PrivFindPrivacyGroup> privFindPrivacyGroup(final List<Base64String> addresses);

    Request<?, BooleanResponse> privDeletePrivacyGroup(final Base64String privacyGroupId);

    Request<?, PrivGetTransactionReceipt> privGetTransactionReceipt(final String transactionHash);
}
