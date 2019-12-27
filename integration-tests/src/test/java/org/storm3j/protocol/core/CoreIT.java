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
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.methods.response.*;
import org.storm3j.protocol.core.methods.response.FstBlock;
import org.storm3j.protocol.http.HttpService;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** JSON-RPC 2.0 Integration Tests. */
public class CoreIT {

    private Storm3j storm3j;

    private IntegrationTestConfig config = new TestnetConfig();

    public CoreIT() {}

    @Before
    public void setUp() {
        this.storm3j = Storm3j.build(new HttpService());
    }

    @Test
    public void testWeb3ClientVersion() throws Exception {
        Storm3ClientVersion web3ClientVersion = storm3j.storm3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        System.out.println("Fst client version: " + clientVersion);
        assertFalse(clientVersion.isEmpty());
    }

    @Test
    public void testWeb3Sha3() throws Exception {
        Storm3Sha3 web3Sha3 = storm3j.storm3Sha3("0x68656c6c6f20776f726c64").send();
        assertThat(
                web3Sha3.getResult(),
                is("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"));
    }

    @Test
    public void testNetVersion() throws Exception {
        NetVersion netVersion = storm3j.netVersion().send();
        assertFalse(netVersion.getNetVersion().isEmpty());
    }

    @Test
    public void testNetListening() throws Exception {
        NetListening netListening = storm3j.netListening().send();
        assertTrue(netListening.isListening());
    }

    @Test
    public void testNetPeerCount() throws Exception {
        NetPeerCount netPeerCount = storm3j.netPeerCount().send();
        assertTrue(netPeerCount.getQuantity().signum() == 1);
    }

    @Test
    public void testEthProtocolVersion() throws Exception {
        FstProtocolVersion fstProtocolVersion = storm3j.fstProtocolVersion().send();
        assertFalse(fstProtocolVersion.getProtocolVersion().isEmpty());
    }

    @Test
    public void testEthSyncing() throws Exception {
        FstSyncing fstSyncing = storm3j.fstSyncing().send();
        assertNotNull(fstSyncing.getResult());
    }

    @Test
    public void testEthCoinbase() throws Exception {
        FstCoinbase fstCoinbase = storm3j.fstCoinbase().send();
        assertNotNull(fstCoinbase.getAddress());
    }

    @Test
    public void testEthMining() throws Exception {
        FstMining fstMining = storm3j.fstMining().send();
        assertNotNull(fstMining.getResult());
    }

    @Test
    public void testEthHashrate() throws Exception {
        FstHashrate fstHashrate = storm3j.fstHashrate().send();
        assertThat(fstHashrate.getHashrate(), is(BigInteger.ZERO));
    }

    @Test
    public void testEthGasPrice() throws Exception {
        FstGasPrice fstGasPrice = storm3j.fstGasPrice().send();
        assertTrue(fstGasPrice.getGasPrice().signum() == 1);
    }

    @Test
    public void testEthAccounts() throws Exception {
        FstAccounts fstAccounts = storm3j.fstAccounts().send();
        assertNotNull(fstAccounts.getAccounts());
    }

    @Test
    public void testEthBlockNumber() throws Exception {
        FstBlockNumber fstBlockNumber = storm3j.fstBlockNumber().send();
        assertTrue(fstBlockNumber.getBlockNumber().signum() == 1);
    }

    @Test
    public void testEthGetBalance() throws Exception {
        FstGetBalance fstGetBalance =
                storm3j.fstGetBalance(config.validAccount(), DefaultBlockParameter.valueOf("latest"))
                        .send();
        assertTrue(fstGetBalance.getBalance().signum() == 1);
    }

    @Test
    public void testEthGetStorageAt() throws Exception {
        FstGetStorageAt fstGetStorageAt =
                storm3j.fstGetStorageAt(
                                config.validContractAddress(),
                                BigInteger.valueOf(0),
                                DefaultBlockParameter.valueOf("latest"))
                        .send();
        assertThat(fstGetStorageAt.getData(), is(config.validContractAddressPositionZero()));
    }

    @Test
    public void testEthGetTransactionCount() throws Exception {
        FstGetTransactionCount fstGetTransactionCount =
                storm3j.fstGetTransactionCount(
                                config.validAccount(), DefaultBlockParameter.valueOf("latest"))
                        .send();
        assertTrue(fstGetTransactionCount.getTransactionCount().signum() == 1);
    }

    @Test
    public void testEthGetBlockTransactionCountByHash() throws Exception {
        FstGetBlockTransactionCountByHash fstGetBlockTransactionCountByHash =
                storm3j.fstGetBlockTransactionCountByHash(config.validBlockHash()).send();
        assertThat(
                fstGetBlockTransactionCountByHash.getTransactionCount(),
                equalTo(config.validBlockTransactionCount()));
    }

    @Test
    public void testEthGetBlockTransactionCountByNumber() throws Exception {
        FstGetBlockTransactionCountByNumber fstGetBlockTransactionCountByNumber =
                storm3j.fstGetBlockTransactionCountByNumber(
                                DefaultBlockParameter.valueOf(config.validBlock()))
                        .send();
        assertThat(
                fstGetBlockTransactionCountByNumber.getTransactionCount(),
                equalTo(config.validBlockTransactionCount()));
    }

    @Test
    public void testEthGetUncleCountByBlockHash() throws Exception {
        FstGetUncleCountByBlockHash fstGetUncleCountByBlockHash =
                storm3j.fstGetUncleCountByBlockHash(config.validBlockHash()).send();
        assertThat(
                fstGetUncleCountByBlockHash.getUncleCount(),
                equalTo(config.validBlockUncleCount()));
    }

    @Test
    public void testEthGetUncleCountByBlockNumber() throws Exception {
        FstGetUncleCountByBlockNumber fstGetUncleCountByBlockNumber =
                storm3j.fstGetUncleCountByBlockNumber(DefaultBlockParameter.valueOf("latest")).send();
        assertThat(
                fstGetUncleCountByBlockNumber.getUncleCount(),
                equalTo(config.validBlockUncleCount()));
    }

    @Test
    public void testEthGetCode() throws Exception {
        FstGetCode fstGetCode =
                storm3j.fstGetCode(
                                config.validContractAddress(),
                                DefaultBlockParameter.valueOf(config.validBlock()))
                        .send();
        assertThat(fstGetCode.getCode(), is(config.validContractCode()));
    }

    @Ignore // TODO: Once account unlock functionality is available
    @Test
    public void testEthSign() throws Exception {
        // FstSign ethSign = storm3j.ethSign();
    }

    @Ignore // TODO: Once account unlock functionality is available
    @Test
    public void testEthSendTransaction() throws Exception {
        FstSendTransaction fstSendTransaction =
                storm3j.fstSendTransaction(config.buildTransaction()).send();
        assertFalse(fstSendTransaction.getTransactionHash().isEmpty());
    }

    @Ignore // TODO: Once account unlock functionality is available
    @Test
    public void testEthSendRawTransaction() throws Exception {}

    @Test
    public void testEthCall() throws Exception {
        FstCall fstCall =
                storm3j.fstCall(config.buildTransaction(), DefaultBlockParameter.valueOf("latest"))
                        .send();

        assertThat(DefaultBlockParameterName.LATEST.getValue(), is("latest"));
        assertThat(fstCall.getValue(), is("0x"));
    }

    @Test
    public void testEthEstimateGas() throws Exception {
        FstEstimateGas fstEstimateGas = storm3j.fstEstimateGas(config.buildTransaction()).send();
        assertTrue(fstEstimateGas.getAmountUsed().signum() == 1);
    }

    @Test
    public void testEthGetBlockByHashReturnHashObjects() throws Exception {
        FstBlock fstBlock = storm3j.fstGetBlockByHash(config.validBlockHash(), false).send();

        FstBlock.Block block = fstBlock.getBlock();
        assertNotNull(fstBlock.getBlock());
        assertThat(block.getNumber(), equalTo(config.validBlock()));
        assertThat(
                block.getTransactions().size(), is(config.validBlockTransactionCount().intValue()));
    }

    @Test
    public void testEthGetBlockByHashReturnFullTransactionObjects() throws Exception {
        FstBlock fstBlock = storm3j.fstGetBlockByHash(config.validBlockHash(), true).send();

        FstBlock.Block block = fstBlock.getBlock();
        assertNotNull(fstBlock.getBlock());
        assertThat(block.getNumber(), equalTo(config.validBlock()));
        assertThat(
                block.getTransactions().size(),
                equalTo(config.validBlockTransactionCount().intValue()));
    }

    @Test
    public void testEthGetBlockByNumberReturnHashObjects() throws Exception {
        FstBlock fstBlock =
                storm3j.fstGetBlockByNumber(DefaultBlockParameter.valueOf(config.validBlock()), false)
                        .send();

        FstBlock.Block block = fstBlock.getBlock();
        assertNotNull(fstBlock.getBlock());
        assertThat(block.getNumber(), equalTo(config.validBlock()));
        assertThat(
                block.getTransactions().size(),
                equalTo(config.validBlockTransactionCount().intValue()));
    }

    @Test
    public void testEthGetBlockByNumberReturnTransactionObjects() throws Exception {
        FstBlock fstBlock =
                storm3j.fstGetBlockByNumber(DefaultBlockParameter.valueOf(config.validBlock()), true)
                        .send();

        FstBlock.Block block = fstBlock.getBlock();
        assertNotNull(fstBlock.getBlock());
        assertThat(block.getNumber(), equalTo(config.validBlock()));
        assertThat(
                block.getTransactions().size(),
                equalTo(config.validBlockTransactionCount().intValue()));
    }

    @Test
    public void testEthGetTransactionByHash() throws Exception {
        FstTransaction fstTransaction =
                storm3j.fstGetTransactionByHash(config.validTransactionHash()).send();
        assertTrue(fstTransaction.getTransaction().isPresent());
        Transaction transaction = fstTransaction.getTransaction().get();
        assertThat(transaction.getBlockHash(), is(config.validBlockHash()));
    }

    @Test
    public void testEthGetTransactionByBlockHashAndIndex() throws Exception {
        BigInteger index = BigInteger.ONE;

        FstTransaction fstTransaction =
                storm3j.fstGetTransactionByBlockHashAndIndex(config.validBlockHash(), index).send();
        assertTrue(fstTransaction.getTransaction().isPresent());
        Transaction transaction = fstTransaction.getTransaction().get();
        assertThat(transaction.getBlockHash(), is(config.validBlockHash()));
        assertThat(transaction.getTransactionIndex(), equalTo(index));
    }

    @Test
    public void testEthGetTransactionByBlockNumberAndIndex() throws Exception {
        BigInteger index = BigInteger.ONE;

        FstTransaction fstTransaction =
                storm3j.fstGetTransactionByBlockNumberAndIndex(
                                DefaultBlockParameter.valueOf(config.validBlock()), index)
                        .send();
        assertTrue(fstTransaction.getTransaction().isPresent());
        Transaction transaction = fstTransaction.getTransaction().get();
        assertThat(transaction.getBlockHash(), is(config.validBlockHash()));
        assertThat(transaction.getTransactionIndex(), equalTo(index));
    }

    @Test
    public void testEthGetTransactionReceipt() throws Exception {
        FstGetTransactionReceipt fstGetTransactionReceipt =
                storm3j.fstGetTransactionReceipt(config.validTransactionHash()).send();
        assertTrue(fstGetTransactionReceipt.getTransactionReceipt().isPresent());
        TransactionReceipt transactionReceipt =
                fstGetTransactionReceipt.getTransactionReceipt().get();
        assertThat(transactionReceipt.getTransactionHash(), is(config.validTransactionHash()));
    }

    @Test
    public void testEthGetUncleByBlockHashAndIndex() throws Exception {
        FstBlock fstBlock =
                storm3j.fstGetUncleByBlockHashAndIndex(config.validUncleBlockHash(), BigInteger.ZERO)
                        .send();
        assertNotNull(fstBlock.getBlock());
    }

    @Test
    public void testEthGetUncleByBlockNumberAndIndex() throws Exception {
        FstBlock fstBlock =
                storm3j.fstGetUncleByBlockNumberAndIndex(
                                DefaultBlockParameter.valueOf(config.validUncleBlock()),
                                BigInteger.ZERO)
                        .send();
        assertNotNull(fstBlock.getBlock());
    }

    @Test
    public void testEthGetCompilers() throws Exception {
        FstGetCompilers fstGetCompilers = storm3j.fstGetCompilers().send();
        assertNotNull(fstGetCompilers.getCompilers());
    }

    @Ignore // The method eth_compileLLL does not exist/is not available
    @Test
    public void testEthCompileLLL() throws Exception {
        FstCompileLLL fstCompileLLL = storm3j.fstCompileLLL("(returnlll (suicide (caller)))").send();
        assertFalse(fstCompileLLL.getCompiledSourceCode().isEmpty());
    }

    @Test
    public void testEthCompileSolidity() throws Exception {
        String sourceCode =
                "pragma solidity ^0.4.0;"
                        + "\ncontract test { function multiply(uint a) returns(uint d) {"
                        + "   return a * 7;   } }"
                        + "\ncontract test2 { function multiply2(uint a) returns(uint d) {"
                        + "   return a * 7;   } }";
        FstCompileSolidity fstCompileSolidity = storm3j.fstCompileSolidity(sourceCode).send();
        assertNotNull(fstCompileSolidity.getCompiledSolidity());
        assertThat(
                fstCompileSolidity.getCompiledSolidity().get("test2").getInfo().getSource(),
                is(sourceCode));
    }

    @Ignore // The method eth_compileSerpent does not exist/is not available
    @Test
    public void testEthCompileSerpent() throws Exception {
        FstCompileSerpent fstCompileSerpent = storm3j.fstCompileSerpent("/* some serpent */").send();
        assertFalse(fstCompileSerpent.getCompiledSourceCode().isEmpty());
    }

    @Test
    public void testFiltersByFilterId() throws Exception {
        org.storm3j.protocol.core.methods.request.FstFilter fstFilter =
                new org.storm3j.protocol.core.methods.request.FstFilter(
                        DefaultBlockParameterName.EARLIEST,
                        DefaultBlockParameterName.LATEST,
                        config.validContractAddress());

        String eventSignature = config.encodedEvent();
        fstFilter.addSingleTopic(eventSignature);

        // eth_newFilter
        FstFilter ethNewFilter = storm3j.fstNewFilter(fstFilter).send();
        BigInteger filterId = ethNewFilter.getFilterId();

        // eth_getFilterLogs
        FstLog ethFilterLogs = storm3j.fstGetFilterLogs(filterId).send();
        List<FstLog.LogResult> filterLogs = ethFilterLogs.getLogs();
        assertFalse(filterLogs.isEmpty());

        // eth_getFilterChanges - nothing will have changed in this interval
        FstLog fstLog = storm3j.fstGetFilterChanges(filterId).send();
        assertTrue(fstLog.getLogs().isEmpty());

        // eth_uninstallFilter
        FstUninstallFilter fstUninstallFilter = storm3j.fstUninstallFilter(filterId).send();
        assertTrue(fstUninstallFilter.isUninstalled());
    }

    @Test
    public void testEthNewBlockFilter() throws Exception {
        FstFilter ethNewBlockFilter = storm3j.fstNewBlockFilter().send();
        assertNotNull(ethNewBlockFilter.getFilterId());
    }

    @Test
    public void testEthNewPendingTransactionFilter() throws Exception {
        FstFilter ethNewPendingTransactionFilter = storm3j.fstNewPendingTransactionFilter().send();
        assertNotNull(ethNewPendingTransactionFilter.getFilterId());
    }

    @Test
    public void testEthGetLogs() throws Exception {
        org.storm3j.protocol.core.methods.request.FstFilter fstFilter =
                new org.storm3j.protocol.core.methods.request.FstFilter(
                        DefaultBlockParameterName.EARLIEST,
                        DefaultBlockParameterName.LATEST,
                        config.validContractAddress());

        fstFilter.addSingleTopic(config.encodedEvent());

        FstLog fstLog = storm3j.fstGetLogs(fstFilter).send();
        List<FstLog.LogResult> logs = fstLog.getLogs();
        assertFalse(logs.isEmpty());
    }

    // @Test
    // public void testEthGetWork() throws Exception {
    //     FstGetWork ethGetWork = requestFactory.ethGetWork();
    //     assertNotNull(ethGetWork.getResult());
    // }

    @Test
    public void testEthSubmitWork() throws Exception {}

    @Test
    public void testEthSubmitHashrate() throws Exception {}

    @Test
    public void testDbPutString() throws Exception {}

    @Test
    public void testDbGetString() throws Exception {}

    @Test
    public void testDbPutHex() throws Exception {}

    @Test
    public void testDbGetHex() throws Exception {}

    @Test
    public void testShhPost() throws Exception {}

    @Ignore // The method shh_version does not exist/is not available
    @Test
    public void testShhVersion() throws Exception {
        ShhVersion shhVersion = storm3j.shhVersion().send();
        assertNotNull(shhVersion.getVersion());
    }

    @Ignore // The method shh_newIdentity does not exist/is not available
    @Test
    public void testShhNewIdentity() throws Exception {
        ShhNewIdentity shhNewIdentity = storm3j.shhNewIdentity().send();
        assertNotNull(shhNewIdentity.getAddress());
    }

    @Test
    public void testShhHasIdentity() throws Exception {}

    @Ignore // The method shh_newIdentity does not exist/is not available
    @Test
    public void testShhNewGroup() throws Exception {
        ShhNewGroup shhNewGroup = storm3j.shhNewGroup().send();
        assertNotNull(shhNewGroup.getAddress());
    }

    @Ignore // The method shh_addToGroup does not exist/is not available
    @Test
    public void testShhAddToGroup() throws Exception {}

    @Test
    public void testShhNewFilter() throws Exception {}

    @Test
    public void testShhUninstallFilter() throws Exception {}

    @Test
    public void testShhGetFilterChanges() throws Exception {}

    @Test
    public void testShhGetMessages() throws Exception {}
}
