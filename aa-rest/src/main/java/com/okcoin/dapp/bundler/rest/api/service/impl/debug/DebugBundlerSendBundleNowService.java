package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.bundler.ExecutionService;
import com.okcoin.dapp.bundler.pool.bundler.IBundleService;
import com.okcoin.dapp.bundler.pool.domain.TxAndOpHashMappingDO;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.constant.RestCommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_SEND_BUNDLE_NOW;

@Service(DEBUG_BUNDLER_SEND_BUNDLE_NOW)
@Slf4j
public class DebugBundlerSendBundleNowService implements AAMethodProcessor {

    @Autowired
    private IBundleService bundleService;

    @Autowired
    private ExecutionService executionService;

    @Override
    public Object process(IChain chain, List<Object> params) {
        TxAndOpHashMappingDO result = executionService.attemptBundle(true);
        bundleService.handlePastEvents();
        if (result == null) {
            return RestCommonConstant.OK;
        }
        return result;
    }
}
