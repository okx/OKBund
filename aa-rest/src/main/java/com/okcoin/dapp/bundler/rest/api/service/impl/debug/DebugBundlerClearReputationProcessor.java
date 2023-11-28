package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.constant.RestCommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_CLEAR_REPUTATION;


@Service(DEBUG_BUNDLER_CLEAR_REPUTATION)
@Slf4j
public class DebugBundlerClearReputationProcessor implements AAMethodProcessor {

    @Autowired
    private ReputationService reputationService;

    @Override
    public String process(IChain chain, List<Object> params) {
        reputationService.clearState();
        return RestCommonConstant.OK;
    }
}
