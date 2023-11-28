package com.okcoin.dapp.bundler.pool.domain.error;

import lombok.Getter;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.generated.Uint256;

@Getter
public class StakeInfoEVM extends StaticStruct {

    public final Uint256 stake;

    public final Uint256 unstakeDelaySec;

    public StakeInfoEVM(Uint256 stake, Uint256 unstakeDelaySec) {
        super(stake, unstakeDelaySec);
        this.stake = stake;
        this.unstakeDelaySec = unstakeDelaySec;
    }
}
