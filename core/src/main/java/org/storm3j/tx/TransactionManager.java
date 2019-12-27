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
package org.storm3j.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.DefaultBlockParameter;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.protocol.exceptions.TransactionException;
import org.storm3j.tx.response.PollingTransactionReceiptProcessor;
import org.storm3j.tx.response.TransactionReceiptProcessor;

import static org.storm3j.protocol.core.JsonRpc2_0Storm3j.DEFAULT_BLOCK_TIME;

/**
 * Transaction manager abstraction for executing transactions with Fst client via various
 * mechanisms.
 */
public abstract class TransactionManager {

    public static final int DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH = 40;
    public static final long DEFAULT_POLLING_FREQUENCY = DEFAULT_BLOCK_TIME;

    private final TransactionReceiptProcessor transactionReceiptProcessor;
    private final String fromAddress;

    protected TransactionManager(
            TransactionReceiptProcessor transactionReceiptProcessor, String fromAddress) {
        this.transactionReceiptProcessor = transactionReceiptProcessor;
        this.fromAddress = fromAddress;
    }

    protected TransactionManager(Storm3j storm3j, String fromAddress) {
        this(
                new PollingTransactionReceiptProcessor(
                        storm3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH),
                fromAddress);
    }

    protected TransactionManager(
            Storm3j storm3j, int attempts, long sleepDuration, String fromAddress) {
        this(new PollingTransactionReceiptProcessor(storm3j, sleepDuration, attempts), fromAddress);
    }

    protected TransactionReceipt executeTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value)
            throws IOException, TransactionException {

        return executeTransaction(gasPrice, gasLimit, to, data, value, false);
    }

    protected TransactionReceipt executeTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            String data,
            BigInteger value,
            boolean constructor)
            throws IOException, TransactionException {

        FstSendTransaction fstSendTransaction =
                sendTransaction(gasPrice, gasLimit, to, data, value, constructor);
        return processResponse(fstSendTransaction);
    }

    public FstSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value)
            throws IOException {
        return sendTransaction(gasPrice, gasLimit, to, data, value, false);
    }

    public abstract FstSendTransaction sendTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            String data,
            BigInteger value,
            boolean constructor)
            throws IOException;

    public abstract String sendCall(
            String to, String data, DefaultBlockParameter defaultBlockParameter) throws IOException;

    public String getFromAddress() {
        return fromAddress;
    }

    private TransactionReceipt processResponse(FstSendTransaction transactionResponse)
            throws IOException, TransactionException {
        if (transactionResponse.hasError()) {
            throw new RuntimeException(
                    "Error processing transaction request: "
                            + transactionResponse.getError().getMessage());
        }

        String transactionHash = transactionResponse.getTransactionHash();

        return transactionReceiptProcessor.waitForTransactionReceipt(transactionHash);
    }
}
