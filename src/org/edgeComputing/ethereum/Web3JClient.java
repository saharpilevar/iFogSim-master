package org.edgeComputing.ethereum;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.math.BigInteger;

import java.util.List;


import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;
import org.web3j.crypto.Credentials;
import org.edgeComputing.ethereum.AuctionManager;
import org.web3j.tx.gas.DefaultGasProvider;

public class Web3JClient {
    public static Web3jService service = new HttpService("http://localhost:8545"); // put fullnode url here
    public static Web3j web3j = Web3j.build(service);
    public Credentials cred;
    public String contractAddress = "0xd31d3e1F60552ba8B35aA3Bd17c949404fdd12c4";
    public Web3JClient() {
        this.cred = Credentials.create(
            "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80"
        );
    }
    public AuctionManager getContract() {
        return AuctionManager.load(
            this.contractAddress, 
            this.web3j, 
            this.cred, 
            new DefaultGasProvider()
        );
    }
    public static BigInteger GetLastBlockNumber()
    {
        try {
            EthBlockNumber result = web3j.ethBlockNumber().sendAsync().get();
            return result.getBlockNumber();
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
    // test function here
    }
}
