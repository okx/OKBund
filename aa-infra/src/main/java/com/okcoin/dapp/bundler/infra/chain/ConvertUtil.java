package com.okcoin.dapp.bundler.infra.chain;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ConvertUtil {

    public static BigDecimal toGWei(BigInteger n) {
        return Convert.fromWei(new BigDecimal(n), Convert.Unit.GWEI);
    }

    public static BigDecimal toEther(BigInteger n) {
        return Convert.fromWei(new BigDecimal(n), Convert.Unit.ETHER);
    }

    public static BigInteger fromGWei(BigDecimal n) {
        return Convert.toWei(n, Convert.Unit.GWEI).toBigInteger();
    }

    public static BigInteger fromEther(BigDecimal n) {
        return Convert.toWei(n, Convert.Unit.ETHER).toBigInteger();
    }

}
