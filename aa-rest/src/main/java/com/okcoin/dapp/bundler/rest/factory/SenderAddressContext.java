package com.okcoin.dapp.bundler.rest.factory;

import lombok.Getter;

@Getter
public class SenderAddressContext {

    private final String initCode;

    private final String entrypoint;


    public SenderAddressContext(String initCode, String entrypoint) {
        this.initCode = initCode;
        this.entrypoint = entrypoint;
    }
}
