package com.okcoin.dapp.bundler.infra.chain;

import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;

import java.util.regex.Pattern;

/**
 * @author Yukino.Xin on 2023/10/28 14:52
 */
public class FieldUtil {

    private static final Pattern PATTERN = Pattern.compile("^0x[0-9a-fA-F]*$");

    private static final Pattern PATTERN_ADDRESS = Pattern.compile("^0x[0-9a-fA-F]{40,40}$");

    private static final Pattern PATTERN_BYTES32 = Pattern.compile("^0x[0-9a-fA-F]{64,64}$");

    public static boolean isValidBytes32(String value) {
        if (!isValidHexPrefix(value)) {
            return false;
        }

        return PATTERN_BYTES32.matcher(value).matches();
    }

    public static boolean isValidAddress(String value) {
        if (!isValidHexPrefix(value)) {
            return false;
        }

        return PATTERN_ADDRESS.matcher(value).matches();
    }

    public static boolean isValidHex(String value) {
        if (!isValidHexPrefix(value)) {
            return false;
        }

        return PATTERN.matcher(value).matches();
    }

    public static boolean isValidHexPrefix(String value) {
        if (value == null) {
            return false;
        }

        if (value.length() < 2) {
            return false;
        }

        return value.startsWith(Web3Constant.HEX_PREFIX);
    }

    public static boolean isEmpty(String field) {
        return !isValidHexPrefix(field) || Web3Constant.HEX_PREFIX.equals(field);
    }
}
