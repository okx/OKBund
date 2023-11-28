package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.google.common.collect.Iterables;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.config.ReputationConfig;
import com.okcoin.dapp.bundler.pool.domain.ret.DepositInfo;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.rest.api.resp.StakeStatusVO;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_GET_STAKE_STATUS;

@Service(DEBUG_BUNDLER_GET_STAKE_STATUS)
@Slf4j
public class DebugBundlerGetStakeStatusService implements AAMethodProcessor {

    @Autowired
    private ReputationConfig reputationConfig;

    @Override
    public StakeStatusVO process(IChain chain, List<Object> params) {
        String address = ((String) Iterables.get(params, 0)).toLowerCase();
        String entryPoint = ((String) Iterables.get(params, 1)).toLowerCase();

        BigInteger minStake = reputationConfig.getMinStake();
        long minUnstakeDelay = reputationConfig.getMinUnstakeDelay();

        DepositInfo depositInfo = Entrypoint.getDepositInfo(chain, entryPoint, address);
        boolean isStaked =
                depositInfo.getStake().getValue().compareTo(minStake) >= 0 && depositInfo.getUnstakeDelaySec().getValue().longValue() >= minUnstakeDelay;
        return StakeStatusVO.resolveBy(depositInfo, CodecUtil.toChecksumAddress(address), isStaked);
    }
}
