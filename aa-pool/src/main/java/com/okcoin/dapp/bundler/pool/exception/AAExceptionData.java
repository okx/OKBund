package com.okcoin.dapp.bundler.pool.exception;

import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;

import java.util.HashMap;

public class AAExceptionData extends HashMap<String, Object> {

    public AAExceptionData(Object... others) {
        super();
        for (int i = 0; i < others.length / 2; i++) {
            String k = (String) others[i * 2];
            Object v = others[i * 2 + 1];
            if (v instanceof String && FieldUtil.isValidAddress((String) v)) {
                putAddress(k, (String) v);
            } else {
                put(k, v);
            }
        }
    }

    public void putAddress(String key, String address) {
        put(key, CodecUtil.toChecksumAddress(address));
    }
}
