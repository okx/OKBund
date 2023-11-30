package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.mem.MempoolService;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.constant.RestCommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_CLEAR_STATE;


@Service(DEBUG_BUNDLER_CLEAR_STATE)
@Slf4j
public class DebugBundlerClearStateService implements AAMethodProcessor {
    @Autowired
    private MempoolService mempoolService;
    @Autowired
    private ReputationService reputationService;

    @Override
    public String process(IChain chain, List<Object> params) {
        mempoolService.clearState();
        reputationService.clearState();
        return RestCommonConstant.OK;
    }
}
