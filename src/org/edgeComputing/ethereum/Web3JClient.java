package org.edgeComputing.ethereum;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlockNumber; 

import java.math.BigInteger;

import java.util.List;


import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

public class Web3JClient {
    private static Web3jService service = new HttpService("http://localhost:8545"); // put fullnode url here
    private static Web3j web3j = Web3j.build(service);

    public Web3JClient() {
        super()
        this.contract = this.web3j.connecttoContract()
    }

    /* */
    public void registerMobileTuple() {
        
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