package com.okcoin.dapp.bundler.pool.domain.error;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.StaticStruct;


public class AggregatorStakeInfo extends StaticStruct {

    public final Address aggregator;

    public final StakeInfoEVM stakeInfo;

    public AggregatorStakeInfo(Address aggregator, StakeInfoEVM stakeInfo) {
        super(aggregator, stakeInfo);
        this.aggregator = aggregator;
        this.stakeInfo = stakeInfo;
    }
}
