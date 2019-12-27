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
import org.storm3j.protocol.core.methods.response.Log;

/** Log filter handler. */
public class LogFilter extends Filter<Log> {

    private final org.storm3j.protocol.core.methods.request.FstFilter fstFilter;

    public LogFilter(
            Storm3j storm3j,
            Callback<Log> callback,
            org.storm3j.protocol.core.methods.request.FstFilter fstFilter) {
        super(storm3j, callback);
        this.fstFilter = fstFilter;
    }

    @Override
    FstFilter sendRequest() throws IOException {
        return storm3j.fstNewFilter(fstFilter).send();
    }

    @Override
    void process(List<FstLog.LogResult> logResults) {
        for (FstLog.LogResult logResult : logResults) {
            if (logResult instanceof FstLog.LogObject) {
                Log log = ((FstLog.LogObject) logResult).get();
                callback.onEvent(log);
            } else {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + " required LogObject");
            }
        }
    }

    @Override
    protected Optional<Request<?, FstLog>> getFilterLogs(BigInteger filterId) {
        return Optional.of(storm3j.fstGetFilterLogs(filterId));
    }
}
