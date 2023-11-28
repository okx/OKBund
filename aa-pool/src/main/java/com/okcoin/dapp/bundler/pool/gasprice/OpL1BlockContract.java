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
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.protocol.core.methods.response.EthCall;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

@Component
public class OpL1BlockContract {

    private static final String OP_L1_BLOCK = "0x8882ffc121f5f2e603147ee085ec0eb7cfe6f175".toLowerCase(Locale.ROOT);

    public OpL1BlockInfo getOpL1BlockInfo(IChain chain) {
        List<Type> inputParameters = Lists.newArrayList();
        List<TypeReference<?>> outputParameters = Lists.newArrayList(TypeReference.create(Uint64.class), TypeReference.create(Uint64.class), TypeReference.create(Uint256.class), TypeReference.create(Bytes32.class), TypeReference.create(Uint64.class), TypeReference.create(Bytes32.class), TypeReference.create(Uint256.class), TypeReference.create(Uint256.class));
        Function function = new Function("getAllInfo", inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        EthCall call = TransactionUtil.call(null, OP_L1_BLOCK, data, chain);
        List<Type> result = FunctionReturnDecoder.decode(call.getValue(), function.getOutputParameters());

        BigInteger l1BaseFee = ((Uint256) result.get(2)).getValue();
        BigInteger l1FeeOverhead = ((Uint256) result.get(6)).getValue();
        BigInteger l1FeeScalar = ((Uint256) result.get(7)).getValue();
        OpL1BlockInfo opL1BlockInfo = new OpL1BlockInfo();
        opL1BlockInfo.setL1FeeOverhead(l1FeeOverhead.intValue());
        opL1BlockInfo.setL1FeeScalar(l1FeeScalar);
        opL1BlockInfo.setL1BaseFee(l1BaseFee);
        return opL1BlockInfo;
    }
}
