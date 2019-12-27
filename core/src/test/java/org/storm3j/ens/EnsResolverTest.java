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

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import org.storm3j.abi.TypeEncoder;
import org.storm3j.abi.datatypes.Utf8String;
import org.storm3j.protocol.storm3j;
import org.storm3j.protocol.storm3jService;
import org.storm3j.protocol.core.Request;
import org.storm3j.protocol.core.methods.response.FstBlock;
import org.storm3j.protocol.core.methods.response.FstCall;
import org.storm3j.protocol.core.methods.response.FstSyncing;
import org.storm3j.protocol.core.methods.response.NetVersion;
import org.storm3j.tx.ChainIdLong;
import org.storm3j.utils.Numeric;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.storm3j.ens.EnsResolver.DEFAULT_SYNC_THRESHOLD;
import static org.storm3j.ens.EnsResolver.isValidEnsName;

public class EnsResolverTest {

    private storm3j storm3j;
    private storm3jService storm3jService;
    private EnsResolver ensResolver;

    @Before
    public void setUp() {
        storm3jService = mock(storm3jService.class);
        storm3j = storm3j.build(storm3jService);
        ensResolver = new EnsResolver(storm3j);
    }

    @Test
    public void testResolve() throws Exception {
        configureSyncing(false);
        configureLatestBlock(System.currentTimeMillis() / 1000); // block timestamp is in seconds

        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));

        String resolverAddress =
                "0x0000000000000000000000004c641fb9bad9b60ef180c31f56051ce826d21a9a";
        String contractAddress =
                "0x00000000000000000000000019e03255f667bdfd50a32722df860b1eeaf4d635";

        FstCall resolverAddressResponse = new FstCall();
        resolverAddressResponse.setResult(resolverAddress);

        FstCall contractAddressResponse = new FstCall();
        contractAddressResponse.setResult(contractAddress);

        when(storm3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);
        when(storm3jService.send(any(Request.class), eq(FstCall.class)))
                .thenReturn(resolverAddressResponse);
        when(storm3jService.send(any(Request.class), eq(FstCall.class)))
                .thenReturn(contractAddressResponse);

        assertThat(
                ensResolver.resolve("storm3j.eth"), is("0x19e03255f667bdfd50a32722df860b1eeaf4d635"));
    }

    @Test
    public void testReverseResolve() throws Exception {
        configureSyncing(false);
        configureLatestBlock(System.currentTimeMillis() / 1000); // block timestamp is in seconds

        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));

        String resolverAddress =
                "0x0000000000000000000000004c641fb9bad9b60ef180c31f56051ce826d21a9a";
        String contractName =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + TypeEncoder.encode(new Utf8String("storm3j.eth"));
        System.err.println(contractName);

        FstCall resolverAddressResponse = new FstCall();
        resolverAddressResponse.setResult(resolverAddress);

        FstCall contractNameResponse = new FstCall();
        contractNameResponse.setResult(contractName);

        when(storm3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);
        when(storm3jService.send(any(Request.class), eq(FstCall.class)))
                .thenReturn(resolverAddressResponse);
        when(storm3jService.send(any(Request.class), eq(FstCall.class)))
                .thenReturn(contractNameResponse);

        assertThat(
                ensResolver.reverseResolve("0x19e03255f667bdfd50a32722df860b1eeaf4d635"),
                is("storm3j.eth"));
    }

    @Test
    public void testIsSyncedSyncing() throws Exception {
        configureSyncing(true);

        assertFalse(ensResolver.isSynced());
    }

    @Test
    public void testIsSyncedFullySynced() throws Exception {
        configureSyncing(false);
        configureLatestBlock(System.currentTimeMillis() / 1000); // block timestamp is in seconds

        assertTrue(ensResolver.isSynced());
    }

    @Test
    public void testIsSyncedBelowThreshold() throws Exception {
        configureSyncing(false);
        configureLatestBlock((System.currentTimeMillis() / 1000) - DEFAULT_SYNC_THRESHOLD);

        assertFalse(ensResolver.isSynced());
    }

    private void configureSyncing(boolean isSyncing) throws IOException {
        FstSyncing fstSyncing = new FstSyncing();
        FstSyncing.Result result = new FstSyncing.Result();
        result.setSyncing(isSyncing);
        fstSyncing.setResult(result);

        when(storm3jService.send(any(Request.class), eq(FstSyncing.class))).thenReturn(fstSyncing);
    }

    private void configureLatestBlock(long timestamp) throws IOException {
        FstBlock.Block block = new FstBlock.Block();
        block.setTimestamp(Numeric.encodeQuantity(BigInteger.valueOf(timestamp)));
        FstBlock fstBlock = new FstBlock();
        fstBlock.setResult(block);

        when(storm3jService.send(any(Request.class), eq(FstBlock.class))).thenReturn(fstBlock);
    }

    @Test
    public void testIsEnsName() {
        assertTrue(isValidEnsName("eth"));
        assertTrue(isValidEnsName("web3.eth"));
        assertTrue(isValidEnsName("0x19e03255f667bdfd50a32722df860b1eeaf4d635.eth"));

        assertFalse(isValidEnsName("0x19e03255f667bdfd50a32722df860b1eeaf4d635"));
        assertFalse(isValidEnsName("19e03255f667bdfd50a32722df860b1eeaf4d635"));

        assertTrue(isValidEnsName(""));
    }
}
