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

import org.storm3j.crypto.Keys;
import org.storm3j.crypto.WalletUtils;
import org.storm3j.ens.contracts.generated.ENS;
import org.storm3j.ens.contracts.generated.PublicResolver;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.DefaultBlockParameterName;
import org.storm3j.protocol.core.methods.response.FstBlock;
import org.storm3j.protocol.core.methods.response.FstSyncing;
import org.storm3j.protocol.core.methods.response.NetVersion;
import org.storm3j.tx.ClientTransactionManager;
import org.storm3j.tx.TransactionManager;
import org.storm3j.tx.gas.DefaultGasProvider;
import org.storm3j.utils.Numeric;

/** Resolution logic for contract addresses. */
public class EnsResolver {

    public static final long DEFAULT_SYNC_THRESHOLD = 1000 * 60 * 3;
    public static final String REVERSE_NAME_SUFFIX = ".addr.reverse";

    private final Storm3j storm3j;
    private final int addressLength;
    private final TransactionManager transactionManager;
    private long syncThreshold; // non-final in case this value needs to be tweaked

    public EnsResolver(Storm3j storm3j, long syncThreshold, int addressLength) {
        this.storm3j = storm3j;
        transactionManager = new ClientTransactionManager(storm3j, null); // don't use empty string
        this.syncThreshold = syncThreshold;
        this.addressLength = addressLength;
    }

    public EnsResolver(Storm3j storm3j, long syncThreshold) {
        this(storm3j, syncThreshold, Keys.ADDRESS_LENGTH_IN_HEX);
    }

    public EnsResolver(Storm3j storm3j) {
        this(storm3j, DEFAULT_SYNC_THRESHOLD);
    }

    public void setSyncThreshold(long syncThreshold) {
        this.syncThreshold = syncThreshold;
    }

    public long getSyncThreshold() {
        return syncThreshold;
    }

    /**
     * Provides an access to a valid public resolver in order to access other API methods.
     *
     * @param ensName our user input ENS name
     * @return PublicResolver
     */
    protected PublicResolver obtainPublicResolver(String ensName) {
        if (isValidEnsName(ensName, addressLength)) {
            try {
                if (!isSynced()) {
                    throw new EnsResolutionException("Node is not currently synced");
                } else {
                    return lookupResolver(ensName);
                }
            } catch (Exception e) {
                throw new EnsResolutionException("Unable to determine sync status of node", e);
            }

        } else {
            throw new EnsResolutionException("EnsName is invalid: " + ensName);
        }
    }

    public String resolve(String contractId) {
        if (isValidEnsName(contractId, addressLength)) {
            PublicResolver resolver = obtainPublicResolver(contractId);

            byte[] nameHash = NameHash.nameHashAsBytes(contractId);
            String contractAddress = null;
            try {
                contractAddress = resolver.addr(nameHash).send();
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute Fst request", e);
            }

            if (!WalletUtils.isValidAddress(contractAddress)) {
                throw new RuntimeException("Unable to resolve address for name: " + contractId);
            } else {
                return contractAddress;
            }
        } else {
            return contractId;
        }
    }

    /**
     * Reverse name resolution as documented in the <a
     * href="https://docs.ens.domains/en/latest/userguide.html#reverse-name-resolution">specification</a>.
     *
     * @param address an ethereum address, example: "0x314159265dd8dbb310642f98f50c066173c1259b"
     * @return a EnsName registered for provided address
     */
    public String reverseResolve(String address) {
        if (WalletUtils.isValidAddress(address, addressLength)) {
            String reverseName = Numeric.cleanHexPrefix(address) + REVERSE_NAME_SUFFIX;
            PublicResolver resolver = obtainPublicResolver(reverseName);

            byte[] nameHash = NameHash.nameHashAsBytes(reverseName);
            String name;
            try {
                name = resolver.name(nameHash).send();
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute Fst request", e);
            }

            if (!isValidEnsName(name, addressLength)) {
                throw new RuntimeException("Unable to resolve name for address: " + address);
            } else {
                return name;
            }
        } else {
            throw new EnsResolutionException("Address is invalid: " + address);
        }
    }

    private PublicResolver lookupResolver(String ensName) throws Exception {
        NetVersion netVersion = storm3j.netVersion().send();
        String registryContract = Contracts.resolveRegistryContract(netVersion.getNetVersion());

        ENS ensRegistry =
                ENS.load(
                        registryContract,
                        storm3j,
                        transactionManager,
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT);

        byte[] nameHash = NameHash.nameHashAsBytes(ensName);
        String resolverAddress = ensRegistry.resolver(nameHash).send();

        return PublicResolver.load(
                resolverAddress,
                storm3j,
                transactionManager,
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT);
    }

    boolean isSynced() throws Exception {
        FstSyncing fstSyncing = storm3j.fstSyncing().send();
        if (fstSyncing.isSyncing()) {
            return false;
        } else {
            FstBlock fstBlock =
                    storm3j.fstGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
            long timestamp = fstBlock.getBlock().getTimestamp().longValueExact() * 1000;

            return System.currentTimeMillis() - syncThreshold < timestamp;
        }
    }

    public static boolean isValidEnsName(String input) {
        return isValidEnsName(input, Keys.ADDRESS_LENGTH_IN_HEX);
    }

    public static boolean isValidEnsName(String input, int addressLength) {
        return input != null // will be set to null on new Contract creation
                && (input.contains(".") || !WalletUtils.isValidAddress(input, addressLength));
    }
}
