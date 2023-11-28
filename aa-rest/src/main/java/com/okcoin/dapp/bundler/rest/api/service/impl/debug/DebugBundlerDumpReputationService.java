package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.domain.reputation.ReputationEntryDO;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import com.okcoin.dapp.bundler.rest.api.resp.ReputationEntryVO;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_DUMP_REPUTATION;

@Service(DEBUG_BUNDLER_DUMP_REPUTATION)
@Slf4j
public class DebugBundlerDumpReputationService implements AAMethodProcessor {

    @Autowired
    ReputationService reputationService;

    @Override
    public List<ReputationEntryVO> process(IChain chain, List<Object> params) {
        String entryPointAddress = (String) Iterables.get(params, 0);
        List<ReputationEntryDO> reputationEntryDOS = reputationService.dumpReputation();
        return dealAddress(reputationEntryDOS);
    }

    private List<ReputationEntryVO> dealAddress(List<ReputationEntryDO> reputationEntryDOS) {
        List<ReputationEntryVO> result = Lists.newArrayList();
        for (ReputationEntryDO reputationEntryDO : reputationEntryDOS) {
            ReputationEntryVO entryVO = new ReputationEntryVO();
            entryVO.setAddress(reputationEntryDO.getAddress());
            entryVO.setOpsSeen(Numeric.encodeQuantity(BigInteger.valueOf(reputationEntryDO.getOpsSeen())));
            entryVO.setOpsIncluded(Numeric.encodeQuantity(BigInteger.valueOf(reputationEntryDO.getOpsIncluded())));
            entryVO.setStatus(Numeric.encodeQuantity(BigInteger.valueOf(reputationService.getStatus(reputationEntryDO.getAddress()).getCode())));
            result.add(entryVO);
        }

        return result;
    }
}
