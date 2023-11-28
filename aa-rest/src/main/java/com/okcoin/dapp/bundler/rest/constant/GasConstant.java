package com.okcoin.dapp.bundler.rest.constant;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public interface GasConstant {

    int PER_USER_OP_WORD = 4;

    int ZERO_BYTE = 4;

    int NON_ZERO_BYTE = 16;

    BigInteger VERIFICATION_GAS_LIMIT_MIN = Numeric.toBigInt("0xfffff");

    BigInteger CALL_GAS_LIMIT_MIN = Numeric.toBigInt("0xffffff");

    BigInteger PRE_VERIFICATION_GAS_MIN = BigInteger.ONE;

    BigInteger MAX_FEE_PER_GAS_FAKE = Numeric.toBigInt("0xfedcba9876543210");

    BigInteger MAX_PRIORITY_FEE_PER_GAS_FAKE = Numeric.toBigInt("0x123456789abcdef");

    BigInteger PRE_VERIFICATION_GAS_FAKE = Numeric.toBigInt("0xfdb97531eca86420");

    BigInteger VERIFICATION_GAS_LIMIT_FAKE = Numeric.toBigInt("0x369cf0");

    BigInteger CALL_GAS_LIMIT_FAKE = Numeric.toBigInt("0xfc9630");

    double FEE_HISTORY_COMMON_REWARD_PERCENTILE = 50D;
}
