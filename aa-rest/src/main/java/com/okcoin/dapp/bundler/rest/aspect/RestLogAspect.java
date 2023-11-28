package com.okcoin.dapp.bundler.rest.aspect;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Splitter;
import com.okcoin.dapp.bundler.rest.api.req.AAReq;
import com.okcoin.dapp.bundler.rest.api.resp.AAError;
import com.okcoin.dapp.bundler.rest.api.resp.AAResp;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Aspect
@Slf4j
@Component
public class RestLogAspect {

    private static final Integer LOG_LENGTH = 3800;

    @Pointcut("@annotation(com.okcoin.dapp.bundler.rest.aspect.MultiChainRestLog)")
    public void multiChainRestLog() {
    }

    @Around("multiChainRestLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        AAResp resp = (AAResp) proceedingJoinPoint.proceed();
        long costTime = System.currentTimeMillis() - startTime;
        Object[] args = proceedingJoinPoint.getArgs();
        AAReq aaReq = (AAReq) args[0];
        Object result = resp.getResult();
        AAError error = resp.getError();

        boolean isError = false;
        String msg =
                "\n-- method: " + aaReq.getMethod() + "\n" +
                        "-- params: " + JSON.toJSONString(aaReq.getParams()) + "\n";

        if (error != null) {
            isError = true;
            msg += "-- errMsg: " + error.getMessage() + "\n" +
                    "-- errCode: " + error.getCode() + "\n" +
                    "-- errData: " + JSON.toJSONString(error.getData()) + "\n";
        } else {
            msg += "-- result: " + JSON.toJSONString(result) + "\n" +
                    "-- costTime: " + costTime + "\n";
        }

        for (String s : dealTooLongMsg(msg)) {
            if (isError) {
                log.error("{}", s);
            } else {
                log.info("{}", s);
            }
        }
        return resp;
    }

    private Iterable<String> dealTooLongMsg(String errorMsg) {
        int length = errorMsg.length();
        if (length < LOG_LENGTH) {
            return Collections.singletonList(errorMsg);
        }
        while (length > LOG_LENGTH) {
            length = length / 2;
        }
        return Splitter.fixedLength(length).split(errorMsg);
    }
}
