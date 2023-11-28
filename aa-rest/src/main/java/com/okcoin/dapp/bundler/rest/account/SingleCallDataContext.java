package com.okcoin.dapp.bundler.rest.account;

import lombok.Getter;

import java.math.BigInteger;


@Getter
public class SingleCallDataContext {

    private final String to;

    private final BigInteger value;

    private final byte[] data;

    public SingleCallDataContext(String to, BigInteger value, byte[] data) {
        this.to = to;
        this.value = value;
        this.data = data;
    }
}
