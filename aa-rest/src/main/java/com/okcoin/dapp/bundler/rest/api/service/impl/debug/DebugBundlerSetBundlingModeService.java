package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.google.common.collect.Iterables;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.bundler.ExecutionService;
import com.okcoin.dapp.bundler.pool.exception.UnexpectedException;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.constant.BundlingModeEnum;
import com.okcoin.dapp.bundler.rest.constant.RestCommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_SET_BUNDLING_MODE;

@Service(DEBUG_BUNDLER_SET_BUNDLING_MODE)
@Slf4j
public class DebugBundlerSetBundlingModeService implements AAMethodProcessor {

    @Autowired
    private ExecutionService executionService;

    @Override
    public String process(IChain chain, List<Object> params) {
        String mode = (String) Iterables.get(params, 0);
        BundlingModeEnum bundlingMode = BundlingModeEnum.resolveBy(mode);
        if (bundlingMode == null) {
            throw new UnexpectedException("must specify interval <number>|manual|auto");
        }

        if (bundlingMode == BundlingModeEnum.AUTO) {
            executionService.setAutoBundler(0, 0);
        } else {
            // TODO YUKINO 2023/10/30: 1000？
            executionService.setAutoBundler(0, 1000);
        }
        return RestCommonConstant.OK;
    }
}
