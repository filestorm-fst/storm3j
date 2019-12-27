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

import org.junit.Test;

import org.storm3j.protocol.storm3j;
import org.storm3j.protocol.storm3jService;
import org.storm3j.protocol.core.DefaultBlockParameter;
import org.storm3j.protocol.core.methods.response.FstCall;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadonlyTransactionManagerTest {

    storm3jService service = mock(storm3jService.class);
    storm3j storm3j = storm3j.build(service);
    DefaultBlockParameter defaultBlockParameter = mock(DefaultBlockParameter.class);
    FstCall response = mock(FstCall.class);

    @Test
    public void sendCallTest() throws IOException {
        when(response.getValue()).thenReturn("test");
        when(service.send(any(), any())).thenReturn(response);
        ReadonlyTransactionManager readonlyTransactionManager =
                new ReadonlyTransactionManager(storm3j, "");
        String value = readonlyTransactionManager.sendCall("", "", defaultBlockParameter);
        assertThat(value, is("test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSendTransaction() throws IOException {
        ReadonlyTransactionManager readonlyTransactionManager =
                new ReadonlyTransactionManager(storm3j, "");
        readonlyTransactionManager.sendTransaction(
                BigInteger.ZERO, BigInteger.ZERO, "", "", BigInteger.ZERO);
    }
}
