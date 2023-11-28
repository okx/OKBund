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
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.List;

@Getter
public class FailedOp implements IEvmError {

    public static final Event ERROR = new Event("FailedOp", Lists.newArrayList(TypeReference.create(Uint256.class), TypeReference.create(Utf8String.class)));

    // 0x220266b6
    public static final String ERROR_METHOD_ID = EventEncoder.encode(ERROR).substring(0, 10);

    private final int opIndex;

    private final String reason;

    private final ChainErrorMsg error;

    public FailedOp(ChainErrorMsg chainErrorMsg) {
        List<Type> types = CodecUtil.decodeError(chainErrorMsg.getData(), ERROR);
        opIndex = ((Uint256) types.get(0)).getValue().intValue();
        reason = (String) types.get(1).getValue();
        error = chainErrorMsg;
    }

    public FailedOp(String hexData) {
        this(new ChainErrorMsg(hexData));
    }
}
