package com.okcoin.dapp.bundler.rest.api.service;


import com.okcoin.dapp.bundler.infra.chain.IChain;

import java.util.List;

public interface AAMethodProcessor {

    Object process(IChain chain, List<Object> params);

}
