package com.okcoin.dapp.bundler.infra.chain;

import lombok.SneakyThrows;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class AccountUtil {

    @SneakyThrows
    public static BigInteger getNonce(String address, boolean isLatest, IChain chain) {
        EthGetTransactionCount ethGetTransactionCount = chain.getWeb3j().ethGetTransactionCount(address, isLatest ? DefaultBlockParameterName.LATEST : DefaultBlockParameterName.PENDING).send();
        ChainErrorUtil.throwChainError(ethGetTransactionCount);
        return ethGetTransactionCount.getTransactionCount();
    }

    @SneakyThrows
    public static BigInteger getBalance(String address, IChain chain) {
        EthGetBalance ethGetBalance = chain.getWeb3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        ChainErrorUtil.throwChainError(ethGetBalance);
        return ethGetBalance.getBalance();
    }

    public static BigDecimal getBalance(String address, Convert.Unit unit, IChain chain) {
        BigInteger balance = getBalance(address, chain);
        return Convert.fromWei(new BigDecimal(balance), unit);
    }

    @SneakyThrows
    public static String getCode(String address, IChain chain) {
        EthGetCode ethGetCode = chain.getWeb3j().ethGetCode(address, DefaultBlockParameterName.LATEST).send();
        ChainErrorUtil.throwChainError(ethGetCode);
        return ethGetCode.getCode();
    }
}
