package com.okcoin.dapp.bundler.rest.api.resp;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserOperationVO {
    private String sender;
    private String nonce;
    private String initCode;
    private String callData;
    private String callGasLimit;
    private String verificationGasLimit;
    private String preVerificationGas;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String paymasterAndData;
    private String signature;
}
