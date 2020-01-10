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
package org.storm3j.console;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.storm3j.crypto.Credentials;
import org.storm3j.crypto.WalletUtils;
import org.storm3j.ens.EnsResolver;
import org.storm3j.protocol.Storm3j;
import org.storm3j.protocol.core.methods.response.TransactionReceipt;
import org.storm3j.protocol.core.methods.response.Storm3ClientVersion;
import org.storm3j.protocol.exceptions.TransactionException;
import org.storm3j.protocol.http.HttpService;
import org.storm3j.protocol.infura.InfuraHttpService;
import org.storm3j.protocol.nodesmith.NodesmithHttpService;
import org.storm3j.tx.Transfer;
import org.storm3j.utils.Convert;

import static org.storm3j.codegen.Console.exitError;

/** Simple class for creating a wallet file. */
public class WalletSendFunds extends WalletManager {

    private static final String USAGE = "send <walletfile> <destination-address>";

    public static void main(String[] args) {
        if (args.length != 2) {
            exitError(USAGE);
        } else {
            new WalletSendFunds().run(args[0], args[1]);
        }
    }

    private void run(String walletFileLocation, String destinationAddress) {
        File walletFile = new File(walletFileLocation);
        Credentials credentials = getCredentials(walletFile);
        console.printf("Wallet for address " + credentials.getAddress() + " loaded\n");

        if (!WalletUtils.isValidAddress(destinationAddress)
                && !EnsResolver.isValidEnsName(destinationAddress)) {
            exitError("Invalid destination address specified");
        }

        Storm3j storm3j = getEthereumClient();

        BigDecimal amountToTransfer = getAmountToTransfer();
        Convert.Unit transferUnit = getTransferUnit();
        BigDecimal amountInWei = Convert.toWei(amountToTransfer, transferUnit);

        confirmTransfer(amountToTransfer, transferUnit, amountInWei, destinationAddress);

        TransactionReceipt transactionReceipt =
                performTransfer(storm3j, destinationAddress, credentials, amountInWei);

        console.printf(
                "Funds have been successfully transferred from %s to %s%n"
                        + "Transaction hash: %s%nMined block number: %s%n",
                credentials.getAddress(),
                destinationAddress,
                transactionReceipt.getTransactionHash(),
                transactionReceipt.getBlockNumber());
    }

    private BigDecimal getAmountToTransfer() {
        String amount =
                console.readLine(
                                "What amound would you like to transfer "
                                        + "(please enter a numeric value): ")
                        .trim();
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            exitError("Invalid amount specified");
        }
        throw new RuntimeException("Application exit failure");
    }

    private Convert.Unit getTransferUnit() {
        String unit =
                console.readLine("Please specify the unit (ether, wei, ...) [ether]: ").trim();

        Convert.Unit transferUnit;
        if (unit.equals("")) {
            transferUnit = Convert.Unit.FST;
        } else {
            transferUnit = Convert.Unit.fromString(unit.toLowerCase());
        }

        return transferUnit;
    }

    private void confirmTransfer(
            BigDecimal amountToTransfer,
            Convert.Unit transferUnit,
            BigDecimal amountInWei,
            String destinationAddress) {

        console.printf(
                "Please confim that you wish to transfer %s %s (%s %s) to address %s%n",
                amountToTransfer.stripTrailingZeros().toPlainString(),
                transferUnit,
                amountInWei.stripTrailingZeros().toPlainString(),
                Convert.Unit.WEI,
                destinationAddress);
        String confirm = console.readLine("Please type 'yes' to proceed: ").trim();
        if (!confirm.toLowerCase().equals("yes")) {
            exitError("OK, some other time perhaps...");
        }
    }

    private TransactionReceipt performTransfer(
            Storm3j storm3j,
            String destinationAddress,
            Credentials credentials,
            BigDecimal amountInWei) {

        console.printf("Commencing transfer (this may take a few minutes) ");
        try {
            Future<TransactionReceipt> future =
                    Transfer.sendFunds(
                                    storm3j,
                                    credentials,
                                    destinationAddress,
                                    amountInWei,
                                    Convert.Unit.WEI)
                            .sendAsync();

            while (!future.isDone()) {
                console.printf(".");
                Thread.sleep(500);
            }
            console.printf("$%n%n");
            return future.get();
        } catch (InterruptedException | ExecutionException | TransactionException | IOException e) {
            exitError("Problem encountered transferring funds: \n" + e.getMessage());
        }
        throw new RuntimeException("Application exit failure");
    }

    private Storm3j getEthereumClient() {
        String clientAddress =
                console.readLine(
                                "Please confirm address of running Fst client you wish to send "
                                        + "the transfer request to ["
                                        + HttpService.DEFAULT_URL
                                        + "]: ")
                        .trim();

        Storm3j storm3j;
        if (clientAddress.equals("")) {
            storm3j = Storm3j.build(new HttpService());
        } else if (clientAddress.contains("infura.io")) {
            storm3j = Storm3j.build(new InfuraHttpService(clientAddress));
        } else if (clientAddress.contains("nodesmith.io")) {
            storm3j = Storm3j.build(new NodesmithHttpService(clientAddress));
        } else {
            storm3j = Storm3j.build(new HttpService(clientAddress));
        }

        try {
            Storm3ClientVersion web3ClientVersion = storm3j.storm3ClientVersion().sendAsync().get();
            if (web3ClientVersion.hasError()) {
                exitError(
                        "Unable to process response from client: " + web3ClientVersion.getError());
            } else {
                console.printf(
                        "Connected successfully to client: %s%n",
                        web3ClientVersion.getStorm3ClientVersion());
                return storm3j;
            }
        } catch (InterruptedException | ExecutionException e) {
            exitError("Problem encountered verifying client: " + e.getMessage());
        }
        throw new RuntimeException("Application exit failure");
    }
}
