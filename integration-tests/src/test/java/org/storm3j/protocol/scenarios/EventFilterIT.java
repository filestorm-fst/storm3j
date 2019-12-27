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
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.storm3j.abi.EventEncoder;
import org.storm3j.abi.FunctionEncoder;
import org.storm3j.abi.FunctionReturnDecoder;
import org.storm3j.abi.TypeReference;
import org.storm3j.abi.datatypes.Event;
import org.storm3j.abi.datatypes.Function;
import org.storm3j.abi.datatypes.Type;
import org.storm3j.abi.datatypes.generated.Uint256;
import org.storm3j.crypto.Credentials;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.methods.request.FstFilter;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.*;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/** Filter scenario integration tests. */
public class EventFilterIT extends Scenario {

    // Deployed Fibonacci contract instance in testnet
    private static final String CONTRACT_ADDRESS = "0x3c05b2564139fb55820b18b72e94b2178eaace7d";

    @Test
    public void testEventFilter() throws Exception {
        unlockAccount();

        Function function = createFibonacciFunction();
        String encodedFunction = FunctionEncoder.encode(function);

        BigInteger gas = estimateGas(encodedFunction);
        String transactionHash = sendTransaction(ALICE, CONTRACT_ADDRESS, gas, encodedFunction);

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        assertFalse(
                "Transaction execution ran out of gas",
                gas.equals(transactionReceipt.getGasUsed()));

        List<Log> logs = transactionReceipt.getLogs();
        assertFalse(logs.isEmpty());

        Log log = logs.get(0);

        List<String> topics = log.getTopics();
        assertThat(topics.size(), is(1));

        Event event =
                new Event(
                        "Notify",
                        Arrays.asList(
                                new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));

        // check function signature - we only have a single topic our event signature,
        // there are no indexed parameters in this example
        String encodedEventSignature = EventEncoder.encode(event);
        assertThat(topics.get(0), is(encodedEventSignature));

        // verify our two event parameters
        List<Type> results =
                FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());
        assertThat(
                results,
                equalTo(
                        Arrays.asList(
                                new Uint256(BigInteger.valueOf(7)),
                                new Uint256(BigInteger.valueOf(13)))));

        // finally check it shows up in the event filter
        List<FstLog.LogResult> filterLogs =
                createFilterForEvent(encodedEventSignature, CONTRACT_ADDRESS);
        assertFalse(filterLogs.isEmpty());
    }

    private BigInteger estimateGas(String encodedFunction) throws Exception {
        FstEstimateGas fstEstimateGas =
                storm3j.fstEstimateGas(
                                Transaction.createFstCallTransaction(
                                        ALICE.getAddress(), null, encodedFunction))
                        .sendAsync()
                        .get();
        // this was coming back as 50,000,000 which is > the block gas limit of 4,712,388
        // see eth.getBlock("latest")
        return fstEstimateGas.getAmountUsed().divide(BigInteger.valueOf(100));
    }

    private String sendTransaction(
            Credentials credentials, String contractAddress, BigInteger gas, String encodedFunction)
            throws Exception {
        BigInteger nonce = getNonce(credentials.getAddress());
        Transaction transaction =
                Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        nonce,
                        Transaction.DEFAULT_GAS,
                        gas,
                        contractAddress,
                        encodedFunction);

        FstSendTransaction transactionResponse =
                storm3j.fstSendTransaction(transaction).sendAsync().get();

        assertFalse(transactionResponse.hasError());

        return transactionResponse.getTransactionHash();
    }

    private List<FstLog.LogResult> createFilterForEvent(
            String encodedEventSignature, String contractAddress) throws Exception {
        FstFilter fstFilter =
                new FstFilter(
                        DefaultBlockParameterName.EARLIEST,
                        DefaultBlockParameterName.LATEST,
                        contractAddress);

        fstFilter.addSingleTopic(encodedEventSignature);

        FstLog fstLog = storm3j.fstGetLogs(fstFilter).send();
        return fstLog.getLogs();
    }
}
