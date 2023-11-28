package com.okcoin.dapp.bundler.pool.domain.error;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.error.ChainErrorMsg;
import com.okcoin.dapp.bundler.infra.chain.error.IEvmError;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;

import java.util.List;

@Getter
public class SenderAddressResult implements IEvmError {

    public static final Event ERROR = new Event("SenderAddressResult", Lists.newArrayList(TypeReference.create(Address.class)));
    public static final String ERROR_METHOD_ID = EventEncoder.encode(ERROR).substring(0, 10);

    private final String address;

    private final ChainErrorMsg error;

    public SenderAddressResult(ChainErrorMsg chainErrorMsg) {
        List<Type> types = CodecUtil.decodeError(chainErrorMsg.getData(), ERROR);
        this.address = ((Address) types.get(0)).getValue();
        this.error = chainErrorMsg;
    }

}
