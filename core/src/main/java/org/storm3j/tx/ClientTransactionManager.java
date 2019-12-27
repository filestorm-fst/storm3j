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
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;
import org.storm3j.tx.response.TransactionReceiptProcessor;

/**
 * TransactionManager implementation for using an Fst node to transact.
 *
 * <p><b>Note</b>: accounts must be unlocked on the node for transactions to be successful.
 */
public class ClientTransactionManager extends TransactionManager {

    private final Storm3j storm3j;

    public ClientTransactionManager(Storm3j storm3j, String fromAddress) {
        super(storm3j, fromAddress);
        this.storm3j = storm3j;
    }

    public ClientTransactionManager(
            Storm3j storm3j, String fromAddress, int attempts, int sleepDuration) {
        super(storm3j, attempts, sleepDuration, fromAddress);
        this.storm3j = storm3j;
    }

    public ClientTransactionManager(
            Storm3j storm3j,
            String fromAddress,
            TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, fromAddress);
        this.storm3j = storm3j;
    }

    @Override
    public FstSendTransaction sendTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            String data,
            BigInteger value,
            boolean constructor)
            throws IOException {

        Transaction transaction =
                new Transaction(getFromAddress(), null, gasPrice, gasLimit, to, value, data);

        return storm3j.fstSendTransaction(transaction).send();
    }

    @Override
    public String sendCall(String to, String data, DefaultBlockParameter defaultBlockParameter)
            throws IOException {
        return storm3j.fstCall(
                        Transaction.createFstCallTransaction(getFromAddress(), to, data),
                        defaultBlockParameter)
                .send()
                .getValue();
    }
}
