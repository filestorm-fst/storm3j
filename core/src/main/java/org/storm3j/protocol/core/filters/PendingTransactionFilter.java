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
import java.util.List;
import java.util.Optional;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.FstFilter;
import org.storm3j.protocol.core.methods.response.FstLog;

/** Handler for working with transaction filter requests. */
public class PendingTransactionFilter extends Filter<String> {

    public PendingTransactionFilter(Storm3j storm3j, Callback<String> callback) {
        super(storm3j, callback);
    }

    @Override
    FstFilter sendRequest() throws IOException {
        return storm3j.fstNewPendingTransactionFilter().send();
    }

    @Override
    void process(List<FstLog.LogResult> logResults) {
        for (FstLog.LogResult logResult : logResults) {
            if (logResult instanceof FstLog.Hash) {
                String transactionHash = ((FstLog.Hash) logResult).get();
                callback.onEvent(transactionHash);
            } else {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + ", required Hash");
            }
        }
    }

    /**
     * Since the pending transaction filter does not support historic filters, the filterId is
     * ignored and an empty optional is returned
     *
     * @param filterId Id of the filter for which the historic log should be retrieved
     * @return Optional.empty()
     */
    @Override
    protected Optional<Request<?, FstLog>> getFilterLogs(BigInteger filterId) {
        return Optional.empty();
    }
}
