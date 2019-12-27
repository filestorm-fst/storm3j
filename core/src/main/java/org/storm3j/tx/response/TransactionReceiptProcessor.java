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
package org.storm3j.tx.response;

import java.io.IOException;
import java.util.Optional;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.methods.response.FstGetTransactionReceipt;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.protocol.exceptions.TransactionException;

/** Abstraction for managing how we wait for transaction receipts to be generated on the network. */
public abstract class TransactionReceiptProcessor {

    private final Storm3j storm3j;

    public TransactionReceiptProcessor(Storm3j storm3j) {
        this.storm3j = storm3j;
    }

    public abstract TransactionReceipt waitForTransactionReceipt(String transactionHash)
            throws IOException, TransactionException;

    Optional<? extends TransactionReceipt> sendTransactionReceiptRequest(String transactionHash)
            throws IOException, TransactionException {
        FstGetTransactionReceipt transactionReceipt =
                storm3j.fstGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError()) {
            throw new TransactionException(
                    "Error processing request: " + transactionReceipt.getError().getMessage());
        }

        return transactionReceipt.getTransactionReceipt();
    }
}
