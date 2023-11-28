package com.okcoin.dapp.bundler.infra.chain;

import lombok.SneakyThrows;
import org.web3j.protocol.admin.methods.response.TxPoolContent;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

import java.math.BigInteger;

public class ChainUtil {

    //region getBlockNumber getNonce getGasPrice getNetVersion getChainId

    @SneakyThrows
    public static BigInteger getBlockNumber(IChain chain) {
        EthBlockNumber ethBlockNumber = chain.getWeb3j().ethBlockNumber().send();
        ChainErrorUtil.throwChainError(ethBlockNumber);
        return ethBlockNumber.getBlockNumber();
    }

    @SneakyThrows
    public static String getNetVersion(IChain chain) {
        NetVersion netVersion = chain.getWeb3j().netVersion().send();
        ChainErrorUtil.throwChainError(netVersion);
        return netVersion.getNetVersion();
    }

    @SneakyThrows
    public static BigInteger getChainId(IChain chain) {
        EthChainId ethChainId = chain.getWeb3j().ethChainId().send();
        ChainErrorUtil.throwChainError(ethChainId);
        return ethChainId.getChainId();
    }

    @SneakyThrows
    public static String getWeb3ClientVersion(IChain chain) {
        Web3ClientVersion web3ClientVersion = chain.getWeb3j().web3ClientVersion().send();
        ChainErrorUtil.throwChainError(web3ClientVersion);
        return web3ClientVersion.getWeb3ClientVersion();
    }
    //endregion


    @SneakyThrows
    public static TxPoolContent.TxPoolContentResult getTxPoolContent(IChain chain) {
        TxPoolContent txPoolContent = chain.getWeb3j().txPoolContent().send();
        ChainErrorUtil.throwChainError(txPoolContent);
        return txPoolContent.getResult();
    }

}
