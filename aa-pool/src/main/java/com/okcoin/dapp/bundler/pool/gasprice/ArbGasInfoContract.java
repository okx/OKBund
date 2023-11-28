package com.okcoin.dapp.bundler.pool.gasprice;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.TransactionUtil;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.EthCall;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

@Component
public class ArbGasInfoContract {

    public static final String ARB_GAS_INFO_ADDRESS = "0x000000000000000000000000000000000000006c".toLowerCase(Locale.ROOT);

    public BigInteger getL1BaseFeeEstimate(IChain chain) {
        List<Type> inputParameters = Lists.newArrayList();
        List<TypeReference<?>> outputParameters = Lists.newArrayList(TypeReference.create(Uint256.class));
        Function function = new Function("getL1BaseFeeEstimate", inputParameters, outputParameters);
        EthCall call = TransactionUtil.call(null, ARB_GAS_INFO_ADDRESS, FunctionEncoder.encode(function), chain);
        List<Type> result = FunctionReturnDecoder.decode(call.getValue(), function.getOutputParameters());
        return ((BigInteger) result.get(0).getValue());
    }

}
