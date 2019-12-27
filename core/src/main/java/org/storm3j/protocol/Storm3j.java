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
package org.storm3j.protocol;

import java.util.concurrent.ScheduledExecutorService;

import org.storm3j.protocol.core.Fst;
import org.storm3j.protocol.core.JsonRpc2_0Storm3j;
import org.storm3j.protocol.rx.Storm3jRx;

/** JSON-RPC Request object building factory. */
public interface Storm3j extends Fst, Storm3jRx {

    /**
     * Construct a new Storm3j instance.
     *
     * @param storm3jService storm3j service instance - i.e. HTTP or IPC
     * @return new Storm3j instance
     */
    static Storm3j build(Storm3jService storm3jService) {
        return new JsonRpc2_0Storm3j(storm3jService);
    }

    /**
     * Construct a new Storm3j instance.
     *
     * @param storm3jService storm3j service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks. <strong>You are
     *     responsible for terminating this thread pool</strong>
     * @return new Storm3j instance
     */
    static Storm3j build(
            Storm3jService storm3jService,
            long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Storm3j(storm3jService, pollingInterval, scheduledExecutorService);
    }

    /** Shutdowns a Storm3j instance and closes opened resources. */
    void shutdown();
}
