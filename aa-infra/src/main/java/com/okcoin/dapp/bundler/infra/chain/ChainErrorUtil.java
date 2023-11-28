package com.okcoin.dapp.bundler.infra.chain;

import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.infra.chain.error.ChainErrorMsg;
import com.okcoin.dapp.bundler.infra.chain.error.EvmError;
import com.okcoin.dapp.bundler.infra.chain.exception.ChainException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.core.Response;

import java.util.List;

@Slf4j
public class ChainErrorUtil {

    public static ChainErrorMsg parseChainError(Response response) {
        Object result = response.getResult();
        if (result instanceof String && ((String) result).startsWith(EvmError.ERROR_METHOD_ID)) {
            String hexRevertReason = ((String) result).substring(EvmError.ERROR_METHOD_ID.length());
            List<Type> decoded = FunctionReturnDecoder.decode(hexRevertReason, EvmError.ERROR.getParameters());
            String decodedRevertReason = ((Utf8String) decoded.get(0)).getValue();
            return new ChainErrorMsg(0, decodedRevertReason, (String) result);
        }
        if (!response.hasError()) {
            return ChainErrorMsg.DEFAULT;
        }
        Response.Error error = response.getError();
        String message = error.getMessage();
        String data = StringUtils.strip(error.getData(), "\"");
        if (!StringUtils.startsWith(data, Web3Constant.HEX_PREFIX)
                && StringUtils.startsWith(message, Web3Constant.HEX_PREFIX)) {
            data = message;
        }
        return new ChainErrorMsg(error.getCode(), message, data);
    }

    public static void throwChainError(Response response) {
        if (!response.hasError()) {
            return;
        }

        throw new ChainException(response.getError());
    }
}
