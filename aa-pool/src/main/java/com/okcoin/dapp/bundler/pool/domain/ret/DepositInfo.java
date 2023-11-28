package com.okcoin.dapp.bundler.pool.domain.ret;

import lombok.Getter;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint48;

/**
 * @author Yukino.Xin on 2023/10/31 10:50
 */

@Getter
public class DepositInfo extends StaticStruct {

    private final Uint112 deposit;

    private final Bool staked;

    private final Uint112 stake;

    private final Uint32 unstakeDelaySec;

    private final Uint48 withdrawTime;

    public DepositInfo(Uint112 deposit, Bool staked, Uint112 stake, Uint32 unstakeDelaySec, Uint48 withdrawTime) {
        super(deposit, staked, staked, unstakeDelaySec, withdrawTime);
        this.deposit = deposit;
        this.staked = staked;
        this.stake = stake;
        this.unstakeDelaySec = unstakeDelaySec;
        this.withdrawTime = withdrawTime;
    }
}
