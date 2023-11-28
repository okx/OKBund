package com.okcoin.dapp.bundler.rest.api.req;

import com.alibaba.fastjson2.JSON;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UserOperationParam {

    protected String callData;
    private String sender;
    private String nonce;
    private String initCode;

    private String paymasterAndData;

    private String signature;

    private String callGasLimit = Web3Constant.HEX_ZERO;

    private String verificationGasLimit = Web3Constant.HEX_ZERO;

    private String preVerificationGas = Web3Constant.HEX_ZERO;

    private String maxFeePerGas = Web3Constant.HEX_ZERO;

    private String maxPriorityFeePerGas = Web3Constant.HEX_ZERO;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public void setSender(String sender) {
        this.sender = StringUtils.lowerCase(sender);
    }

    public void setNonce(String nonce) {
        this.nonce = StringUtils.lowerCase(nonce);
    }

    public void setInitCode(String initCode) {
        this.initCode = StringUtils.lowerCase(initCode);
    }

    public void setCallData(String callData) {
        this.callData = StringUtils.lowerCase(callData);
    }

    public void setCallGasLimit(String callGasLimit) {
        this.callGasLimit = StringUtils.lowerCase(callGasLimit);
    }

    public void setVerificationGasLimit(String verificationGasLimit) {
        this.verificationGasLimit = StringUtils.lowerCase(verificationGasLimit);
    }

    public void setPreVerificationGas(String preVerificationGas) {
        this.preVerificationGas = StringUtils.lowerCase(preVerificationGas);
    }

    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = StringUtils.lowerCase(maxFeePerGas);
    }

    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = StringUtils.lowerCase(maxPriorityFeePerGas);
    }

    public void setPaymasterAndData(String paymasterAndData) {
        this.paymasterAndData = StringUtils.lowerCase(paymasterAndData);
    }

    public void setSignature(String signature) {
        this.signature = StringUtils.lowerCase(signature);
    }
}
