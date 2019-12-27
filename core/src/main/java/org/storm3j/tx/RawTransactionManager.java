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

import org.storm3j.crypto.Credentials;
import org.storm3j.crypto.Hash;
import org.storm3j.crypto.RawTransaction;
import org.storm3j.crypto.TransactionEncoder;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.DefaultBlockParameter;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.FstGetTransactionCount;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;
import org.storm3j.tx.exceptions.TxHashMismatchException;
import org.storm3j.tx.response.TransactionReceiptProcessor;
import org.storm3j.utils.Numeric;
import org.storm3j.utils.TxHashVerifier;

/**
 * TransactionManager implementation using Fst wallet file to create and sign transactions
 * locally.
 *
 * <p>This transaction manager provides support for specifying the chain id for transactions as per
 * <a href="https://github.com/ethereum/EIPs/issues/155">EIP155</a>, as well as for locally signing
 * RawTransaction instances without broadcasting them.
 */
public class RawTransactionManager extends TransactionManager {

    private final Storm3j storm3j;
    final Credentials credentials;

    private final long chainId;

    protected TxHashVerifier txHashVerifier = new TxHashVerifier();

    public RawTransactionManager(Storm3j storm3j, Credentials credentials, long chainId) {
        super(storm3j, credentials.getAddress());

        this.storm3j = storm3j;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(
            Storm3j storm3j,
            Credentials credentials,
            long chainId,
            TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, credentials.getAddress());

        this.storm3j = storm3j;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(
            Storm3j storm3j, Credentials credentials, long chainId, int attempts, long sleepDuration) {
        super(storm3j, attempts, sleepDuration, credentials.getAddress());

        this.storm3j = storm3j;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(Storm3j storm3j, Credentials credentials) {
        this(storm3j, credentials, ChainId.NONE);
    }

    public RawTransactionManager(
            Storm3j storm3j, Credentials credentials, int attempts, int sleepDuration) {
        this(storm3j, credentials, ChainId.NONE, attempts, sleepDuration);
    }

    protected BigInteger getNonce() throws IOException {
        FstGetTransactionCount fstGetTransactionCount =
                storm3j.fstGetTransactionCount(
                                credentials.getAddress(), DefaultBlockParameterName.PENDING)
                        .send();

        return fstGetTransactionCount.getTransactionCount();
    }

    public TxHashVerifier getTxHashVerifier() {
        return txHashVerifier;
    }

    public void setTxHashVerifier(TxHashVerifier txHashVerifier) {
        this.txHashVerifier = txHashVerifier;
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

        BigInteger nonce = getNonce();

        RawTransaction rawTransaction =
                RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);

        return signAndSend(rawTransaction);
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

    /*
     * @param rawTransaction a RawTransaction istance to be signed
     * @return The transaction signed and encoded without ever broadcasting it
     */
    public String sign(RawTransaction rawTransaction) {

        byte[] signedMessage;

        if (chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        return Numeric.toHexString(signedMessage);
    }

    public FstSendTransaction signAndSend(RawTransaction rawTransaction) throws IOException {
        String hexValue = sign(rawTransaction);
        FstSendTransaction fstSendTransaction = storm3j.fstSendRawTransaction(hexValue).send();

        if (fstSendTransaction != null && !fstSendTransaction.hasError()) {
            String txHashLocal = Hash.sha3(hexValue);
            String txHashRemote = fstSendTransaction.getTransactionHash();
            if (!txHashVerifier.verify(txHashLocal, txHashRemote)) {
                throw new TxHashMismatchException(txHashLocal, txHashRemote);
            }
        }

        return fstSendTransaction;
    }
}
