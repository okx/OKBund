package com.okcoin.dapp.bundler.pool.domain.debug;

import com.okcoin.dapp.bundler.pool.domain.error.ReturnInfo;
import com.okcoin.dapp.bundler.pool.domain.error.StakeInfo;
import lombok.Data;

import java.util.Map;

@Data
public class SimulateValidationResult {

    private final ReturnInfo returnInfo;

    private final StakeInfo senderInfo;

    private final StakeInfo factoryInfo;

    private final StakeInfo paymasterInfo;

    private final StakeInfo aggregatorInfo;

    private final ReferencedCodeHashes referencedContracts;

    private final Map<String, SlotMap> storageMap;

    public SimulateValidationResult(ReturnInfo returnInfo, StakeInfo senderInfo, StakeInfo factoryInfo, StakeInfo paymasterInfo,
                                    StakeInfo aggregatorInfo, ReferencedCodeHashes referencedContracts,
                                    Map<String, SlotMap> storageMap) {
        this.returnInfo = returnInfo;
        this.senderInfo = senderInfo;
        this.factoryInfo = factoryInfo;
        this.paymasterInfo = paymasterInfo;
        this.aggregatorInfo = aggregatorInfo;
        this.referencedContracts = referencedContracts;
        this.storageMap = storageMap;
    }
}
