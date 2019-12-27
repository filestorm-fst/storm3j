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

import org.junit.Before;
import org.junit.Test;

import org.storm3j.protocol.storm3j;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.Response;
import org.storm3j.protocol.core.methods.response.FstGetTransactionReceipt;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.protocol.exceptions.TransactionException;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PollingTransactionReceiptProcessorTest {
    private static final String TRANSACTION_HASH = "0x00";
    private storm3j storm3j;
    private long sleepDuration;
    private int attempts;
    private PollingTransactionReceiptProcessor processor;

    @Before
    public void setUp() {
        storm3j = mock(storm3j.class);
        sleepDuration = 100;
        attempts = 3;
        processor = new PollingTransactionReceiptProcessor(storm3j, sleepDuration, attempts);
    }

    @Test
    public void returnsTransactionReceiptWhenItIsAvailableInstantly() throws Exception {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        doReturn(requestReturning(response(transactionReceipt)))
                .when(storm3j)
                .ethGetTransactionReceipt(TRANSACTION_HASH);

        TransactionReceipt receipt = processor.waitForTransactionReceipt(TRANSACTION_HASH);

        assertThat(receipt, sameInstance(transactionReceipt));
    }

    @Test
    public void throwsTransactionExceptionWhenReceiptIsNotAvailableInTime() throws Exception {
        doReturn(requestReturning(response(null)))
                .when(storm3j)
                .ethGetTransactionReceipt(TRANSACTION_HASH);

        try {
            processor.waitForTransactionReceipt(TRANSACTION_HASH);
            fail("call should fail with TransactionException");
        } catch (TransactionException e) {
            assertTrue(e.getTransactionHash().isPresent());
            assertEquals(e.getTransactionHash().get(), TRANSACTION_HASH);
        }
    }

    private static <T extends Response<?>> Request<String, T> requestReturning(T response) {
        Request<String, T> request = mock(Request.class);
        try {
            when(request.send()).thenReturn(response);
        } catch (IOException e) {
            // this will never happen
        }
        return request;
    }

    private static FstGetTransactionReceipt response(TransactionReceipt transactionReceipt) {
        FstGetTransactionReceipt response = new FstGetTransactionReceipt();
        response.setResult(transactionReceipt);
        return response;
    }
}
