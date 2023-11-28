package com.okcoin.dapp.bundler.manager;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class TransactionGas {

    private final BigInteger gasLimit;
    private final BigInteger maxFeePerGas;
    private final BigInteger maxPriorityFeePerGas;

    public TransactionGas(BigInteger gasLimit, BigInteger maxFeePerGas, BigInteger maxPriorityFeePerGas) {
        this.gasLimit = gasLimit;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }
}
