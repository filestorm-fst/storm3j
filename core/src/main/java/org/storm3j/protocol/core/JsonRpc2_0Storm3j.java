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

import io.reactivex.Flowable;
import org.storm3j.protocol.core.methods.response.*;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.core.methods.request.ShhFilter;
import org.storm3j.protocol.core.methods.request.ShhPost;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.FstBlock;
import org.storm3j.protocol.rx.JsonRpc2_0Rx;
import org.storm3j.protocol.websocket.events.LogNotification;
import org.storm3j.protocol.websocket.events.NewHeadsNotification;
import org.storm3j.utils.Async;
import org.storm3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/** JSON-RPC 2.0 factory implementation. */
public class JsonRpc2_0Storm3j implements Storm3j {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final Storm3jService storm3jService;
    private final JsonRpc2_0Rx storm3jRx;
    private final long blockTime;
    private final ScheduledExecutorService scheduledExecutorService;

    public JsonRpc2_0Storm3j(Storm3jService storm3jService) {
        this(storm3jService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0Storm3j(
            Storm3jService storm3jService,
            long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.storm3jService = storm3jService;
        this.storm3jRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public Request<?, Storm3ClientVersion> storm3ClientVersion() {
        return new Request<>(
                "web3_clientVersion",
                Collections.<String>emptyList(),
                storm3jService,
                Storm3ClientVersion.class);
    }

    @Override
    public Request<?, Storm3Sha3> storm3Sha3(String data) {
        return new Request<>("web3_sha3", Arrays.asList(data), storm3jService, Storm3Sha3.class);
    }

    @Override
    public Request<?, NetVersion> netVersion() {
        return new Request<>(
                "net_version", Collections.<String>emptyList(), storm3jService, NetVersion.class);
    }

    @Override
    public Request<?, NetListening> netListening() {
        return new Request<>(
                "net_listening", Collections.<String>emptyList(), storm3jService, NetListening.class);
    }

    @Override
    public Request<?, NetPeerCount> netPeerCount() {
        return new Request<>(
                "net_peerCount", Collections.<String>emptyList(), storm3jService, NetPeerCount.class);
    }

    @Override
    public Request<?, FstProtocolVersion> fstProtocolVersion() {
        return new Request<>(
                "fst_protocolVersion",
                Collections.<String>emptyList(),
                storm3jService,
                FstProtocolVersion.class);
    }

    @Override
    public Request<?, FstCoinbase> fstCoinbase() {
        return new Request<>(
                "fst_coinbase", Collections.<String>emptyList(), storm3jService, FstCoinbase.class);
    }

    @Override
    public Request<?, FstSyncing> fstSyncing() {
        return new Request<>(
                "fst_syncing", Collections.<String>emptyList(), storm3jService, FstSyncing.class);
    }

    @Override
    public Request<?, FstMining> fstMining() {
        return new Request<>(
                "fst_mining", Collections.<String>emptyList(), storm3jService, FstMining.class);
    }

    @Override
    public Request<?, FstHashrate> fstHashrate() {
        return new Request<>(
                "fst_hashrate", Collections.<String>emptyList(), storm3jService, FstHashrate.class);
    }

    @Override
    public Request<?, FstGasPrice> fstGasPrice() {
        return new Request<>(
                "fst_gasPrice", Collections.<String>emptyList(), storm3jService, FstGasPrice.class);
    }

    @Override
    public Request<?, FstAccounts> fstAccounts() {
        return new Request<>(
                "fst_accounts", Collections.<String>emptyList(), storm3jService, FstAccounts.class);
    }

    @Override
    public Request<?, FstBlockNumber> fstBlockNumber() {
        return new Request<>(
                "fst_blockNumber",
                Collections.<String>emptyList(),
                storm3jService,
                FstBlockNumber.class);
    }

    @Override
    public Request<?, FstGetBalance> fstGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_getBalance",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                storm3jService,
                FstGetBalance.class);
    }

    @Override
    public Request<?, FstGetStorageAt> fstGetStorageAt(
            String address, BigInteger position, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_getStorageAt",
                Arrays.asList(
                        address,
                        Numeric.encodeQuantity(position),
                        defaultBlockParameter.getValue()),
                storm3jService,
                FstGetStorageAt.class);
    }

    @Override
    public Request<?, FstGetTransactionCount> fstGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_getTransactionCount",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                storm3jService,
                FstGetTransactionCount.class);
    }

    @Override
    public Request<?, FstGetBlockTransactionCountByHash> fstGetBlockTransactionCountByHash(
            String blockHash) {
        return new Request<>(
                "fst_getBlockTransactionCountByHash",
                Arrays.asList(blockHash),
                storm3jService,
                FstGetBlockTransactionCountByHash.class);
    }

    @Override
    public Request<?, FstGetBlockTransactionCountByNumber> fstGetBlockTransactionCountByNumber(
            DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_getBlockTransactionCountByNumber",
                Arrays.asList(defaultBlockParameter.getValue()),
                storm3jService,
                FstGetBlockTransactionCountByNumber.class);
    }

    @Override
    public Request<?, FstGetUncleCountByBlockHash> fstGetUncleCountByBlockHash(String blockHash) {
        return new Request<>(
                "fst_getUncleCountByBlockHash",
                Arrays.asList(blockHash),
                storm3jService,
                FstGetUncleCountByBlockHash.class);
    }

    @Override
    public Request<?, FstGetUncleCountByBlockNumber> fstGetUncleCountByBlockNumber(
            DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_getUncleCountByBlockNumber",
                Arrays.asList(defaultBlockParameter.getValue()),
                storm3jService,
                FstGetUncleCountByBlockNumber.class);
    }

    @Override
    public Request<?, FstGetCode> fstGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_getCode",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                storm3jService,
                FstGetCode.class);
    }

    @Override
    public Request<?, FstSign> fstSign(String address, String sha3HashOfDataToSign) {
        return new Request<>(
                "fst_sign",
                Arrays.asList(address, sha3HashOfDataToSign),
                storm3jService,
                FstSign.class);
    }

    @Override
    public Request<?, FstSendTransaction>
    fstSendTransaction(Transaction transaction) {
        return new Request<>(
                "fst_sendTransaction",
                Arrays.asList(transaction),
                storm3jService,
                FstSendTransaction.class);
    }

    @Override
    public Request<?, FstSendTransaction>
    fstSendRawTransaction(String signedTransactionData) {
        return new Request<>(
                "fst_sendRawTransaction",
                Arrays.asList(signedTransactionData),
                storm3jService,
                FstSendTransaction.class);
    }

    @Override
    public Request<?, FstCall> fstCall(
            Transaction transaction, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "fst_call",
                Arrays.asList(transaction, defaultBlockParameter),
                storm3jService,
                FstCall.class);
    }

    @Override
    public Request<?, FstEstimateGas> fstEstimateGas(Transaction transaction) {
        return new Request<>(
                "fst_estimateGas", Arrays.asList(transaction), storm3jService, FstEstimateGas.class);
    }

    @Override
    public Request<?, FstBlock> fstGetBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return new Request<>(
                "fst_getBlockByHash",
                Arrays.asList(blockHash, returnFullTransactionObjects),
                storm3jService,
                FstBlock.class);
    }

    @Override
    public Request<?, FstBlock> fstGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects) {
        return new Request<>(
                "fst_getBlockByNumber",
                Arrays.asList(defaultBlockParameter.getValue(), returnFullTransactionObjects),
                storm3jService,
                FstBlock.class);
    }

    @Override
    public Request<?, FstTransaction> fstGetTransactionByHash(String transactionHash) {
        return new Request<>(
                "fst_getTransactionByHash",
                Arrays.asList(transactionHash),
                storm3jService,
                FstTransaction.class);
    }

    @Override
    public Request<?, FstTransaction> fstGetTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return new Request<>(
                "fst_getTransactionByBlockHashAndIndex",
                Arrays.asList(blockHash, Numeric.encodeQuantity(transactionIndex)),
                storm3jService,
                FstTransaction.class);
    }

    @Override
    public Request<?, FstTransaction> fstGetTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex) {
        return new Request<>(
                "fst_getTransactionByBlockNumberAndIndex",
                Arrays.asList(
                        defaultBlockParameter.getValue(), Numeric.encodeQuantity(transactionIndex)),
                storm3jService,
                FstTransaction.class);
    }

    @Override
    public Request<?, FstGetTransactionReceipt> fstGetTransactionReceipt(String transactionHash) {
        return new Request<>(
                "fst_getTransactionReceipt",
                Arrays.asList(transactionHash),
                storm3jService,
                FstGetTransactionReceipt.class);
    }

    @Override
    public Request<?, FstBlock> fstGetUncleByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return new Request<>(
                "fst_getUncleByBlockHashAndIndex",
                Arrays.asList(blockHash, Numeric.encodeQuantity(transactionIndex)),
                storm3jService,
                FstBlock.class);
    }

    @Override
    public Request<?, FstBlock> fstGetUncleByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger uncleIndex) {
        return new Request<>(
                "fst_getUncleByBlockNumberAndIndex",
                Arrays.asList(defaultBlockParameter.getValue(), Numeric.encodeQuantity(uncleIndex)),
                storm3jService,
                FstBlock.class);
    }

    @Override
    public Request<?, FstGetCompilers> fstGetCompilers() {
        return new Request<>(
                "fst_getCompilers",
                Collections.<String>emptyList(),
                storm3jService,
                FstGetCompilers.class);
    }

    @Override
    public Request<?, FstCompileLLL> fstCompileLLL(String sourceCode) {
        return new Request<>(
                "fst_compileLLL", Arrays.asList(sourceCode), storm3jService, FstCompileLLL.class);
    }

    @Override
    public Request<?, FstCompileSolidity> fstCompileSolidity(String sourceCode) {
        return new Request<>(
                "fst_compileSolidity",
                Arrays.asList(sourceCode),
                storm3jService,
                FstCompileSolidity.class);
    }

    @Override
    public Request<?, FstCompileSerpent> fstCompileSerpent(String sourceCode) {
        return new Request<>(
                "fst_compileSerpent",
                Arrays.asList(sourceCode),
                storm3jService,
                FstCompileSerpent.class);
    }

    @Override
    public Request<?, FstFilter> fstNewFilter(
            org.storm3j.protocol.core.methods.request.FstFilter fstFilter) {
        return new Request<>(
                "fst_newFilter", Arrays.asList(fstFilter), storm3jService, FstFilter.class);
    }

    @Override
    public Request<?, FstFilter> fstNewBlockFilter() {
        return new Request<>(
                "fst_newBlockFilter",
                Collections.<String>emptyList(),
                storm3jService,
                FstFilter.class);
    }

    @Override
    public Request<?, FstFilter> fstNewPendingTransactionFilter() {
        return new Request<>(
                "fst_newPendingTransactionFilter",
                Collections.<String>emptyList(),
                storm3jService,
                FstFilter.class);
    }

    @Override
    public Request<?, FstUninstallFilter> fstUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "fst_uninstallFilter",
                Arrays.asList(Numeric.toHexStringWithPrefixSafe(filterId)),
                storm3jService,
                FstUninstallFilter.class);
    }

    @Override
    public Request<?, FstLog> fstGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "fst_getFilterChanges",
                Arrays.asList(Numeric.toHexStringWithPrefixSafe(filterId)),
                storm3jService,
                FstLog.class);
    }

    @Override
    public Request<?, FstLog> fstGetFilterLogs(BigInteger filterId) {
        return new Request<>(
                "fst_getFilterLogs",
                Arrays.asList(Numeric.toHexStringWithPrefixSafe(filterId)),
                storm3jService,
                FstLog.class);
    }

    @Override
    public Request<?, FstLog> fstGetLogs(
            org.storm3j.protocol.core.methods.request.FstFilter fstFilter) {
        return new Request<>("fst_getLogs", Arrays.asList(fstFilter), storm3jService, FstLog.class);
    }

    @Override
    public Request<?, FstGetWork> fstGetWork() {
        return new Request<>(
                "fst_getWork", Collections.<String>emptyList(), storm3jService, FstGetWork.class);
    }

    @Override
    public Request<?, FstSubmitWork> fstSubmitWork(
            String nonce, String headerPowHash, String mixDigest) {
        return new Request<>(
                "fst_submitWork",
                Arrays.asList(nonce, headerPowHash, mixDigest),
                storm3jService,
                FstSubmitWork.class);
    }

    @Override
    public Request<?, FstSubmitHashrate> fstSubmitHashrate(String hashrate, String clientId) {
        return new Request<>(
                "fst_submitHashrate",
                Arrays.asList(hashrate, clientId),
                storm3jService,
                FstSubmitHashrate.class);
    }

    @Override
    public Request<?, DbPutString> dbPutString(
            String databaseName, String keyName, String stringToStore) {
        return new Request<>(
                "db_putString",
                Arrays.asList(databaseName, keyName, stringToStore),
                storm3jService,
                DbPutString.class);
    }

    @Override
    public Request<?, DbGetString> dbGetString(String databaseName, String keyName) {
        return new Request<>(
                "db_getString",
                Arrays.asList(databaseName, keyName),
                storm3jService,
                DbGetString.class);
    }

    @Override
    public Request<?, DbPutHex> dbPutHex(String databaseName, String keyName, String dataToStore) {
        return new Request<>(
                "db_putHex",
                Arrays.asList(databaseName, keyName, dataToStore),
                storm3jService,
                DbPutHex.class);
    }

    @Override
    public Request<?, DbGetHex> dbGetHex(String databaseName, String keyName) {
        return new Request<>(
                "db_getHex", Arrays.asList(databaseName, keyName), storm3jService, DbGetHex.class);
    }

    @Override
    public Request<?, org.storm3j.protocol.core.methods.response.ShhPost> shhPost(ShhPost shhPost) {
        return new Request<>(
                "shh_post",
                Arrays.asList(shhPost),
                storm3jService,
                org.storm3j.protocol.core.methods.response.ShhPost.class);
    }

    @Override
    public Request<?, ShhVersion> shhVersion() {
        return new Request<>(
                "shh_version", Collections.<String>emptyList(), storm3jService, ShhVersion.class);
    }

    @Override
    public Request<?, ShhNewIdentity> shhNewIdentity() {
        return new Request<>(
                "shh_newIdentity",
                Collections.<String>emptyList(),
                storm3jService,
                ShhNewIdentity.class);
    }

    @Override
    public Request<?, ShhHasIdentity> shhHasIdentity(String identityAddress) {
        return new Request<>(
                "shh_hasIdentity",
                Arrays.asList(identityAddress),
                storm3jService,
                ShhHasIdentity.class);
    }

    @Override
    public Request<?, ShhNewGroup> shhNewGroup() {
        return new Request<>(
                "shh_newGroup", Collections.<String>emptyList(), storm3jService, ShhNewGroup.class);
    }

    @Override
    public Request<?, ShhAddToGroup> shhAddToGroup(String identityAddress) {
        return new Request<>(
                "shh_addToGroup",
                Arrays.asList(identityAddress),
                storm3jService,
                ShhAddToGroup.class);
    }

    @Override
    public Request<?, ShhNewFilter> shhNewFilter(ShhFilter shhFilter) {
        return new Request<>(
                "shh_newFilter", Arrays.asList(shhFilter), storm3jService, ShhNewFilter.class);
    }

    @Override
    public Request<?, ShhUninstallFilter> shhUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "shh_uninstallFilter",
                Arrays.asList(Numeric.toHexStringWithPrefixSafe(filterId)),
                storm3jService,
                ShhUninstallFilter.class);
    }

    @Override
    public Request<?, ShhMessages> shhGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "shh_getFilterChanges",
                Arrays.asList(Numeric.toHexStringWithPrefixSafe(filterId)),
                storm3jService,
                ShhMessages.class);
    }

    @Override
    public Request<?, ShhMessages> shhGetMessages(BigInteger filterId) {
        return new Request<>(
                "shh_getMessages",
                Arrays.asList(Numeric.toHexStringWithPrefixSafe(filterId)),
                storm3jService,
                ShhMessages.class);
    }

    @Override
    public Flowable<NewHeadsNotification> newHeadsNotifications() {
        return storm3jService.subscribe(
                new Request<>(
                        "fst_subscribe",
                        Collections.singletonList("newHeads"),
                        storm3jService,
                        FstSubscribe.class),
                "fst_unsubscribe",
                NewHeadsNotification.class);
    }

    @Override
    public Flowable<LogNotification> logsNotifications(
            List<String> addresses, List<String> topics) {

        Map<String, Object> params = createLogsParams(addresses, topics);

        return storm3jService.subscribe(
                new Request<>(
                        "fst_subscribe",
                        Arrays.asList("logs", params),
                        storm3jService,
                        FstSubscribe.class),
                "fst_unsubscribe",
                LogNotification.class);
    }

    private Map<String, Object> createLogsParams(List<String> addresses, List<String> topics) {
        Map<String, Object> params = new HashMap<>();
        if (!addresses.isEmpty()) {
            params.put("address", addresses);
        }
        if (!topics.isEmpty()) {
            params.put("topics", topics);
        }
        return params;
    }

    @Override
    public Flowable<String> fstBlockHashFlowable() {
        return storm3jRx.fstBlockHashFlowable(blockTime);
    }

    @Override
    public Flowable<String> fstPendingTransactionHashFlowable() {
        return storm3jRx.fstPendingTransactionHashFlowable(blockTime);
    }

    @Override
    public Flowable<Log> fstLogFlowable(
            org.storm3j.protocol.core.methods.request.FstFilter fstFilter) {
        return storm3jRx.fstLogFlowable(fstFilter, blockTime);
    }

    @Override
    public Flowable<org.storm3j.protocol.core.methods.response.Transaction> transactionFlowable() {
        return storm3jRx.transactionFlowable(blockTime);
    }

    @Override
    public Flowable<org.storm3j.protocol.core.methods.response.Transaction>
            pendingTransactionFlowable() {
        return storm3jRx.pendingTransactionFlowable(blockTime);
    }

    @Override
    public Flowable<FstBlock> blockFlowable(boolean fullTransactionObjects) {
        return storm3jRx.blockFlowable(fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<FstBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return storm3jRx.replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<FstBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending) {
        return storm3jRx.replayBlocksFlowable(
                startBlock, endBlock, fullTransactionObjects, ascending);
    }

    @Override
    public Flowable<FstBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<FstBlock> onCompleteFlowable) {
        return storm3jRx.replayPastBlocksFlowable(
                startBlock, fullTransactionObjects, onCompleteFlowable);
    }

    @Override
    public Flowable<FstBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return storm3jRx.replayPastBlocksFlowable(startBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<org.storm3j.protocol.core.methods.response.Transaction>
            replayPastTransactionsFlowable(
                    DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return storm3jRx.replayTransactionsFlowable(startBlock, endBlock);
    }

    @Override
    public Flowable<org.storm3j.protocol.core.methods.response.Transaction>
            replayPastTransactionsFlowable(DefaultBlockParameter startBlock) {
        return storm3jRx.replayPastTransactionsFlowable(startBlock);
    }

    @Override
    public Flowable<FstBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return storm3jRx.replayPastAndFutureBlocksFlowable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<org.storm3j.protocol.core.methods.response.Transaction>
            replayPastAndFutureTransactionsFlowable(DefaultBlockParameter startBlock) {
        return storm3jRx.replayPastAndFutureTransactionsFlowable(startBlock, blockTime);
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
        try {
            storm3jService.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close storm3j service", e);
        }
    }
}
