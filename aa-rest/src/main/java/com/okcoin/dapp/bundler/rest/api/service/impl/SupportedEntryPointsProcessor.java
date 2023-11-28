package com.okcoin.dapp.bundler.rest.api.service.impl;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.ETH_SUPPORTED_ENTRY_POINTS;


@Service(ETH_SUPPORTED_ENTRY_POINTS)
public class SupportedEntryPointsProcessor implements AAMethodProcessor {

    @Autowired
    private PoolConfig poolConfig;

    public List<String> process(IChain chain, List<Object> params) {

        return Lists.newArrayList(CodecUtil.toChecksumAddress(poolConfig.getEntrypoint()));
    }
}
