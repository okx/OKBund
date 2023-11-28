package com.okcoin.dapp.bundler.rest.api.resp;

import lombok.Data;


@Data
public class AAResp<E> {

    private long id;

    private String jsonrpc;

    private E result;

    private AAError error;
}
