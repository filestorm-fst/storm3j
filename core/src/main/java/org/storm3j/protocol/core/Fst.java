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
package org.storm3j.protocol.core;

import java.math.BigInteger;

import org.storm3j.protocol.core.methods.request.ShhFilter;
import org.storm3j.protocol.core.methods.response.*;
import org.storm3j.protocol.core.methods.response.FstBlock;

/** Core Fst JSON-RPC API. */
public interface Fst {
    Request<?, Storm3ClientVersion> storm3ClientVersion();

    Request<?, Storm3Sha3> storm3Sha3(String data);

    Request<?, NetVersion> netVersion();

    Request<?, NetListening> netListening();

    Request<?, NetPeerCount> netPeerCount();

    Request<?, FstProtocolVersion> fstProtocolVersion();

    Request<?, FstCoinbase> fstCoinbase();

    Request<?, FstSyncing> fstSyncing();

    Request<?, FstMining> fstMining();

    Request<?, FstHashrate> fstHashrate();

    Request<?, FstGasPrice> fstGasPrice();

    Request<?, FstAccounts> fstAccounts();

    Request<?, FstBlockNumber> fstBlockNumber();

    Request<?, FstGetBalance> fstGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, FstGetStorageAt> fstGetStorageAt(
            String address, BigInteger position, DefaultBlockParameter defaultBlockParameter);

    Request<?, FstGetTransactionCount> fstGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, FstGetBlockTransactionCountByHash> fstGetBlockTransactionCountByHash(
            String blockHash);

    Request<?, FstGetBlockTransactionCountByNumber> fstGetBlockTransactionCountByNumber(
            DefaultBlockParameter defaultBlockParameter);

    Request<?, FstGetUncleCountByBlockHash> fstGetUncleCountByBlockHash(String blockHash);

    Request<?, FstGetUncleCountByBlockNumber> fstGetUncleCountByBlockNumber(
            DefaultBlockParameter defaultBlockParameter);

    Request<?, FstGetCode> fstGetCode(String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, FstSign> fstSign(String address, String sha3HashOfDataToSign);

    Request<?, FstSendTransaction> fstSendTransaction(
            org.storm3j.protocol.core.methods.request.Transaction transaction);

    Request<?, FstSendTransaction> fstSendRawTransaction(
            String signedTransactionData);

    Request<?, FstCall> fstCall(
            org.storm3j.protocol.core.methods.request.Transaction transaction,
            DefaultBlockParameter defaultBlockParameter);

    Request<?, FstEstimateGas> fstEstimateGas(
            org.storm3j.protocol.core.methods.request.Transaction transaction);

    Request<?, FstBlock> fstGetBlockByHash(String blockHash, boolean returnFullTransactionObjects);

    Request<?, FstBlock> fstGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects);

    Request<?, FstTransaction> fstGetTransactionByHash(String transactionHash);

    Request<?, FstTransaction> fstGetTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex);

    Request<?, FstTransaction> fstGetTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex);

    Request<?, FstGetTransactionReceipt> fstGetTransactionReceipt(String transactionHash);

    Request<?, FstBlock> fstGetUncleByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex);

    Request<?, FstBlock> fstGetUncleByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex);

    Request<?, FstGetCompilers> fstGetCompilers();

    Request<?, FstCompileLLL> fstCompileLLL(String sourceCode);

    Request<?, FstCompileSolidity> fstCompileSolidity(String sourceCode);

    Request<?, FstCompileSerpent> fstCompileSerpent(String sourceCode);

    Request<?, FstFilter> fstNewFilter(org.storm3j.protocol.core.methods.request.FstFilter fstFilter);

    Request<?, FstFilter> fstNewBlockFilter();

    Request<?, FstFilter> fstNewPendingTransactionFilter();

    Request<?, FstUninstallFilter> fstUninstallFilter(BigInteger filterId);

    Request<?, FstLog> fstGetFilterChanges(BigInteger filterId);

    Request<?, FstLog> fstGetFilterLogs(BigInteger filterId);

    Request<?, FstLog> fstGetLogs(org.storm3j.protocol.core.methods.request.FstFilter fstFilter);

    Request<?, FstGetWork> fstGetWork();

    Request<?, FstSubmitWork> fstSubmitWork(String nonce, String headerPowHash, String mixDigest);

    Request<?, FstSubmitHashrate> fstSubmitHashrate(String hashrate, String clientId);

    Request<?, DbPutString> dbPutString(String databaseName, String keyName, String stringToStore);

    Request<?, DbGetString> dbGetString(String databaseName, String keyName);

    Request<?, DbPutHex> dbPutHex(String databaseName, String keyName, String dataToStore);

    Request<?, DbGetHex> dbGetHex(String databaseName, String keyName);

    Request<?, org.storm3j.protocol.core.methods.response.ShhPost> shhPost(
            org.storm3j.protocol.core.methods.request.ShhPost shhPost);

    Request<?, ShhVersion> shhVersion();

    Request<?, ShhNewIdentity> shhNewIdentity();

    Request<?, ShhHasIdentity> shhHasIdentity(String identityAddress);

    Request<?, ShhNewGroup> shhNewGroup();

    Request<?, ShhAddToGroup> shhAddToGroup(String identityAddress);

    Request<?, ShhNewFilter> shhNewFilter(ShhFilter shhFilter);

    Request<?, ShhUninstallFilter> shhUninstallFilter(BigInteger filterId);

    Request<?, ShhMessages> shhGetFilterChanges(BigInteger filterId);

    Request<?, ShhMessages> shhGetMessages(BigInteger filterId);
}
