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

import java.math.BigInteger;

import org.junit.Test;

import org.storm3j.crypto.RawTransaction;
import org.storm3j.crypto.TransactionEncoder;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.methods.response.FstGetTransactionCount;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.utils.Convert;
import org.storm3j.utils.Numeric;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/** Create, sign and send a raw transaction. */
public class CreateRawTransactionIT extends Scenario {

    @Test
    public void testTransferEther() throws Exception {
        BigInteger nonce = getNonce(ALICE.getAddress());
        RawTransaction rawTransaction = createEtherTransaction(nonce, BOB.getAddress());

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, ALICE);
        String hexValue = Numeric.toHexString(signedMessage);

        FstSendTransaction fstSendTransaction =
                storm3j.fstSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = fstSendTransaction.getTransactionHash();

        assertFalse(transactionHash.isEmpty());

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        assertThat(transactionReceipt.getTransactionHash(), is(transactionHash));
    }

    @Test
    public void testDeploySmartContract() throws Exception {
        BigInteger nonce = getNonce(ALICE.getAddress());
        RawTransaction rawTransaction = createSmartContractTransaction(nonce);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, ALICE);
        String hexValue = Numeric.toHexString(signedMessage);

        FstSendTransaction fstSendTransaction =
                storm3j.fstSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = fstSendTransaction.getTransactionHash();

        assertFalse(transactionHash.isEmpty());

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        assertThat(transactionReceipt.getTransactionHash(), is(transactionHash));

        assertFalse(
                "Contract execution ran out of gas",
                rawTransaction.getGasLimit().equals(transactionReceipt.getGasUsed()));
    }

    private static RawTransaction createEtherTransaction(BigInteger nonce, String toAddress) {
        BigInteger value = Convert.toWei("0.5", Convert.Unit.ETHER).toBigInteger();

        return RawTransaction.createEtherTransaction(nonce, GAS_PRICE, GAS_LIMIT, toAddress, value);
    }

    private static RawTransaction createSmartContractTransaction(BigInteger nonce)
            throws Exception {
        return RawTransaction.createContractTransaction(
                nonce, GAS_PRICE, GAS_LIMIT, BigInteger.ZERO, getFibonacciSolidityBinary());
    }

    BigInteger getNonce(String address) throws Exception {
        FstGetTransactionCount fstGetTransactionCount =
                storm3j.fstGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                        .sendAsync()
                        .get();

        return fstGetTransactionCount.getTransactionCount();
    }
}
