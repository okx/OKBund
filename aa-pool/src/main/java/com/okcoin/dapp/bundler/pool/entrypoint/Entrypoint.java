package com.okcoin.dapp.bundler.pool.entrypoint;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.ChainErrorUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.TransactionUtil;
import com.okcoin.dapp.bundler.infra.chain.error.ChainErrorMsg;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.error.SenderAddressResult;
import com.okcoin.dapp.bundler.pool.domain.ret.DepositInfo;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint192;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.ENTRYPOINT_EXT_GAS;

public class Entrypoint {

    private static final String FAKE_BENEFICIARY = "0x1234567890abcdeffedcba098765432113579ace";

    public static String getSenderAddress(String entrypoint, String initCode, IChain chain) {
        Function function = new Function("getSenderAddress", Lists.newArrayList(new DynamicBytes(Numeric.hexStringToByteArray(initCode))), Lists.newArrayList());
        String data = FunctionEncoder.encode(function);
        EthCall call = TransactionUtil.call(null, entrypoint, data, chain);
        ChainErrorMsg chainErrorMsg = ChainErrorUtil.parseChainError(call);
        if (chainErrorMsg.isMethodId(SenderAddressResult.ERROR_METHOD_ID)) {
            return new SenderAddressResult(chainErrorMsg).getAddress();
        } else {
            return Address.DEFAULT.getValue();
        }
    }

    public static BigInteger getNonce(String entrypoint, String sender, BigInteger key, IChain chain) {
        Function function = new Function("getNonce", Lists.newArrayList(new Address(sender), new Uint192(key)), Lists.newArrayList(TypeReference.create(Uint256.class)));
        String data = FunctionEncoder.encode(function);
        EthCall call = TransactionUtil.call(null, entrypoint, data, chain);
        ChainErrorUtil.throwChainError(call);
        return ((Uint256) FunctionReturnDecoder.decode(call.getValue(), function.getOutputParameters()).get(0)).getValue();
    }


    public static String getHandleOpsCallData(List<UserOperationDO> ops) {
        return getHandleOpsCallData(ops, FAKE_BENEFICIARY);
    }

    public static String handleOps(List<UserOperationDO> uopList, String beneficiary, String from,
                                   BigInteger gasLimit, BigInteger maxFeePerGas, BigInteger maxPriorityFeePerGas) {
        UserOperationDO uop = uopList.get(0);
        BigInteger verificationGasLimitMax = uopList.stream().map(UserOperationDO::getVerificationGasLimit).max(Comparator.comparing(x -> x)).orElse(BigInteger.ZERO);
        IChain chain = uop.getChain();
        String entryPoint = uop.getEntryPoint();
        String data = getHandleOpsCallData(uopList, beneficiary);
        EthSendTransaction transaction = TransactionUtil.execute(chain, from,
                gasLimit.add(verificationGasLimitMax).add(ENTRYPOINT_EXT_GAS), entryPoint, null, data,
                false, maxFeePerGas, maxPriorityFeePerGas);
        ChainErrorUtil.throwChainError(transaction);

        return transaction.getTransactionHash();
    }

    public static String getHandleOpsCallData(List<UserOperationDO> ops, String beneficiary) {
        DynamicArray<DynamicStruct> userOps = new DynamicArray<>(DynamicStruct.class, ops.stream().map(UserOperationDO::toDynamicStruct).collect(Collectors.toList()));
        Function function = new Function("handleOps", Lists.newArrayList(userOps, new Address(beneficiary)), Lists.newArrayList());
        return FunctionEncoder.encode(function);
    }

    public static BigInteger balanceOf(IChain chain, String entrypoint, String accountAddress) {
        List<Type> inputParameters = Lists.newArrayList(new Address(accountAddress));
        List<TypeReference<?>> outputParameters = Lists.newArrayList(TypeReference.create(Uint256.class));
        Function function = new Function("balanceOf", inputParameters, outputParameters);
        EthCall call = TransactionUtil.call(null, entrypoint, FunctionEncoder.encode(function), chain);
        return (BigInteger) FunctionReturnDecoder.decode(call.getValue(), function.getOutputParameters()).get(0).getValue();
    }

    public static DepositInfo getDepositInfo(IChain chain, String entrypoint, String accountAddress) {
        List<Type> inputParameters = Lists.newArrayList(new Address(accountAddress));
        List<TypeReference<?>> outputParameters = Lists.newArrayList(TypeReference.create(DepositInfo.class));
        Function function = new Function("getDepositInfo", inputParameters, outputParameters);
        EthCall call = TransactionUtil.call(null, entrypoint, FunctionEncoder.encode(function), chain);
        return (DepositInfo) FunctionReturnDecoder.decode(call.getValue(), function.getOutputParameters()).get(0);
    }
}
