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

/** Transaction manager implementation for read-only operations on smart contracts. */
public class ReadonlyTransactionManager extends TransactionManager {
    private final Storm3j storm3j;
    private String fromAddress;

    public ReadonlyTransactionManager(Storm3j storm3j, String fromAddress) {
        super(storm3j, fromAddress);
        this.storm3j = storm3j;
        this.fromAddress = fromAddress;
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
        throw new UnsupportedOperationException(
                "Only read operations are supported by this transaction manager");
    }

    @Override
    public String sendCall(String to, String data, DefaultBlockParameter defaultBlockParameter)
            throws IOException {
        return storm3j.fstCall(
                        Transaction.createFstCallTransaction(fromAddress, to, data),
                        defaultBlockParameter)
                .send()
                .getValue();
    }
}
