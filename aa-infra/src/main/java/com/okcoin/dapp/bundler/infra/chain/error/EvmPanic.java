package com.okcoin.dapp.bundler.infra.chain.error;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import lombok.Getter;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import java.util.List;

@Getter
public class EvmPanic implements IEvmError {

    public static final Event ERROR = new Event("Panic", Lists.newArrayList(TypeReference.create(Uint256.class)));

    /**
     * 0x4e487b71
     */
    public static final String ERROR_METHOD_ID = IEvmError.getMethodId(ERROR);

    /**
     * 0x01: If you call assert with an argument that evaluates to false.
     * 0x11: If an arithmetic operation results in underflow or overflow outside of an unchecked { ... } block.
     * 0x12; If you divide or modulo by zero (e.g. 5 / 0 or 23 % 0).
     * 0x21: If you convert a value that is too big or negative into an enum type.
     * 0x22: If you access a storage byte array that is incorrectly encoded.
     * 0x31: If you call .pop() on an empty array.
     * 0x32: If you access an array, bytesN or an array slice at an out-of-bounds or negative index (i.e. x[i] where i >= x.length or i < 0).
     * 0x41: If you allocate too much memory or create an array that is too large.
     * 0x51: If you call a zero-initialized variable of internal function type.
     */
    private final String message;

    private final ChainErrorMsg error;

    public EvmPanic(ChainErrorMsg chainErrorMsg) {
        List<Type> types = CodecUtil.decodeError(chainErrorMsg.getData(), ERROR);
        this.message = Numeric.encodeQuantity(((Uint256) types.get(0)).getValue());
        error = chainErrorMsg;
    }

}
