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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.DefaultBlockParameter;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.DefaultBlockParameterNumber;
import org.storm3j.protocol.core.filters.BlockFilter;
import org.storm3j.protocol.core.filters.LogFilter;
import org.storm3j.protocol.core.filters.PendingTransactionFilter;
import org.storm3j.protocol.core.methods.request.FstFilter;
import org.storm3j.protocol.core.methods.response.FstBlock;
import org.storm3j.protocol.core.methods.response.Log;
import org.storm3j.protocol.core.methods.response.Transaction;
import org.storm3j.utils.Flowables;

/** storm3j reactive API implementation. */
public class JsonRpc2_0Rx {

    private final Storm3j storm3j;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Scheduler scheduler;

    public JsonRpc2_0Rx(Storm3j storm3j, ScheduledExecutorService scheduledExecutorService) {
        this.storm3j = storm3j;
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduler = Schedulers.from(scheduledExecutorService);
    }

    public Flowable<String> fstBlockHashFlowable(long pollingInterval) {
        return Flowable.create(
                subscriber -> {
                    BlockFilter blockFilter = new BlockFilter(storm3j, subscriber::onNext);
                    run(blockFilter, subscriber, pollingInterval);
                },
                BackpressureStrategy.BUFFER);
    }

    public Flowable<String> fstPendingTransactionHashFlowable(long pollingInterval) {
        return Flowable.create(
                subscriber -> {
                    PendingTransactionFilter pendingTransactionFilter =
                            new PendingTransactionFilter(storm3j, subscriber::onNext);

                    run(pendingTransactionFilter, subscriber, pollingInterval);
                },
                BackpressureStrategy.BUFFER);
    }

    public Flowable<Log> fstLogFlowable(
            FstFilter fstFilter, long pollingInterval) {
        return Flowable.create(
                subscriber -> {
                    LogFilter logFilter = new LogFilter(storm3j, subscriber::onNext, fstFilter);

                    run(logFilter, subscriber, pollingInterval);
                },
                BackpressureStrategy.BUFFER);
    }

    private <T> void run(
            org.storm3j.protocol.core.filters.Filter<T> filter,
            FlowableEmitter<? super T> emitter,
            long pollingInterval) {

        filter.run(scheduledExecutorService, pollingInterval);
        emitter.setCancellable(filter::cancel);
    }

    public Flowable<Transaction> transactionFlowable(long pollingInterval) {
        return blockFlowable(true, pollingInterval).flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<Transaction> pendingTransactionFlowable(long pollingInterval) {
        return fstPendingTransactionHashFlowable(pollingInterval)
                .flatMap(
                        transactionHash ->
                                storm3j.fstGetTransactionByHash(transactionHash).flowable())
                .filter(ethTransaction -> ethTransaction.getTransaction().isPresent())
                .map(ethTransaction -> ethTransaction.getTransaction().get());
    }

    public Flowable<FstBlock> blockFlowable(boolean fullTransactionObjects, long pollingInterval) {
        return fstBlockHashFlowable(pollingInterval)
                .flatMap(
                        blockHash ->
                                storm3j.fstGetBlockByHash(blockHash, fullTransactionObjects)
                                        .flowable());
    }

    public Flowable<FstBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects, true);
    }

    public Flowable<FstBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending) {
        // We use a scheduler to ensure this Flowable runs asynchronously for users to be
        // consistent with the other Flowables
        return replayBlocksFlowableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                .subscribeOn(scheduler);
    }

    private Flowable<FstBlock> replayBlocksFlowableSync(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksFlowableSync(startBlock, endBlock, fullTransactionObjects, true);
    }

    private Flowable<FstBlock> replayBlocksFlowableSync(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending) {

        BigInteger startBlockNumber = null;
        BigInteger endBlockNumber = null;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            endBlockNumber = getBlockNumber(endBlock);
        } catch (IOException e) {
            Flowable.error(e);
        }

        if (ascending) {
            return Flowables.range(startBlockNumber, endBlockNumber)
                    .flatMap(
                            i ->
                                    storm3j.fstGetBlockByNumber(
                                                    new DefaultBlockParameterNumber(i),
                                                    fullTransactionObjects)
                                            .flowable());
        } else {
            return Flowables.range(startBlockNumber, endBlockNumber, false)
                    .flatMap(
                            i ->
                                    storm3j.fstGetBlockByNumber(
                                                    new DefaultBlockParameterNumber(i),
                                                    fullTransactionObjects)
                                            .flowable());
        }
    }

    public Flowable<Transaction> replayTransactionsFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return replayBlocksFlowable(startBlock, endBlock, true)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<FstBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<FstBlock> onCompleteFlowable) {
        // We use a scheduler to ensure this Flowable runs asynchronously for users to be
        // consistent with the other Flowables
        return replayPastBlocksFlowableSync(startBlock, fullTransactionObjects, onCompleteFlowable)
                .subscribeOn(scheduler);
    }

    public Flowable<FstBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return replayPastBlocksFlowable(startBlock, fullTransactionObjects, Flowable.empty());
    }

    private Flowable<FstBlock> replayPastBlocksFlowableSync(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<FstBlock> onCompleteFlowable) {

        BigInteger startBlockNumber;
        BigInteger latestBlockNumber;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            latestBlockNumber = getLatestBlockNumber();
        } catch (IOException e) {
            return Flowable.error(e);
        }

        if (startBlockNumber.compareTo(latestBlockNumber) > -1) {
            return onCompleteFlowable;
        } else {
            return Flowable.concat(
                    replayBlocksFlowableSync(
                            new DefaultBlockParameterNumber(startBlockNumber),
                            new DefaultBlockParameterNumber(latestBlockNumber),
                            fullTransactionObjects),
                    Flowable.defer(
                            () ->
                                    replayPastBlocksFlowableSync(
                                            new DefaultBlockParameterNumber(
                                                    latestBlockNumber.add(BigInteger.ONE)),
                                            fullTransactionObjects,
                                            onCompleteFlowable)));
        }
    }

    public Flowable<Transaction> replayPastTransactionsFlowable(DefaultBlockParameter startBlock) {
        return replayPastBlocksFlowable(startBlock, true, Flowable.empty())
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<FstBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            long pollingInterval) {

        return replayPastBlocksFlowable(
                startBlock,
                fullTransactionObjects,
                blockFlowable(fullTransactionObjects, pollingInterval));
    }

    public Flowable<Transaction> replayPastAndFutureTransactionsFlowable(
            DefaultBlockParameter startBlock, long pollingInterval) {
        return replayPastAndFutureBlocksFlowable(startBlock, true, pollingInterval)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    private BigInteger getLatestBlockNumber() throws IOException {
        return getBlockNumber(DefaultBlockParameterName.LATEST);
    }

    private BigInteger getBlockNumber(DefaultBlockParameter defaultBlockParameter)
            throws IOException {
        if (defaultBlockParameter instanceof DefaultBlockParameterNumber) {
            return ((DefaultBlockParameterNumber) defaultBlockParameter).getBlockNumber();
        } else {
            FstBlock latestFstBlock =
                    storm3j.fstGetBlockByNumber(defaultBlockParameter, false).send();
            return latestFstBlock.getBlock().getNumber();
        }
    }

    private static List<Transaction> toTransactions(FstBlock fstBlock) {
        // If you ever see an exception thrown here, it's probably due to an incomplete chain in
        // Geth/Parity. You should resync to solve.
        return fstBlock.getBlock().getTransactions().stream()
                .map(transactionResult -> (Transaction) transactionResult.get())
                .collect(Collectors.toList());
    }
}
