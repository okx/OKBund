package com.okcoin.dapp.bundler.rest.api.req;

import lombok.Data;

import java.util.List;

@Data
public class AAReq {

    private String jsonrpc;

    private String method;

    private List<Object> params;

    private Long id;
}
