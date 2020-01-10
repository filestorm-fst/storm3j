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
import java.util.Arrays;

import org.junit.Test;

import org.storm3j.protocol.RequestTester;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.methods.request.FstFilter;
import org.storm3j.protocol.core.methods.request.ShhFilter;
import org.storm3j.protocol.core.methods.request.ShhPost;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.http.HttpService;
import org.storm3j.utils.Numeric;

public class RequestTest extends RequestTester {

    private Storm3j storm3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        storm3j = Storm3j.build(httpService);
    }

    @Test
    public void testWeb3ClientVersion() throws Exception {
        storm3j.storm3ClientVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"web3_clientVersion\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testWeb3Sha3() throws Exception {
        storm3j.storm3Sha3("0x68656c6c6f20776f726c64").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"web3_sha3\","
                        + "\"params\":[\"0x68656c6c6f20776f726c64\"],\"id\":1}");
    }

    @Test
    public void testNetVersion() throws Exception {
        storm3j.netVersion().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"net_version\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testNetListening() throws Exception {
        storm3j.netListening().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"net_listening\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testNetPeerCount() throws Exception {
        storm3j.netPeerCount().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthProtocolVersion() throws Exception {
        storm3j.fstProtocolVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_protocolVersion\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthSyncing() throws Exception {
        storm3j.fstSyncing().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_syncing\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthCoinbase() throws Exception {
        storm3j.fstCoinbase().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_coinbase\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthMining() throws Exception {
        storm3j.fstMining().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_mining\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthHashrate() throws Exception {
        storm3j.fstHashrate().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_hashrate\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthGasPrice() throws Exception {
        storm3j.fstGasPrice().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_gasPrice\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthAccounts() throws Exception {
        storm3j.fstAccounts().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_accounts\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthBlockNumber() throws Exception {
        storm3j.fstBlockNumber().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthGetBalance() throws Exception {
        storm3j.fstGetBalance(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBalance\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetStorageAt() throws Exception {
        storm3j.fstGetStorageAt(
                        "0x295a70b2de5e3953354a6a8344e616ed314d7251",
                        BigInteger.ZERO,
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getStorageAt\","
                        + "\"params\":[\"0x295a70b2de5e3953354a6a8344e616ed314d7251\",\"0x0\",\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetTransactionCount() throws Exception {
        storm3j.fstGetTransactionCount(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionCount\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetBlockTransactionCountByHash() throws Exception {
        storm3j.fstGetBlockTransactionCountByHash(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockTransactionCountByHash\",\"params\":[\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],\"id\":1}");
    }

    @Test
    public void testEthGetBlockTransactionCountByNumber() throws Exception {
        storm3j.fstGetBlockTransactionCountByNumber(
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockTransactionCountByNumber\","
                        + "\"params\":[\"0xe8\"],\"id\":1}");
    }

    @Test
    public void testEthGetUncleCountByBlockHash() throws Exception {
        storm3j.fstGetUncleCountByBlockHash(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleCountByBlockHash\",\"params\":[\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],\"id\":1}");
    }

    @Test
    public void testEthGetUncleCountByBlockNumber() throws Exception {
        storm3j.fstGetUncleCountByBlockNumber(DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleCountByBlockNumber\","
                        + "\"params\":[\"0xe8\"],\"id\":1}");
    }

    @Test
    public void testEthGetCode() throws Exception {
        storm3j.fstGetCode(
                        "0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b",
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0x2")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getCode\","
                        + "\"params\":[\"0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b\",\"0x2\"],\"id\":1}");
    }

    @Test
    public void testEthSign() throws Exception {
        storm3j.fstSign(
                        "0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab",
                        "0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sign\","
                        + "\"params\":[\"0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab\","
                        + "\"0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthSendTransaction() throws Exception {
        storm3j.fstSendTransaction(
                        new Transaction(
                                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                                BigInteger.ONE,
                                Numeric.toBigInt("0x9184e72a000"),
                                Numeric.toBigInt("0x76c0"),
                                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                                Numeric.toBigInt("0x9184e72a"),
                                "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb"
                                        + "970870f072445675058bb8eb970870f072445675"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"to\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"gas\":\"0x76c0\",\"gasPrice\":\"0x9184e72a000\",\"value\":\"0x9184e72a\",\"data\":\"0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675\",\"nonce\":\"0x1\"}],\"id\":1}");
    }

    @Test
    public void testEthSendRawTransaction() throws Exception {
        storm3j.fstSendRawTransaction(
                        "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f"
                                + "072445675058bb8eb970870f072445675")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendRawTransaction\",\"params\":[\"0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675\"],\"id\":1}");
    }

    @Test
    public void testEthCall() throws Exception {
        storm3j.fstCall(
                        Transaction.createFstCallTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                                "0x0"),
                        DefaultBlockParameter.valueOf("latest"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\","
                        + "\"params\":[{\"from\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\","
                        + "\"to\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"data\":\"0x0\"},"
                        + "\"latest\"],\"id\":1}");
    }

    @Test
    public void testEthEstimateGas() throws Exception {
        storm3j.fstEstimateGas(
                        Transaction.createFstCallTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                "0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f",
                                "0x0"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_estimateGas\","
                        + "\"params\":[{\"from\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\","
                        + "\"to\":\"0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f\",\"data\":\"0x0\"}],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthEstimateGasContractCreation() throws Exception {
        storm3j.fstEstimateGas(
                        Transaction.createContractTransaction(
                                "0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f",
                                BigInteger.ONE,
                                BigInteger.TEN,
                                ""))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_estimateGas\","
                        + "\"params\":[{\"from\":\"0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f\","
                        + "\"gasPrice\":\"0xa\",\"data\":\"0x\",\"nonce\":\"0x1\"}],\"id\":1}");
    }

    @Test
    public void testEthGetBlockByHash() throws Exception {
        storm3j.fstGetBlockByHash(
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331", true)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByHash\",\"params\":["
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\""
                        + ",true],\"id\":1}");
    }

    @Test
    public void testEthGetBlockByNumber() throws Exception {
        storm3j.fstGetBlockByNumber(DefaultBlockParameter.valueOf(Numeric.toBigInt("0x1b4")), true)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByNumber\","
                        + "\"params\":[\"0x1b4\",true],\"id\":1}");
    }

    @Test
    public void testEthGetTransactionByHash() throws Exception {
        storm3j.fstGetTransactionByHash(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionByHash\",\"params\":["
                        + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetTransactionByBlockHashAndIndex() throws Exception {
        storm3j.fstGetTransactionByBlockHashAndIndex(
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionByBlockHashAndIndex\",\"params\":[\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\"0x0\"],\"id\":1}");
    }

    @Test
    public void testEthGetTransactionByBlockNumberAndIndex() throws Exception {
        storm3j.fstGetTransactionByBlockNumberAndIndex(
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0x29c")), BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionByBlockNumberAndIndex\","
                        + "\"params\":[\"0x29c\",\"0x0\"],\"id\":1}");
    }

    @Test
    public void testEthGetTransactionReceipt() throws Exception {
        storm3j.fstGetTransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":["
                        + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetUncleByBlockHashAndIndex() throws Exception {
        storm3j.fstGetUncleByBlockHashAndIndex(
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleByBlockHashAndIndex\","
                        + "\"params\":["
                        + "\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\"0x0\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetUncleByBlockNumberAndIndex() throws Exception {
        storm3j.fstGetUncleByBlockNumberAndIndex(
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0x29c")), BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleByBlockNumberAndIndex\","
                        + "\"params\":[\"0x29c\",\"0x0\"],\"id\":1}");
    }

    @Test
    public void testEthGetCompilers() throws Exception {
        storm3j.fstGetCompilers().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getCompilers\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthCompileSolidity() throws Exception {
        storm3j.fstCompileSolidity(
                        "contract test { function multiply(uint a) returns(uint d) {   return a * 7;   } }")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileSolidity\","
                        + "\"params\":[\"contract test { function multiply(uint a) returns(uint d) {"
                        + "   return a * 7;   } }\"],\"id\":1}");
    }

    @Test
    public void testEthCompileLLL() throws Exception {
        storm3j.fstCompileLLL("(returnlll (suicide (caller)))").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileLLL\","
                        + "\"params\":[\"(returnlll (suicide (caller)))\"],\"id\":1}");
    }

    @Test
    public void testEthCompileSerpent() throws Exception {
        storm3j.fstCompileSerpent("/* some serpent */").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileSerpent\","
                        + "\"params\":[\"/* some serpent */\"],\"id\":1}");
    }

    @Test
    public void testEthNewFilter() throws Exception {
        FstFilter ethFilter = new FstFilter().addSingleTopic("0x12341234");

        storm3j.fstNewFilter(ethFilter).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_newFilter\","
                        + "\"params\":[{\"topics\":[\"0x12341234\"]}],\"id\":1}");
    }

    @Test
    public void testEthNewBlockFilter() throws Exception {
        storm3j.fstNewBlockFilter().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_newBlockFilter\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthNewPendingTransactionFilter() throws Exception {
        storm3j.fstNewPendingTransactionFilter().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_newPendingTransactionFilter\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthUninstallFilter() throws Exception {
        storm3j.fstUninstallFilter(Numeric.toBigInt("0xb")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_uninstallFilter\","
                        + "\"params\":[\"0x0b\"],\"id\":1}");
    }

    @Test
    public void testEthGetFilterChanges() throws Exception {
        storm3j.fstGetFilterChanges(Numeric.toBigInt("0x16")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getFilterChanges\","
                        + "\"params\":[\"0x16\"],\"id\":1}");
    }

    @Test
    public void testEthGetFilterLogs() throws Exception {
        storm3j.fstGetFilterLogs(Numeric.toBigInt("0x16")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getFilterLogs\","
                        + "\"params\":[\"0x16\"],\"id\":1}");
    }

    @Test
    public void testEthGetLogs() throws Exception {
        storm3j.fstGetLogs(
                        new FstFilter()
                                .addSingleTopic(
                                        "0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\","
                        + "\"params\":[{\"topics\":["
                        + "\"0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b\"]}],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthGetLogsWithNumericBlockRange() throws Exception {
        storm3j.fstGetLogs(
                        new FstFilter(
                                DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")),
                                DefaultBlockParameter.valueOf("latest"),
                                ""))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\","
                        + "\"params\":[{\"topics\":[],\"fromBlock\":\"0xe8\","
                        + "\"toBlock\":\"latest\",\"address\":[\"\"]}],\"id\":1}");
    }

    @Test
    public void testEthGetWork() throws Exception {
        storm3j.fstGetWork().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_getWork\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testEthSubmitWork() throws Exception {
        storm3j.fstSubmitWork(
                        "0x0000000000000001",
                        "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
                        "0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_submitWork\","
                        + "\"params\":[\"0x0000000000000001\","
                        + "\"0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef\","
                        + "\"0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testEthSubmitHashRate() throws Exception {
        storm3j.fstSubmitHashrate(
                        "0x0000000000000000000000000000000000000000000000000000000000500000",
                        "0x59daa26581d0acd1fce254fb7e85952f4c09d0915afd33d3886cd914bc7d283c")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_submitHashrate\","
                        + "\"params\":["
                        + "\"0x0000000000000000000000000000000000000000000000000000000000500000\","
                        + "\"0x59daa26581d0acd1fce254fb7e85952f4c09d0915afd33d3886cd914bc7d283c\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testDbPutString() throws Exception {
        storm3j.dbPutString("testDB", "myKey", "myString").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_putString\","
                        + "\"params\":[\"testDB\",\"myKey\",\"myString\"],\"id\":1}");
    }

    @Test
    public void testDbGetString() throws Exception {
        storm3j.dbGetString("testDB", "myKey").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_getString\","
                        + "\"params\":[\"testDB\",\"myKey\"],\"id\":1}");
    }

    @Test
    public void testDbPutHex() throws Exception {
        storm3j.dbPutHex("testDB", "myKey", "0x68656c6c6f20776f726c64").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_putHex\","
                        + "\"params\":[\"testDB\",\"myKey\",\"0x68656c6c6f20776f726c64\"],\"id\":1}");
    }

    @Test
    public void testDbGetHex() throws Exception {
        storm3j.dbGetHex("testDB", "myKey").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_getHex\","
                        + "\"params\":[\"testDB\",\"myKey\"],\"id\":1}");
    }

    @Test
    public void testShhVersion() throws Exception {
        storm3j.shhVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_version\"," + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testShhPost() throws Exception {

        storm3j.shhPost(
                        new ShhPost(
                                "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1",
                                "0x3e245533f97284d442460f2998cd41858798ddf04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a0d4d661997d3940272b717b1",
                                Arrays.asList(
                                        "0x776869737065722d636861742d636c69656e74",
                                        "0x4d5a695276454c39425154466b61693532"),
                                "0x7b2274797065223a226d6",
                                Numeric.toBigInt("0x64"),
                                Numeric.toBigInt("0x64")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_post\",\"params\":[{\"from\":\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\",\"to\":\"0x3e245533f97284d442460f2998cd41858798ddf04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a0d4d661997d3940272b717b1\",\"topics\":[\"0x776869737065722d636861742d636c69656e74\",\"0x4d5a695276454c39425154466b61693532\"],\"payload\":\"0x7b2274797065223a226d6\",\"priority\":\"0x64\",\"ttl\":\"0x64\"}],\"id\":1}");
    }

    @Test
    public void testShhNewIdentity() throws Exception {
        storm3j.shhNewIdentity().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"shh_newIdentity\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testShhHasIdentity() throws Exception {

        storm3j.shhHasIdentity(
                        "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_hasIdentity\",\"params\":[\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\"],\"id\":1}");
    }

    @Test
    public void testShhNewGroup() throws Exception {
        storm3j.shhNewGroup().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"shh_newGroup\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testShhAddToGroup() throws Exception {

        storm3j.shhAddToGroup(
                        "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_addToGroup\",\"params\":[\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\"],\"id\":1}");
    }

    @Test
    public void testShhNewFilter() throws Exception {

        storm3j.shhNewFilter(
                        new ShhFilter(
                                        "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1")
                                .addSingleTopic("0x12341234bf4b564f"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_newFilter\",\"params\":[{\"topics\":[\"0x12341234bf4b564f\"],\"to\":\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\"}],\"id\":1}");
    }

    @Test
    public void testShhUninstallFilter() throws Exception {
        storm3j.shhUninstallFilter(Numeric.toBigInt("0x7")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_uninstallFilter\","
                        + "\"params\":[\"0x07\"],\"id\":1}");
    }

    @Test
    public void testShhGetFilterChanges() throws Exception {
        storm3j.shhGetFilterChanges(Numeric.toBigInt("0x7")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_getFilterChanges\","
                        + "\"params\":[\"0x07\"],\"id\":1}");
    }

    @Test
    public void testShhGetMessages() throws Exception {
        storm3j.shhGetMessages(Numeric.toBigInt("0x7")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_getMessages\","
                        + "\"params\":[\"0x07\"],\"id\":1}");
    }
}
