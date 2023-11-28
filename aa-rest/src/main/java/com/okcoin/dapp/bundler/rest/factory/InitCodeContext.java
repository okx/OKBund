package com.okcoin.dapp.bundler.rest.factory;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class InitCodeContext {

    private final String factory;

    private final String owner;

    private final BigInteger salt;

    public InitCodeContext(String factory, String owner, BigInteger salt) {
        this.factory = factory;
        this.owner = owner;
        this.salt = salt;
    }
}
