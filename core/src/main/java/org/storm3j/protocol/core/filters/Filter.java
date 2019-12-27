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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.Response;
import org.storm3j.protocol.core.Response.Error;
import org.storm3j.protocol.core.RpcErrors;
import org.storm3j.protocol.core.methods.response.FstFilter;
import org.storm3j.protocol.core.methods.response.FstLog;
import org.storm3j.protocol.core.methods.response.FstUninstallFilter;

/** Class for creating managed filter requests with callbacks. */
public abstract class Filter<T> {

    private static final Logger log = LoggerFactory.getLogger(Filter.class);

    final Storm3j storm3j;
    final Callback<T> callback;

    private volatile BigInteger filterId;

    private ScheduledFuture<?> schedule;

    private ScheduledExecutorService scheduledExecutorService;

    private long blockTime;

    public Filter(Storm3j storm3j, Callback<T> callback) {
        this.storm3j = storm3j;
        this.callback = callback;
    }

    public void run(ScheduledExecutorService scheduledExecutorService, long blockTime) {
        try {
            FstFilter fstFilter = sendRequest();
            if (fstFilter.hasError()) {
                throwException(fstFilter.getError());
            }

            filterId = fstFilter.getFilterId();
            this.scheduledExecutorService = scheduledExecutorService;
            this.blockTime = blockTime;
            // this runs in the caller thread as if any exceptions are encountered, we shouldn't
            // proceed with creating the scheduled task below
            getInitialFilterLogs();

            /*
            We want the filter to be resilient against client issues. On numerous occasions
            users have reported socket timeout exceptions when connected over HTTP to Geth and
            Parity clients. For examples, refer to
            https://github.com/storm3j/storm3j/issues/144 and
            https://github.com/ethereum/go-ethereum/issues/15243.

            Hence we consume errors and log them as errors, allowing our polling for changes to
            resume. The downside of this approach is that users will not be notified of
            downstream connection issues. But given the intermittent nature of the connection
            issues, this seems like a reasonable compromise.

            The alternative approach would be to have another thread that blocks waiting on
            schedule.get(), catching any Exceptions thrown, and passing them back up to the
            caller. However, the user would then be required to recreate subscriptions manually
            which isn't ideal given the aforementioned issues.
            */
            schedule =
                    scheduledExecutorService.scheduleAtFixedRate(
                            () -> {
                                try {
                                    this.pollFilter(fstFilter);
                                } catch (Throwable e) {
                                    // All exceptions must be caught, otherwise our job terminates
                                    // without
                                    // any notification
                                    log.error("Error sending request", e);
                                }
                            },
                            0,
                            blockTime,
                            TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throwException(e);
        }
    }

    private void getInitialFilterLogs() {
        try {
            Optional<Request<?, FstLog>> maybeRequest = this.getFilterLogs(this.filterId);
            FstLog fstLog = null;
            if (maybeRequest.isPresent()) {
                fstLog = maybeRequest.get().send();
            } else {
                fstLog = new FstLog();
                fstLog.setResult(Collections.emptyList());
            }
            process(fstLog.getLogs());

        } catch (IOException e) {
            throwException(e);
        }
    }

    private void pollFilter(FstFilter fstFilter) {
        FstLog fstLog = null;
        try {
            fstLog = storm3j.fstGetFilterChanges(filterId).send();
        } catch (IOException e) {
            throwException(e);
        }
        if (fstLog.hasError()) {
            Error error = fstLog.getError();
            switch (error.getCode()) {
                case RpcErrors.FILTER_NOT_FOUND:
                    reinstallFilter();
                    break;
                default:
                    throwException(error);
                    break;
            }
        } else {
            process(fstLog.getLogs());
        }
    }

    abstract FstFilter sendRequest() throws IOException;

    abstract void process(List<FstLog.LogResult> logResults);

    private void reinstallFilter() {
        log.warn("The filter has not been found. Filter id: " + filterId);
        schedule.cancel(true);
        this.run(scheduledExecutorService, blockTime);
    }

    public void cancel() {
        schedule.cancel(false);

        try {
            FstUninstallFilter fstUninstallFilter = storm3j.fstUninstallFilter(filterId).send();
            if (fstUninstallFilter.hasError()) {
                throwException(fstUninstallFilter.getError());
            }

            if (!fstUninstallFilter.isUninstalled()) {
                throw new FilterException("Filter with id '" + filterId + "' failed to uninstall");
            }
        } catch (IOException e) {
            throwException(e);
        }
    }

    /**
     * Retrieves historic filters for the filter with the given id. Getting historic logs is not
     * supported by all filters. If not the method should return an empty FstLog object
     *
     * @param filterId Id of the filter for which the historic log should be retrieved
     * @return Historic logs, or an empty optional if the filter cannot retrieve historic logs
     */
    protected abstract Optional<Request<?, FstLog>> getFilterLogs(BigInteger filterId);

    void throwException(Response.Error error) {
        throw new FilterException(
                "Invalid request: " + (error == null ? "Unknown Error" : error.getMessage()));
    }

    void throwException(Throwable cause) {
        throw new FilterException("Error sending request", cause);
    }
}
