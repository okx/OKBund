package com.okcoin.dapp.bundler.rest.api.service.impl;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.ETH_CHAIN_ID;


@Service(ETH_CHAIN_ID)
public class ChainIdProcessor implements AAMethodProcessor {

    @Autowired
    private PoolConfig poolConfig;

    public String process(IChain chain, List<Object> params) {
        return Numeric.encodeQuantity(BigInteger.valueOf(poolConfig.getChainId()));
    }
}
