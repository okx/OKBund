package com.okcoin.dapp.bundler.pool.util;


import java.math.BigDecimal;
import java.math.BigInteger;

public class MathUtil {

    public static <T extends Comparable<T>> T max(T x, T y) {
        if (x.compareTo(y) > 0) {
            return x;
        } else {
            return y;
        }
    }

    public static <T extends Comparable<T>> T min(T x, T y) {
        if (x.compareTo(y) < 0) {
            return x;
        } else {
            return y;
        }
    }

    public static BigInteger multiply(BigInteger x, BigDecimal y) {
        return new BigDecimal(x).multiply(y).toBigInteger();
    }
}
