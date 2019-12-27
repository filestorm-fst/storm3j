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

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.protocol.exceptions.TransactionException;

/**
 * Return an {@link EmptyTransactionReceipt} receipt back to callers containing only the transaction
 * hash.
 */
public class NoOpProcessor extends TransactionReceiptProcessor {

    public NoOpProcessor(Storm3j storm3j) {
        super(storm3j);
    }

    @Override
    public TransactionReceipt waitForTransactionReceipt(String transactionHash)
            throws IOException, TransactionException {
        return new EmptyTransactionReceipt(transactionHash);
    }
}
