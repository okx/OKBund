package com.okcoin.dapp.bundler.infra.chain.web3j.req;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TracerConfig {

    private boolean onlyTopCall;

}
