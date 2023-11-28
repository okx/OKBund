package com.okcoin.dapp.bundler.rest.api.resp;

import lombok.Data;


@Data
public class AAError {

    private int code;

    private String message;

    private Object data;

    public AAError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
