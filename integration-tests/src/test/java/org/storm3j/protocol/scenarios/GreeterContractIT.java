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
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.storm3j.abi.FunctionEncoder;
import org.storm3j.abi.FunctionReturnDecoder;
import org.storm3j.abi.TypeReference;
import org.storm3j.abi.datatypes.Function;
import org.storm3j.abi.datatypes.Type;
import org.storm3j.abi.datatypes.Utf8String;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.FstCall;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration test demonstrating integration with Greeter contract taken from the <a
 * href="https://github.com/ethereum/go-ethereum/wiki/Contract-Tutorial">Contract Tutorial</a> on
 * the Go Fst Wiki.
 */
public class GreeterContractIT extends Scenario {

    private static final String VALUE = "Greetings!";

    @Test
    public void testGreeterContract() throws Exception {
        boolean accountUnlocked = unlockAccount();
        assertTrue(accountUnlocked);

        // create our smart contract
        String createTransactionHash = sendCreateContractTransaction();
        assertFalse(createTransactionHash.isEmpty());

        TransactionReceipt createTransactionReceipt =
                waitForTransactionReceipt(createTransactionHash);

        assertThat(createTransactionReceipt.getTransactionHash(), is(createTransactionHash));

        assertFalse(
                "Contract execution ran out of gas",
                createTransactionReceipt.getGasUsed().equals(GAS_LIMIT));

        String contractAddress = createTransactionReceipt.getContractAddress();

        assertNotNull(contractAddress);

        // call our getter
        Function getFunction = createGreetFunction();
        String responseValue = callSmartContractFunction(getFunction, contractAddress);
        assertFalse(responseValue.isEmpty());

        List<Type> response =
                FunctionReturnDecoder.decode(responseValue, getFunction.getOutputParameters());
        assertThat(response.size(), is(1));
        assertThat(response.get(0).getValue(), is(VALUE));
    }

    private String sendCreateContractTransaction() throws Exception {
        BigInteger nonce = getNonce(ALICE.getAddress());

        String encodedConstructor =
                FunctionEncoder.encodeConstructor(Collections.singletonList(new Utf8String(VALUE)));

        Transaction transaction =
                Transaction.createContractTransaction(
                        ALICE.getAddress(),
                        nonce,
                        GAS_PRICE,
                        GAS_LIMIT,
                        BigInteger.ZERO,
                        getGreeterSolidityBinary() + encodedConstructor);

        FstSendTransaction transactionResponse =
                storm3j.fstSendTransaction(transaction).sendAsync().get();

        return transactionResponse.getTransactionHash();
    }

    private String callSmartContractFunction(Function function, String contractAddress)
            throws Exception {

        String encodedFunction = FunctionEncoder.encode(function);

        FstCall response =
                storm3j.fstCall(
                                Transaction.createFstCallTransaction(
                                        ALICE.getAddress(), contractAddress, encodedFunction),
                                DefaultBlockParameterName.LATEST)
                        .sendAsync()
                        .get();

        return response.getValue();
    }

    private static String getGreeterSolidityBinary() throws Exception {
        return load("/solidity/greeter/build/Greeter.bin");
    }

    Function createGreetFunction() {
        return new Function(
                "greet",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Utf8String>() {}));
    }
}
