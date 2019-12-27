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
package org.storm3j.ens;

import org.junit.Test;

import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.http.HttpService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnsIT {

    @Test
    public void testEns() throws Exception {

        Storm3j storm3j = Storm3j.build(new HttpService());
        EnsResolver ensResolver = new EnsResolver(storm3j);

        assertThat(
                ensResolver.resolve("storm3j.test"),
                is("0x19e03255f667bdfd50a32722df860b1eeaf4d635"));
    }
}
