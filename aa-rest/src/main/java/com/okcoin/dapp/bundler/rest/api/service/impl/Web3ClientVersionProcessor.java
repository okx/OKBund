package com.okcoin.dapp.bundler.rest.api.service.impl;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.WEB3_CLIENT_VERSION;


@Service(WEB3_CLIENT_VERSION)
public class Web3ClientVersionProcessor implements AAMethodProcessor {

    @Autowired
    private PoolConfig poolConfig;

    public String process(IChain chain, List<Object> params) {
        //'aa-bundler/' + erc4337RuntimeVersion + (this.config.unsafe ? '/unsafe' : '')
        return "'aa-bundler/erc4337RuntimeVersion" + (poolConfig.isSafeMode() ? "/unsafe" : "");
    }
}
