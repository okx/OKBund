package com.okcoin.dapp.bundler.infra.chain.error;

import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;

public interface IEvmError {

    static IEvmError parseDefaultError(ChainErrorMsg chainErrorMsg) {
        if (chainErrorMsg.isMethodId(EvmError.ERROR_METHOD_ID)) {
            return new EvmError(chainErrorMsg);
        } else {
            return new UnKnowError(chainErrorMsg);
        }
    }

    default String getMethodId() {
        return getError().getMethodId();
    }

    static String getMethodId(Event ERROR) {
        return EventEncoder.encode(ERROR).substring(0, Web3Constant.METHOD_ID_LENGTH);
    }

    ChainErrorMsg getError();

    static String resolveFinalReason(String reason) {
        if (reason.startsWith(EvmError.ERROR_METHOD_ID)) {
            return new EvmError(reason).getReason();
        } else {
            return reason;
        }
    }

}
