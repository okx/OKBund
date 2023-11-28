package com.okcoin.dapp.bundler.pool.util;

import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionData;

import java.util.concurrent.TimeUnit;

import static com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum.NOT_IN_TIME_RANGE;

public class ValidateTimeUtil {

    public static void checkValidateTime(long validAfter, long validUntil, long validUntilFutureSeconds) {
        AAExceptionData data = new AAExceptionData(Eip4377CommonConstant.VALID_UNTIL, validUntil,
                Eip4377CommonConstant.VALID_AFTER, validAfter);
        if (validAfter > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) {
            throw new AAException(data, NOT_IN_TIME_RANGE, "time-range in the future time");
        }

        if (validUntil == 0) {
            validUntil = Web3Constant.UINT48_MAX;
        }

        if (validUntil <= TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) {
            throw new AAException(data, NOT_IN_TIME_RANGE, "already expired");
        }

        if (validUntil <= TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + validUntilFutureSeconds) {
            throw new AAException(data, NOT_IN_TIME_RANGE, "expires too soon");
        }
    }


}
