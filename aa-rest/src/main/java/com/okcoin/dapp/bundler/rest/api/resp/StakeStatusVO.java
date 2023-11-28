package com.okcoin.dapp.bundler.rest.api.resp;

import com.okcoin.dapp.bundler.pool.domain.ret.DepositInfo;
import lombok.Data;

/**
 * @author Yukino.Xin on 2023/10/31 11:01
 */

@Data
public class StakeStatusVO {

    private StakeInfoVO stakeInfo;

    private Boolean isStaked;

    public static StakeStatusVO resolveBy(DepositInfo depositInfo, String addr, boolean isStaked) {
        StakeStatusVO stakeStatusVO = new StakeStatusVO();
        StakeInfoVO stakeInfoVO = new StakeInfoVO();
        stakeInfoVO.setStake(depositInfo.getStake().getValue().toString());
        stakeInfoVO.setUnstakeDelaySec(depositInfo.getUnstakeDelaySec().getValue().toString());
        stakeInfoVO.setAddr(addr);

        stakeStatusVO.setStakeInfo(stakeInfoVO);
        stakeStatusVO.setIsStaked(isStaked);
        return stakeStatusVO;
    }
}
