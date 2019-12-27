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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.storm3j.abi.EventEncoder;
import org.storm3j.abi.EventValues;
import org.storm3j.abi.FunctionEncoder;
import org.storm3j.abi.TypeReference;
import org.storm3j.abi.datatypes.Address;
import org.storm3j.abi.datatypes.Event;
import org.storm3j.abi.datatypes.Function;
import org.storm3j.abi.datatypes.Type;
import org.storm3j.abi.datatypes.Utf8String;
import org.storm3j.abi.datatypes.generated.Uint256;
import org.storm3j.crypto.Credentials;
import org.storm3j.crypto.SampleKeys;
import org.storm3j.protocol.storm3j;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.RemoteCall;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.Response;
import org.storm3j.protocol.core.methods.request.Transaction;
import org.storm3j.protocol.core.methods.response.*;
import org.storm3j.protocol.core.methods.response.FstCall;
import org.storm3j.protocol.exceptions.TransactionException;
import org.storm3j.tx.gas.ContractGasProvider;
import org.storm3j.tx.gas.DefaultGasProvider;
import org.storm3j.tx.gas.StaticGasProvider;
import org.storm3j.utils.Async;
import org.storm3j.utils.Numeric;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContractTest extends ManagedTransactionTester {

    private static final String TEST_CONTRACT_BINARY = "12345";

    private TestContract contract;

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        super.setUp();

        contract =
                new TestContract(
                        ADDRESS,
                        storm3j,
                        getVerifiedTransactionManager(SampleKeys.CREDENTIALS),
                        new DefaultGasProvider());
    }

    @Test
    public void testGetContractAddress() {
        assertThat(contract.getContractAddress(), is(ADDRESS));
    }

    @Test
    public void testGetContractTransactionReceipt() {
        assertFalse(contract.getTransactionReceipt().isPresent());
    }

    @Test
    public void testDeploy() throws Exception {
        TransactionReceipt transactionReceipt = createTransactionReceipt();
        Contract deployedContract = deployContract(transactionReceipt);

        assertThat(deployedContract.getContractAddress(), is(ADDRESS));
        assertTrue(deployedContract.getTransactionReceipt().isPresent());
        assertThat(deployedContract.getTransactionReceipt().get(), equalTo(transactionReceipt));
    }

    @Test
    public void testContractDeployFails() throws Exception {
        thrown.expect(TransactionException.class);
        thrown.expectMessage(
                "Transaction has failed with status: 0x0. Gas used: 1. (not-enough gas?)");
        TransactionReceipt transactionReceipt = createFailedTransactionReceipt();
        deployContract(transactionReceipt);
    }

    @Test
    public void testContractDeployWithNullStatusSucceeds() throws Exception {
        TransactionReceipt transactionReceipt = createTransactionReceiptWithStatus(null);
        Contract deployedContract = deployContract(transactionReceipt);

        assertThat(deployedContract.getContractAddress(), is(ADDRESS));
        assertTrue(deployedContract.getTransactionReceipt().isPresent());
        assertThat(deployedContract.getTransactionReceipt().get(), equalTo(transactionReceipt));
    }

    @Test
    public void testIsValid() throws Exception {
        prepareEthGetCode(TEST_CONTRACT_BINARY);

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    public void testIsValidSkipMetadata() throws Exception {
        prepareEthGetCode(
                TEST_CONTRACT_BINARY
                        + "a165627a7a72305820"
                        + "a9bc86938894dc250f6ea25dd823d4472fad6087edcda429a3504e3713a9fc880029");

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    public void testIsValidDifferentCode() throws Exception {
        prepareEthGetCode(TEST_CONTRACT_BINARY + "0");

        Contract contract = deployContract(createTransactionReceipt());
        assertFalse(contract.isValid());
    }

    @Test
    public void testIsValidEmptyCode() throws Exception {
        prepareEthGetCode("");

        Contract contract = deployContract(createTransactionReceipt());
        assertFalse(contract.isValid());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIsValidNoBinThrows() throws Exception {
        TransactionManager txManager = mock(TransactionManager.class);
        TestContract contract =
                new TestContract(
                        Contract.BIN_NOT_PROVIDED,
                        ADDRESS,
                        storm3j,
                        txManager,
                        new DefaultGasProvider());
        contract.isValid();
    }

    @Test(expected = RuntimeException.class)
    public void testDeployInvalidContractAddress() throws Throwable {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);

        prepareTransaction(transactionReceipt);

        String encodedConstructor =
                FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Uint256(BigInteger.TEN)));

        try {
            TestContract.deployRemoteCall(
                            TestContract.class,
                            storm3j,
                            SampleKeys.CREDENTIALS,
                            ManagedTransaction.GAS_PRICE,
                            Contract.GAS_LIMIT,
                            "0xcafed00d",
                            encodedConstructor,
                            BigInteger.ZERO)
                    .send();
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testCallSingleValue() throws Exception {
        // Example taken from FunctionReturnDecoderTest

        FstCall fstCall = new FstCall();
        fstCall.setResult(
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000000");
        prepareCall(fstCall);

        assertThat(contract.callSingleValue().send(), equalTo(new Utf8String("")));
    }

    @Test
    public void testCallSingleValueEmpty() throws Exception {
        // Example taken from FunctionReturnDecoderTest

        FstCall fstCall = new FstCall();
        fstCall.setResult("0x");
        prepareCall(fstCall);

        assertNull(contract.callSingleValue().send());
    }

    @Test
    public void testCallMultipleValue() throws Exception {
        FstCall fstCall = new FstCall();
        fstCall.setResult(
                "0x0000000000000000000000000000000000000000000000000000000000000037"
                        + "0000000000000000000000000000000000000000000000000000000000000007");
        prepareCall(fstCall);

        assertThat(
                contract.callMultipleValue().send(),
                equalTo(
                        Arrays.asList(
                                new Uint256(BigInteger.valueOf(55)),
                                new Uint256(BigInteger.valueOf(7)))));
    }

    @Test
    public void testCallMultipleValueEmpty() throws Exception {
        FstCall fstCall = new FstCall();
        fstCall.setResult("0x");
        prepareCall(fstCall);

        assertThat(contract.callMultipleValue().send(), equalTo(emptyList()));
    }

    @SuppressWarnings("unchecked")
    private void prepareCall(FstCall fstCall) throws IOException {
        Request<?, FstCall> request = mock(Request.class);
        when(request.send()).thenReturn(fstCall);

        when(storm3j.ethCall(any(Transaction.class), eq(DefaultBlockParameterName.LATEST)))
                .thenReturn((Request) request);
    }

    @Test
    public void testTransaction() throws Exception {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setStatus("0x1");

        prepareTransaction(transactionReceipt);

        assertThat(
                contract.performTransaction(
                                new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                        .send(),
                is(transactionReceipt));
    }

    @Test
    public void testTransactionFailed() throws Exception {
        thrown.expect(TransactionException.class);
        thrown.expectMessage(
                "Transaction has failed with status: 0x0. Gas used: 1. (not-enough gas?)");

        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setStatus("0x0");
        transactionReceipt.setGasUsed("0x1");

        prepareTransaction(transactionReceipt);
        contract.performTransaction(new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                .send();
    }

    @Test
    public void testProcessEvent() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        Log log = new Log();
        log.setTopics(
                Arrays.asList(
                        // encoded function
                        "0xfceb437c298f40d64702ac26411b2316e79f3c28ffa60edfc891ad4fc8ab82ca",
                        // indexed value
                        "0000000000000000000000003d6cb163f7c72d20b0fcd6baae5889329d138a4a"));
        // non-indexed value
        log.setData("0000000000000000000000000000000000000000000000000000000000000001");

        transactionReceipt.setLogs(Arrays.asList(log));

        EventValues eventValues = contract.processEvent(transactionReceipt).get(0);

        assertThat(
                eventValues.getIndexedValues(),
                equalTo(singletonList(new Address("0x3d6cb163f7c72d20b0fcd6baae5889329d138a4a"))));
        assertThat(
                eventValues.getNonIndexedValues(),
                equalTo(singletonList(new Uint256(BigInteger.ONE))));
    }

    @Test
    public void testProcessEventForLogWithoutTopics() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        final Log log = new Log();
        log.setTopics(Collections.emptyList());
        // non-indexed value
        log.setData("0000000000000000000000000000000000000000000000000000000000000001");
        transactionReceipt.setLogs(Arrays.asList(log));

        final List<EventValues> eventValues = contract.processEvent(transactionReceipt);
        assertTrue("No events expected", eventValues.isEmpty());
    }

    @Test(expected = TransactionException.class)
    public void testTimeout() throws Throwable {
        prepareTransaction(null);

        TransactionManager transactionManager =
                getVerifiedTransactionManager(SampleKeys.CREDENTIALS, 1, 1);

        contract = new TestContract(ADDRESS, storm3j, transactionManager, new DefaultGasProvider());

        testErrorScenario();
    }

    @Test(expected = RuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testInvalidTransactionResponse() throws Throwable {
        prepareNonceRequest();

        FstSendTransaction fstSendTransaction = new FstSendTransaction();
        fstSendTransaction.setError(new Response.Error(1, "Invalid transaction"));

        Request<?, FstSendTransaction> rawTransactionRequest = mock(Request.class);
        when(rawTransactionRequest.sendAsync()).thenReturn(Async.run(() -> fstSendTransaction));
        when(storm3j.ethSendRawTransaction(any(String.class)))
                .thenReturn((Request) rawTransactionRequest);

        testErrorScenario();
    }

    @Test
    public void testSetGetAddresses() throws Exception {
        assertNull(contract.getDeployedAddress("1"));
        contract.setDeployedAddress("1", "0x000000000000add0e00000000000");
        assertNotNull(contract.getDeployedAddress("1"));
        contract.setDeployedAddress("2", "0x000000000000add0e00000000000");
        assertNotNull(contract.getDeployedAddress("2"));
    }

    @Test
    public void testSetGetGasPrice() {
        assertEquals(ManagedTransaction.GAS_PRICE, contract.getGasPrice());
        BigInteger newPrice = ManagedTransaction.GAS_PRICE.multiply(BigInteger.valueOf(2));
        contract.setGasPrice(newPrice);
        assertEquals(newPrice, contract.getGasPrice());
    }

    @Test
    public void testStaticGasProvider() throws IOException, TransactionException {
        StaticGasProvider gasProvider = new StaticGasProvider(BigInteger.TEN, BigInteger.ONE);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.executeTransaction(
                        any(BigInteger.class),
                        any(BigInteger.class),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean()))
                .thenReturn(new TransactionReceipt());

        contract = new TestContract(ADDRESS, storm3j, txManager, gasProvider);

        Function func =
                new Function(
                        "test", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        contract.executeTransaction(func);

        verify(txManager)
                .executeTransaction(
                        eq(BigInteger.TEN),
                        eq(BigInteger.ONE),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean());
    }

    @Test(expected = RuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testInvalidTransactionReceipt() throws Throwable {
        prepareNonceRequest();
        prepareTransactionRequest();

        FstGetTransactionReceipt fstGetTransactionReceipt = new FstGetTransactionReceipt();
        fstGetTransactionReceipt.setError(new Response.Error(1, "Invalid transaction receipt"));

        Request<?, FstGetTransactionReceipt> getTransactionReceiptRequest = mock(Request.class);
        when(getTransactionReceiptRequest.sendAsync())
                .thenReturn(Async.run(() -> fstGetTransactionReceipt));
        when(storm3j.ethGetTransactionReceipt(TRANSACTION_HASH))
                .thenReturn((Request) getTransactionReceiptRequest);

        testErrorScenario();
    }

    @Test
    public void testExtractEventParametersWithLogGivenATransactionReceipt() {

        final java.util.function.Function<String, Event> eventFactory =
                name -> new Event(name, emptyList());

        final BiFunction<Integer, Event, Log> logFactory =
                (logIndex, event) ->
                        new Log(
                                false,
                                "" + logIndex,
                                "0",
                                "0x0",
                                "0x0",
                                "0",
                                "0x" + logIndex,
                                "",
                                "",
                                singletonList(EventEncoder.encode(event)));

        final Event testEvent1 = eventFactory.apply("TestEvent1");
        final Event testEvent2 = eventFactory.apply("TestEvent2");

        final List<Log> logs =
                Arrays.asList(logFactory.apply(0, testEvent1), logFactory.apply(1, testEvent2));

        final TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setLogs(logs);

        final List<Contract.EventValuesWithLog> eventValuesWithLogs1 =
                contract.extractEventParametersWithLog(testEvent1, transactionReceipt);

        assertEquals(eventValuesWithLogs1.size(), 1);
        assertEquals(eventValuesWithLogs1.get(0).getLog(), logs.get(0));

        final List<Contract.EventValuesWithLog> eventValuesWithLogs2 =
                contract.extractEventParametersWithLog(testEvent2, transactionReceipt);

        assertEquals(eventValuesWithLogs2.size(), 1);
        assertEquals(eventValuesWithLogs2.get(0).getLog(), logs.get(1));
    }

    void testErrorScenario() throws Throwable {
        try {
            contract.performTransaction(new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                    .send();
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    private TransactionReceipt createTransactionReceipt() {
        return createTransactionReceiptWithStatus("0x1");
    }

    private TransactionReceipt createFailedTransactionReceipt() {
        return createTransactionReceiptWithStatus("0x0");
    }

    private TransactionReceipt createTransactionReceiptWithStatus(String status) {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setContractAddress(ADDRESS);
        transactionReceipt.setStatus(status);
        transactionReceipt.setGasUsed("0x1");
        return transactionReceipt;
    }

    private Contract deployContract(TransactionReceipt transactionReceipt) throws Exception {

        prepareTransaction(transactionReceipt);

        String encodedConstructor =
                FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Uint256(BigInteger.TEN)));

        return TestContract.deployRemoteCall(
                        TestContract.class,
                        storm3j,
                        getVerifiedTransactionManager(SampleKeys.CREDENTIALS),
                        ManagedTransaction.GAS_PRICE,
                        Contract.GAS_LIMIT,
                        "0xcafed00d",
                        encodedConstructor,
                        BigInteger.ZERO)
                .send();
    }

    @SuppressWarnings("unchecked")
    private void prepareEthGetCode(String binary) throws IOException {
        FstGetCode fstGetCode = new FstGetCode();
        fstGetCode.setResult(Numeric.prependHexPrefix(binary));

        Request<?, FstGetCode> ethGetCodeRequest = mock(Request.class);
        when(ethGetCodeRequest.send()).thenReturn(fstGetCode);
        when(storm3j.ethGetCode(ADDRESS, DefaultBlockParameterName.LATEST))
                .thenReturn((Request) ethGetCodeRequest);
    }

    private static class TestContract extends Contract {
        public TestContract(
                String contractAddress,
                storm3j storm3j,
                Credentials credentials,
                BigInteger gasPrice,
                BigInteger gasLimit) {
            super(TEST_CONTRACT_BINARY, contractAddress, storm3j, credentials, gasPrice, gasLimit);
        }

        public TestContract(
                String contractAddress,
                storm3j storm3j,
                TransactionManager transactionManager,
                ContractGasProvider gasProvider) {
            this(TEST_CONTRACT_BINARY, contractAddress, storm3j, transactionManager, gasProvider);
        }

        public TestContract(
                String binary,
                String contractAddress,
                storm3j storm3j,
                TransactionManager transactionManager,
                ContractGasProvider gasProvider) {
            super(binary, contractAddress, storm3j, transactionManager, gasProvider);
        }

        public RemoteCall<Utf8String> callSingleValue() {
            Function function =
                    new Function(
                            "call",
                            Arrays.<Type>asList(),
                            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
            return executeRemoteCallSingleValueReturn(function);
        }

        public RemoteCall<List<Type>> callMultipleValue()
                throws ExecutionException, InterruptedException {
            Function function =
                    new Function(
                            "call",
                            Arrays.<Type>asList(),
                            Arrays.<TypeReference<?>>asList(
                                    new TypeReference<Uint256>() {},
                                    new TypeReference<Uint256>() {}));
            return executeRemoteCallMultipleValueReturn(function);
        }

        public RemoteCall<TransactionReceipt> performTransaction(Address address, Uint256 amount) {
            Function function =
                    new Function(
                            "approve",
                            Arrays.<Type>asList(address, amount),
                            Collections.<TypeReference<?>>emptyList());
            return executeRemoteCallTransaction(function);
        }

        public List<EventValues> processEvent(TransactionReceipt transactionReceipt) {
            Event event =
                    new Event(
                            "Event",
                            Arrays.<TypeReference<?>>asList(
                                    new TypeReference<Address>(true) {},
                                    new TypeReference<Uint256>() {}));
            return extractEventParameters(event, transactionReceipt);
        }
    }
}