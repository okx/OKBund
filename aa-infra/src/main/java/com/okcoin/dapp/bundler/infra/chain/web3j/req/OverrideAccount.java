package com.okcoin.dapp.bundler.infra.chain.web3j.req;

import lombok.Getter;
import lombok.Setter;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@Getter
public class OverrideAccount {

    private String nonce;

    @Setter
    private String code;

    private String balance;

    @Setter
    private String state;

    @Setter
    private String stateDiff;


    public void setNonce(BigInteger nonce) {
        this.nonce = Numeric.encodeQuantity(nonce);
    }

    public void setBalance(BigInteger balance) {
        this.balance = Numeric.encodeQuantity(balance);
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
