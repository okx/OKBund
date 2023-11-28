package com.okcoin.dapp.bundler.pool.util;

import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;

/**
 * @author yukino.xin
 * @date 2023/10/25 17:27
 */
public class AddressUtil {

    public static boolean isEmpty(String addr) {
        return addr == null || Web3Constant.HEX_PREFIX.equals(addr);
    }
}
