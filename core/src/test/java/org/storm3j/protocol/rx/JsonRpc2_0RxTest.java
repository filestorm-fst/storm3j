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
package org.storm3j.protocol.rx;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import org.storm3j.protocol.ObjectMapperFactory;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.core.DefaultBlockParameterNumber;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.*;
import org.storm3j.protocol.core.methods.response.FstBlock;
import org.storm3j.utils.Numeric;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonRpc2_0RxTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private Storm3j storm3j;

    private Storm3jService storm3jService;

    @Before
    public void setUp() {
        storm3jService = mock(Storm3jService.class);
        storm3j = Storm3j.build(storm3jService, 1000, Executors.newSingleThreadScheduledExecutor());
    }

    @Test
    public void testReplayBlocksFlowable() throws Exception {

        List<FstBlock> fstBlocks = Arrays.asList(createBlock(0), createBlock(1), createBlock(2));

        OngoingStubbing<FstBlock> stubbing =
                when(storm3jService.send(any(Request.class), eq(FstBlock.class)));
        for (FstBlock fstBlock : fstBlocks) {
            stubbing = stubbing.thenReturn(fstBlock);
        }

        Flowable<FstBlock> flowable =
                storm3j.replayPastBlocksFlowable(
                        new DefaultBlockParameterNumber(BigInteger.ZERO),
                        new DefaultBlockParameterNumber(BigInteger.valueOf(2)),
                        false);

        CountDownLatch transactionLatch = new CountDownLatch(fstBlocks.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<FstBlock> results = new ArrayList<>(fstBlocks.size());
        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(fstBlocks));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    @Test
    public void testReplayBlocksDescendingFlowable() throws Exception {

        List<FstBlock> fstBlocks = Arrays.asList(createBlock(2), createBlock(1), createBlock(0));

        OngoingStubbing<FstBlock> stubbing =
                when(storm3jService.send(any(Request.class), eq(FstBlock.class)));
        for (FstBlock fstBlock : fstBlocks) {
            stubbing = stubbing.thenReturn(fstBlock);
        }

        Flowable<FstBlock> flowable =
                storm3j.replayPastBlocksFlowable(
                        new DefaultBlockParameterNumber(BigInteger.ZERO),
                        new DefaultBlockParameterNumber(BigInteger.valueOf(2)),
                        false,
                        false);

        CountDownLatch transactionLatch = new CountDownLatch(fstBlocks.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<FstBlock> results = new ArrayList<>(fstBlocks.size());
        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(fstBlocks));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    @Test
    public void testReplayPastBlocksFlowable() throws Exception {
        List<FstBlock> expected =
                Arrays.asList(
                        createBlock(0),
                        createBlock(1),
                        createBlock(2),
                        createBlock(3),
                        createBlock(4));

        List<FstBlock> fstBlocks =
                Arrays.asList(
                        expected.get(2), // greatest block
                        expected.get(0),
                        expected.get(1),
                        expected.get(2),
                        expected.get(4), // greatest block
                        expected.get(3),
                        expected.get(4),
                        expected.get(4)); // greatest block

        OngoingStubbing<FstBlock> stubbing =
                when(storm3jService.send(any(Request.class), eq(FstBlock.class)));
        for (FstBlock fstBlock : fstBlocks) {
            stubbing = stubbing.thenReturn(fstBlock);
        }

        FstFilter fstFilter =
                objectMapper.readValue(
                        "{\n"
                                + "  \"id\":1,\n"
                                + "  \"jsonrpc\": \"2.0\",\n"
                                + "  \"result\": \"0x1\"\n"
                                + "}",
                        FstFilter.class);
        FstLog fstLog =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":["
                                + "\"0x31c2342b1e0b8ffda1507fbffddf213c4b3c1e819ff6a84b943faabb0ebf2403\""
                                + "]}",
                        FstLog.class);
        FstUninstallFilter fstUninstallFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", FstUninstallFilter.class);

        when(storm3jService.send(any(Request.class), eq(FstFilter.class))).thenReturn(fstFilter);
        when(storm3jService.send(any(Request.class), eq(FstLog.class))).thenReturn(fstLog);
        when(storm3jService.send(any(Request.class), eq(FstUninstallFilter.class)))
                .thenReturn(fstUninstallFilter);

        Flowable<FstBlock> flowable =
                storm3j.replayPastBlocksFlowable(
                        new DefaultBlockParameterNumber(BigInteger.ZERO), false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<FstBlock> results = new ArrayList<>(expected.size());
        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1250, TimeUnit.MILLISECONDS);
        assertThat(results, equalTo(expected));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    public void testReplayPastAndFutureBlocksFlowable() throws Exception {
        List<FstBlock> expected =
                Arrays.asList(
                        createBlock(0),
                        createBlock(1),
                        createBlock(2),
                        createBlock(3),
                        createBlock(4),
                        createBlock(5),
                        createBlock(6));

        List<FstBlock> fstBlocks =
                Arrays.asList(
                        expected.get(2), // greatest block
                        expected.get(0),
                        expected.get(1),
                        expected.get(2),
                        expected.get(4), // greatest block
                        expected.get(3),
                        expected.get(4),
                        expected.get(4), // greatest block
                        expected.get(5), // initial response from ethGetFilterLogs call
                        expected.get(6)); // subsequent block from new block flowable

        OngoingStubbing<FstBlock> stubbing =
                when(storm3jService.send(any(Request.class), eq(FstBlock.class)));
        for (FstBlock fstBlock : fstBlocks) {
            stubbing = stubbing.thenReturn(fstBlock);
        }

        FstFilter fstFilter =
                objectMapper.readValue(
                        "{\n"
                                + "  \"id\":1,\n"
                                + "  \"jsonrpc\": \"2.0\",\n"
                                + "  \"result\": \"0x1\"\n"
                                + "}",
                        FstFilter.class);
        FstLog fstLog =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":["
                                + "\"0x31c2342b1e0b8ffda1507fbffddf213c4b3c1e819ff6a84b943faabb0ebf2403\""
                                + "]}",
                        FstLog.class);
        FstUninstallFilter fstUninstallFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", FstUninstallFilter.class);

        when(storm3jService.send(any(Request.class), eq(FstFilter.class))).thenReturn(fstFilter);
        when(storm3jService.send(any(Request.class), eq(FstLog.class))).thenReturn(fstLog);
        when(storm3jService.send(any(Request.class), eq(FstUninstallFilter.class)))
                .thenReturn(fstUninstallFilter);

        Flowable<FstBlock> flowable =
                storm3j.replayPastAndFutureBlocksFlowable(
                        new DefaultBlockParameterNumber(BigInteger.ZERO), false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<FstBlock> results = new ArrayList<>(expected.size());
        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1250, TimeUnit.MILLISECONDS);
        assertThat(results, equalTo(expected));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    @Test
    public void testReplayTransactionsFlowable() throws Exception {

        List<FstBlock> fstBlocks =
                Arrays.asList(
                        createBlockWithTransactions(
                                0,
                                Arrays.asList(
                                        createTransaction("0x1234"),
                                        createTransaction("0x1235"),
                                        createTransaction("0x1236"))),
                        createBlockWithTransactions(
                                1,
                                Arrays.asList(
                                        createTransaction("0x2234"),
                                        createTransaction("0x2235"),
                                        createTransaction("0x2236"))),
                        createBlockWithTransactions(
                                2,
                                Arrays.asList(
                                        createTransaction("0x3234"), createTransaction("0x3235"))));

        OngoingStubbing<FstBlock> stubbing =
                when(storm3jService.send(any(Request.class), eq(FstBlock.class)));
        for (FstBlock fstBlock : fstBlocks) {
            stubbing = stubbing.thenReturn(fstBlock);
        }

        List<Transaction> expectedTransactions =
                fstBlocks.stream()
                        .flatMap(it -> it.getBlock().getTransactions().stream())
                        .map(it -> (Transaction) it.get())
                        .collect(Collectors.toList());

        Flowable<Transaction> flowable =
                storm3j.replayPastTransactionsFlowable(
                        new DefaultBlockParameterNumber(BigInteger.ZERO),
                        new DefaultBlockParameterNumber(BigInteger.valueOf(2)));

        CountDownLatch transactionLatch = new CountDownLatch(expectedTransactions.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<Transaction> results = new ArrayList<>(expectedTransactions.size());
        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(expectedTransactions));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    private FstBlock createBlock(int number) {
        FstBlock fstBlock = new FstBlock();
        FstBlock.Block block = new FstBlock.Block();
        block.setNumber(Numeric.encodeQuantity(BigInteger.valueOf(number)));

        fstBlock.setResult(block);
        return fstBlock;
    }

    private FstBlock createBlockWithTransactions(int blockNumber, List<Transaction> transactions) {
        FstBlock fstBlock = new FstBlock();
        FstBlock.Block block = new FstBlock.Block();
        block.setNumber(Numeric.encodeQuantity(BigInteger.valueOf(blockNumber)));

        List<FstBlock.TransactionResult> transactionResults =
                transactions.stream()
                        .map(it -> (FstBlock.TransactionResult<Transaction>) () -> it)
                        .collect(Collectors.toList());
        block.setTransactions(transactionResults);

        fstBlock.setResult(block);
        return fstBlock;
    }

    private Transaction createTransaction(String transactionHash) {
        Transaction transaction = new Transaction();
        transaction.setHash(transactionHash);
        return transaction;
    }
}
