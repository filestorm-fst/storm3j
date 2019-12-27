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
package org.storm3j.protocol.scenarios;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.storm3j.protocol.core.RemoteCall;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.tx.FastRawTransactionManager;
import org.storm3j.tx.Transfer;
import org.storm3j.tx.response.Callback;
import org.storm3j.tx.response.PollingTransactionReceiptProcessor;
import org.storm3j.tx.response.QueuingTransactionReceiptProcessor;
import org.storm3j.utils.Convert;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.storm3j.tx.TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH;

public class FastRawTransactionManagerIT extends Scenario {

    private static final int COUNT = 10; // don't set too high if using a real Fst network
    private static final long POLLING_FREQUENCY = 15000;

    @Rule public TestRule benchmarkRun = new BenchmarkRule();

    @Test
    public void testTransactionPolling() throws Exception {

        List<Future<TransactionReceipt>> transactionReceipts = new LinkedList<>();
        FastRawTransactionManager transactionManager =
                new FastRawTransactionManager(
                        storm3j,
                        ALICE,
                        new PollingTransactionReceiptProcessor(
                                storm3j, POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH));

        Transfer transfer = new Transfer(storm3j, transactionManager);
        BigInteger gasPrice = transfer.requestCurrentGasPrice();

        for (int i = 0; i < COUNT; i++) {

            Future<TransactionReceipt> transactionReceiptFuture =
                    createTransaction(transfer, gasPrice).sendAsync();
            transactionReceipts.add(transactionReceiptFuture);
        }

        for (int i = 0;
                i < DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH && !transactionReceipts.isEmpty();
                i++) {

            for (Iterator<Future<TransactionReceipt>> iterator = transactionReceipts.iterator();
                    iterator.hasNext(); ) {
                Future<TransactionReceipt> transactionReceiptFuture = iterator.next();

                if (transactionReceiptFuture.isDone()) {
                    TransactionReceipt transactionReceipt = transactionReceiptFuture.get();
                    assertFalse(transactionReceipt.getBlockHash().isEmpty());
                    iterator.remove();
                }
            }

            Thread.sleep(POLLING_FREQUENCY);
        }

        assertTrue(transactionReceipts.isEmpty());
    }

    @Test
    public void testTransactionQueuing() throws Exception {

        Map<String, Object> pendingTransactions = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<TransactionReceipt> transactionReceipts =
                new ConcurrentLinkedQueue<>();

        FastRawTransactionManager transactionManager =
                new FastRawTransactionManager(
                        storm3j,
                        ALICE,
                        new QueuingTransactionReceiptProcessor(
                                storm3j,
                                new Callback() {
                                    @Override
                                    public void accept(TransactionReceipt transactionReceipt) {
                                        transactionReceipts.add(transactionReceipt);
                                    }

                                    @Override
                                    public void exception(Exception exception) {}
                                },
                                DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH,
                                POLLING_FREQUENCY));

        Transfer transfer = new Transfer(storm3j, transactionManager);

        BigInteger gasPrice = transfer.requestCurrentGasPrice();

        for (int i = 0; i < COUNT; i++) {
            TransactionReceipt transactionReceipt = createTransaction(transfer, gasPrice).send();
            pendingTransactions.put(transactionReceipt.getTransactionHash(), new Object());
        }

        for (int i = 0;
                i < DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH && !pendingTransactions.isEmpty();
                i++) {
            for (TransactionReceipt transactionReceipt : transactionReceipts) {
                assertFalse(transactionReceipt.getBlockHash().isEmpty());
                pendingTransactions.remove(transactionReceipt.getTransactionHash());
                transactionReceipts.remove(transactionReceipt);
            }

            Thread.sleep(POLLING_FREQUENCY);
        }

        assertTrue(transactionReceipts.isEmpty());
    }

    private RemoteCall<TransactionReceipt> createTransaction(
            Transfer transfer, BigInteger gasPrice) {
        return transfer.sendFunds(
                BOB.getAddress(),
                BigDecimal.valueOf(1.0),
                Convert.Unit.KWEI,
                gasPrice,
                Transfer.GAS_LIMIT);
    }
}