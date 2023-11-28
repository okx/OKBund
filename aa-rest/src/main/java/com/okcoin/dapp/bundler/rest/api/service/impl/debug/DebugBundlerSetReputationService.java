package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Iterables;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import com.okcoin.dapp.bundler.rest.api.req.ReputationEntryParam;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.constant.RestCommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_SET_REPUTATION;

@Service(DEBUG_BUNDLER_SET_REPUTATION)
@Slf4j
public class DebugBundlerSetReputationService implements AAMethodProcessor {

    @Autowired
    private ReputationService reputationService;

    @Override
    public String process(IChain chain, List<Object> params) {
        String entryPointAddress = (String) Iterables.get(params, 1);
        List<ReputationEntryParam> paramList = JSON.parseArray(JSON.toJSONString(Iterables.get(params, 0)),
                ReputationEntryParam.class);
        for (ReputationEntryParam entryParam : paramList) {
            reputationService.update(StringUtils.lowerCase(entryParam.getAddress()), entryParam.getOpsSeen(),
                    entryParam.getOpsIncluded(),
                    true);
        }
        return RestCommonConstant.OK;
    }
}
