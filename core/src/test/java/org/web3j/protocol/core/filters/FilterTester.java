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
package org.storm3j.protocol.core.filters;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.junit.Before;

import org.storm3j.protocol.ObjectMapperFactory;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.storm3jService;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.FstFilter;
import org.storm3j.protocol.core.methods.response.FstLog;
import org.storm3j.protocol.core.methods.response.FstUninstallFilter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class FilterTester {

    private storm3jService storm3jService;
    Storm3j storm3j;

    final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    @Before
    public void setUp() {
        storm3jService = mock(storm3jService.class);
        storm3j = Storm3j.build(storm3jService, 1000, scheduledExecutorService);
    }

    <T> void runTest(FstLog fstLog, Flowable<T> flowable) throws Exception {
        FstFilter fstFilter =
                objectMapper.readValue(
                        "{\n"
                                + "  \"id\":1,\n"
                                + "  \"jsonrpc\": \"2.0\",\n"
                                + "  \"result\": \"0x1\"\n"
                                + "}",
                        FstFilter.class);

        FstUninstallFilter fstUninstallFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", FstUninstallFilter.class);

        FstLog notFoundFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,"
                                + "\"error\":{\"code\":-32000,\"message\":\"filter not found\"}}",
                        FstLog.class);

        @SuppressWarnings("unchecked")
        List<T> expected = createExpected(fstLog);
        Set<T> results = Collections.synchronizedSet(new HashSet<T>());

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());

        CountDownLatch completedLatch = new CountDownLatch(1);

        when(storm3jService.send(any(Request.class), eq(FstFilter.class))).thenReturn(fstFilter);
        when(storm3jService.send(any(Request.class), eq(FstLog.class)))
                .thenReturn(fstLog)
                .thenReturn(notFoundFilter)
                .thenReturn(fstLog);
        when(storm3jService.send(any(Request.class), eq(FstUninstallFilter.class)))
                .thenReturn(fstUninstallFilter);

        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(new HashSet<>(expected)));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    List createExpected(FstLog fstLog) {
        List<FstLog.LogResult> logResults = fstLog.getLogs();
        if (logResults.isEmpty()) {
            fail("Results cannot be empty");
        }

        return fstLog.getLogs().stream().map(t -> t.get()).collect(Collectors.toList());
    }
}
