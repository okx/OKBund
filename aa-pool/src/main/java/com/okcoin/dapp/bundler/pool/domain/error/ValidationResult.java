package com.okcoin.dapp.bundler.pool.domain.error;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.error.ChainErrorMsg;
import com.okcoin.dapp.bundler.infra.chain.error.IEvmError;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;

import java.util.List;

@Getter
public class ValidationResult implements IEvmError {

    public static final Event VALIDATION_RESULT = new Event("ValidationResult",
            Lists.newArrayList(TypeReference.create(ReturnInfo.class),
                    TypeReference.create(StakeInfoEVM.class),
                    TypeReference.create(StakeInfoEVM.class),
                    TypeReference.create(StakeInfoEVM.class)
            )
    );

    public static final Event VALIDATION_RESULT_WITH_AGGREGATION = new Event("ValidationResultWithAggregation",
            Lists.newArrayList(TypeReference.create(ReturnInfo.class),
                    TypeReference.create(StakeInfoEVM.class),
                    TypeReference.create(StakeInfoEVM.class),
                    TypeReference.create(StakeInfoEVM.class),
                    TypeReference.create(AggregatorStakeInfo.class)
            )
    );

    public static final String VALIDATION_RESULT_ERROR_METHOD_ID = EventEncoder.encode(VALIDATION_RESULT).substring(0, 10);
    public static final String VALIDATION_RESULT_WITH_AGGREGATION_ERROR_METHOD_ID = EventEncoder.encode(VALIDATION_RESULT_WITH_AGGREGATION).substring(0, 10);

    private final ReturnInfo returnInfo;

    private final StakeInfo senderInfo;

    private final StakeInfo factoryInfo;

    private final StakeInfo paymasterInfo;

    private StakeInfo aggregatorInfo;

    private final ChainErrorMsg error;


    public ValidationResult(ChainErrorMsg chainErrorMsg) {
        List<Type> types;
        if (chainErrorMsg.isMethodId(VALIDATION_RESULT_ERROR_METHOD_ID)) {
            types = CodecUtil.decodeError(chainErrorMsg.getData(), VALIDATION_RESULT);
        } else {
            types = CodecUtil.decodeError(chainErrorMsg.getData(), VALIDATION_RESULT_WITH_AGGREGATION);
            AggregatorStakeInfo aggregatorStakeInfo = (AggregatorStakeInfo) types.get(4);
            aggregatorInfo = new StakeInfo(aggregatorStakeInfo.stakeInfo);
            aggregatorInfo.setAddr(aggregatorStakeInfo.aggregator.getValue());

        }
        returnInfo = (ReturnInfo) types.get(0);
        senderInfo = new StakeInfo((StakeInfoEVM) types.get(1));
        factoryInfo = new StakeInfo((StakeInfoEVM) types.get(2));
        paymasterInfo = new StakeInfo((StakeInfoEVM) types.get(3));
        this.error = chainErrorMsg;

    }

    public static boolean isMatch(String methodId) {
        return VALIDATION_RESULT_ERROR_METHOD_ID.equals(methodId) || VALIDATION_RESULT_WITH_AGGREGATION_ERROR_METHOD_ID.equals(methodId);
    }
}

