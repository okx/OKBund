package com.okcoin.dapp.bundler.rest.api.service;

import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.rest.config.DebugConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum.METHOD_NOT_SUPPORT;


@Component
public class AAMethodProcessorFactory {

    @Autowired
    private Map<String, AAMethodProcessor> aaMethodProcessors;

    @Autowired
    private DebugConfig debugConfig;

    public AAMethodProcessor get(String method) {
        if (method.contains("debug") && !debugConfig.isDebugOpen()) {
            throw new AAException(METHOD_NOT_SUPPORT, "Method {} is not supported", method);
        }

        AAMethodProcessor processor = aaMethodProcessors.get(method);
        if (processor == null) {
            throw new AAException(METHOD_NOT_SUPPORT, "Method {} is not supported", method);

        }

        return processor;

    }

}
