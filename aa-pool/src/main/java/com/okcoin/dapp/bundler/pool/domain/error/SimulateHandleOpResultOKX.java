package com.okcoin.dapp.bundler.pool.domain.error;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.infra.chain.error.ChainErrorMsg;
import com.okcoin.dapp.bundler.infra.chain.error.EvmError;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint48;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;


@Getter
public class SimulateHandleOpResultOKX {

    public static final Event ERROR = new Event("SimulateHandleOpResult",
            Lists.newArrayList(TypeReference.create(Uint48.class), TypeReference.create(Uint48.class),
                    TypeReference.create(Address.class), TypeReference.create(Uint256.class), TypeReference.create(Uint256.class),
                    TypeReference.create(Bool.class), TypeReference.create(DynamicBytes.class),
                    TypeReference.create(Uint256.class), TypeReference.create(Uint256.class), TypeReference.create(Uint256.class),
                    TypeReference.create(Bool.class), TypeReference.create(DynamicBytes.class)));

    public static final String ERROR_METHOD_ID = EventEncoder.encode(ERROR).substring(0, 10);

    private final long validAfter;

    private final long validUntil;

    private final String aggregator;

    private final BigInteger preVerificationGas;

    private final BigInteger verificationGasUsed;

    private final boolean execSuccess;

    private final String execErrMsg;

    private final BigInteger actualGasUsed;

    private final BigInteger postOpGasUsed;

    private final BigInteger paid;

    private final boolean targetSuccess;

    private final String targetResult;

    private final BigInteger callGasUsed;

    private final BigInteger actualGasPrice;

    public SimulateHandleOpResultOKX(ChainErrorMsg chainErrorMsg) {
        List<Type> types = CodecUtil.decodeError(chainErrorMsg.getData(), ERROR);
        validAfter = ((Uint48) types.get(0)).getValue().longValue();
        long validUntilTemp = ((Uint48) types.get(1)).getValue().longValue();
        validUntil = validUntilTemp == 0 ? Web3Constant.UINT48_MAX : validUntilTemp;
        aggregator = ((Address) types.get(2)).getValue();
        preVerificationGas = ((Uint256) types.get(3)).getValue();
        verificationGasUsed = ((Uint256) types.get(4)).getValue();
        execSuccess = ((Bool) types.get(5)).getValue();
        execErrMsg = Numeric.toHexString(((DynamicBytes) types.get(6)).getValue());
        actualGasUsed = ((Uint256) types.get(7)).getValue();
        postOpGasUsed = ((Uint256) types.get(8)).getValue();
        paid = ((Uint256) types.get(9)).getValue();
        targetSuccess = (boolean) types.get(10).getValue();
        targetResult = Numeric.toHexString(((DynamicBytes) types.get(11)).getValue());
        callGasUsed = actualGasUsed.subtract(postOpGasUsed).subtract(verificationGasUsed).subtract(preVerificationGas);
        actualGasPrice = paid.divide(actualGasUsed);
    }

    public void tryRevert() {
        if (execSuccess) {
            return;
        }

        String reason = execErrMsg;
        if (StringUtils.startsWith(execErrMsg, EvmError.ERROR_METHOD_ID)) {
            reason = new EvmError(execErrMsg).getReason();
        }
        throw new AAException(AAExceptionEnum.USER_OPERATION_REVERTED, reason);

    }
}

