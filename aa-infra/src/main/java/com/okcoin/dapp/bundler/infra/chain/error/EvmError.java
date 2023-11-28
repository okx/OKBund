package com.okcoin.dapp.bundler.infra.chain.error;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import lombok.Getter;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.utils.Numeric;

import java.util.List;

@Getter
public class EvmError implements IEvmError {

    public static final Event ERROR = new Event("Error", Lists.newArrayList(TypeReference.create(Utf8String.class)));

    // 0x08c379a0
    public static final String ERROR_METHOD_ID = IEvmError.getMethodId(ERROR);

    private final String hexData;

    private final String reason;

    private final ChainErrorMsg error;

    public EvmError(ChainErrorMsg chainErrorMsg) {
        List<Type> types = CodecUtil.decodeError(chainErrorMsg.getData(), ERROR);
        String reason = (String) types.get(0).getValue();
        String hexData;
        if (reason.startsWith(ERROR_METHOD_ID)) {
            hexData = reason;
        } else {
            hexData = Numeric.toHexString(reason.getBytes());
        }

        error = chainErrorMsg;
        if (hexData.startsWith(ERROR_METHOD_ID)) {
            chainErrorMsg = new ChainErrorMsg(hexData);
            this.hexData = chainErrorMsg.getData();
            this.reason = new EvmError(chainErrorMsg).getReason();
        } else {
            this.hexData = hexData;
            this.reason = reason;
        }
    }

    public EvmError(String hexData) {
        this(new ChainErrorMsg(hexData));
    }

}
