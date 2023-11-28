package com.okcoin.dapp.bundler.pool.domain.error;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class StakeInfo {

    private final BigInteger stake;

    private final int unstakeDelaySec;

    private String addr;

    public StakeInfo(StakeInfoEVM stakeInfo) {
        this.stake = stakeInfo.getStake().getValue();
        this.unstakeDelaySec = stakeInfo.getUnstakeDelaySec().getValue().intValue();
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
