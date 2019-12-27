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
package org.storm3j.protocol.eea;

import java.util.Collections;

import org.storm3j.protocol.Storm3jService;
import org.storm3j.protocol.core.JsonRpc2_0Storm3j;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.FstSendTransaction;

public class JsonRpc2_0Eea extends JsonRpc2_0Storm3j implements Eea {
    public JsonRpc2_0Eea(Storm3jService storm3jService) {
        super(storm3jService);
    }

    @Override
    public Request<?, FstSendTransaction> eeaSendRawTransaction(
            final String signedTransactionData) {
        return new Request<>(
                "eea_sendRawTransaction",
                Collections.singletonList(signedTransactionData),
                storm3jService,
                FstSendTransaction.class);
    }
}
