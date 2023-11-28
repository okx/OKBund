package com.okcoin.dapp.bundler.infra.chain.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.web3j.protocol.core.Response;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChainException extends RuntimeException {

    private final int code;

    private final String msg;

    private final String data;

    public ChainException(Response.Error error) {
        super(error.getCode() + "::" + error.getMessage() + (error.getData() == null ? "" : "::" + error.getData()));
        this.code = error.getCode();
        this.msg = error.getMessage();
        this.data = error.getData();
    }
}
