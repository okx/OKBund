package com.okcoin.dapp.bundler.rest.test;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class TestUserOperationReq {

    private String entryPoint;

    private String account;

    private String accountFactory;

    private String privateKey;

    private String callData;

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint.toLowerCase();
    }

    public void setAccount(String account) {
        this.account = account.toLowerCase();
    }

    public void setAccountFactory(String accountFactory) {
        this.accountFactory = accountFactory.toLowerCase();
    }

    public void setCallData(String callData) {
        this.callData = callData.toLowerCase();
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
