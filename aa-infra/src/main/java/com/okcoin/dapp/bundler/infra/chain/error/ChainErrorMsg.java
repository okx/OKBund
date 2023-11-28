package com.okcoin.dapp.bundler.infra.chain.error;

import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import lombok.Getter;

/**
 * go-ethereum@v1.10.8-okc1/core/vm/errors.go
 */
@Getter
public class ChainErrorMsg {

    public static final ChainErrorMsg DEFAULT = new ChainErrorMsg(Integer.MIN_VALUE, "", Web3Constant.HEX_PREFIX);

    private final int code;

    private final String message;

    private final String data;

    public ChainErrorMsg(int code, String message, String data) {
        this.code = code;
        this.message = message;
        if (FieldUtil.isValidHex(data)) {
            this.data = data;
        } else {
            this.data = Web3Constant.HEX_PREFIX;
        }
    }

    public ChainErrorMsg(String data) {
        this.code = 0;
        this.message = data;
        this.data = data;
    }

    public String getMethodId() {
        if (FieldUtil.isEmpty(data)) {
            return Web3Constant.HEX_PREFIX;
        }

        if (CodecUtil.hasValidMethodId(data)) {
            return data.substring(0, Web3Constant.METHOD_ID_LENGTH);
        } else {
            return Web3Constant.HEX_PREFIX;
        }

    }

    public boolean isMethodId(String methodId) {
        return getMethodId().equals(methodId);
    }
}
