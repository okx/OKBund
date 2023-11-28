package com.okcoin.dapp.bundler.rest.gas;

import lombok.Data;

import java.math.BigInteger;

@Data
public class UserOperationGasDO {

    private BigInteger preVerificationGas;

    private BigInteger verificationGasLimit;

    private BigInteger callGasLimit;

    private long validAfter;

    private long validUntil;

    public UserOperationGasDO(BigInteger preVerificationGas, BigInteger verificationGasLimit, BigInteger callGasLimit,
                              long validAfter, long validUntil) {
        this.preVerificationGas = preVerificationGas;
        this.verificationGasLimit = verificationGasLimit;
        this.callGasLimit = callGasLimit;
        this.validAfter = validAfter;
        this.validUntil = validUntil;
    }
}
