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

import org.junit.Before;

import org.storm3j.crypto.Credentials;
import org.storm3j.crypto.SampleKeys;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.*;
import org.storm3j.protocol.core.methods.response.FstGetTransactionCount;
import org.storm3j.utils.TxHashVerifier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ManagedTransactionTester {

    static final String ADDRESS = "0x3d6cb163f7c72d20b0fcd6baae5889329d138a4a";
    static final String TRANSACTION_HASH = "0xHASH";
    protected Storm3j storm3j;
    protected TxHashVerifier txHashVerifier;

    @Before
    public void setUp() throws Exception {
        storm3j = mock(Storm3j.class);
        txHashVerifier = mock(TxHashVerifier.class);
        when(txHashVerifier.verify(any(), any())).thenReturn(true);
    }

    public TransactionManager getVerifiedTransactionManager(
            Credentials credentials, int attempts, int sleepDuration) {
        RawTransactionManager transactionManager =
                new RawTransactionManager(storm3j, credentials, attempts, sleepDuration);
        transactionManager.setTxHashVerifier(txHashVerifier);
        return transactionManager;
    }

    public TransactionManager getVerifiedTransactionManager(Credentials credentials) {
        RawTransactionManager transactionManager = new RawTransactionManager(storm3j, credentials);
        transactionManager.setTxHashVerifier(txHashVerifier);
        return transactionManager;
    }

    void prepareTransaction(TransactionReceipt transactionReceipt) throws IOException {
        prepareNonceRequest();
        prepareTransactionRequest();
        prepareTransactionReceipt(transactionReceipt);
    }

    @SuppressWarnings("unchecked")
    void prepareNonceRequest() throws IOException {
        FstGetTransactionCount fstGetTransactionCount = new FstGetTransactionCount();
        fstGetTransactionCount.setResult("0x1");

        Request<?, FstGetTransactionCount> transactionCountRequest = mock(Request.class);
        when(transactionCountRequest.send()).thenReturn(fstGetTransactionCount);
        when(storm3j.fstGetTransactionCount(SampleKeys.ADDRESS, DefaultBlockParameterName.PENDING))
                .thenReturn((Request) transactionCountRequest);
    }

    @SuppressWarnings("unchecked")
    void prepareTransactionRequest() throws IOException {
        FstSendTransaction fstSendTransaction = new FstSendTransaction();
        fstSendTransaction.setResult(TRANSACTION_HASH);

        Request<?, FstSendTransaction> rawTransactionRequest = mock(Request.class);
        when(rawTransactionRequest.send()).thenReturn(fstSendTransaction);
        when(storm3j.fstSendRawTransaction(any(String.class)))
                .thenReturn((Request) rawTransactionRequest);
    }

    @SuppressWarnings("unchecked")
    void prepareTransactionReceipt(TransactionReceipt transactionReceipt) throws IOException {
        FstGetTransactionReceipt fstGetTransactionReceipt = new FstGetTransactionReceipt();
        fstGetTransactionReceipt.setResult(transactionReceipt);

        Request<?, FstGetTransactionReceipt> getTransactionReceiptRequest = mock(Request.class);
        when(getTransactionReceiptRequest.send()).thenReturn(fstGetTransactionReceipt);
        when(storm3j.fstGetTransactionReceipt(TRANSACTION_HASH))
                .thenReturn((Request) getTransactionReceiptRequest);
    }

    @SuppressWarnings("unchecked")
    protected TransactionReceipt prepareTransfer() throws IOException {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setStatus("0x1");
        prepareTransaction(transactionReceipt);

        FstGasPrice fstGasPrice = new FstGasPrice();
        fstGasPrice.setResult("0x1");

        Request<?, FstGasPrice> gasPriceRequest = mock(Request.class);
        when(gasPriceRequest.send()).thenReturn(fstGasPrice);
        when(storm3j.fstGasPrice()).thenReturn((Request) gasPriceRequest);

        return transactionReceipt;
    }
}
