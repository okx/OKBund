package com.okcoin.dapp.bundler.infra.chain.web3j.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TraceCallConfig {

    private String tracer;

    private StateOverride stateOverrides;

}
