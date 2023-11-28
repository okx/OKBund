package com.okcoin.dapp.bundler.rest.api.resp;

import lombok.Data;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@Data
public class EstimateUserOperationGasVO {

    private String preVerificationGas;

    private String verificationGasLimit;

    private String callGasLimit;

    private String validAfter;

    private String validUntil;

    public EstimateUserOperationGasVO(BigInteger preVerificationGas, BigInteger verificationGasLimit, BigInteger callGasLimit, long validAfter, long validUntil) {
        this.preVerificationGas = Numeric.encodeQuantity(preVerificationGas);
        this.verificationGasLimit = Numeric.encodeQuantity(verificationGasLimit);
        this.callGasLimit = Numeric.encodeQuantity(callGasLimit);
        this.validAfter = Numeric.encodeQuantity(BigInteger.valueOf(validAfter));
        this.validUntil = Numeric.encodeQuantity(BigInteger.valueOf(validUntil));
    }
}
