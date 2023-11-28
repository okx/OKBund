package com.okcoin.dapp.bundler.rest.api;


import com.okcoin.dapp.bundler.pool.config.ChainConfig;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.rest.api.req.AAReq;
import com.okcoin.dapp.bundler.rest.api.resp.AAError;
import com.okcoin.dapp.bundler.rest.api.resp.AAResp;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessorFactory;
import com.okcoin.dapp.bundler.rest.aspect.MultiChainRestLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("rpc")
@Slf4j
public class AAController {

    @Autowired
    private AAMethodProcessorFactory aaMethodProcessorFactory;

    @Autowired
    private ChainConfig chainConfig;

    @MultiChainRestLog
    @PostMapping
    public AAResp chain(@RequestBody AAReq request) {
        AAResp<Object> response = new AAResp<>();
        response.setId(request.getId());
        response.setJsonrpc(request.getJsonrpc());
        String method = request.getMethod();
        List<Object> params = request.getParams();
        try {
            AAMethodProcessor processor = aaMethodProcessorFactory.get(method);
            Object result = processor.process(chainConfig, params);
            response.setResult(result);
        } catch (AAException e) {
            log.error("{}, aa error", method, e);
            response.setError(new AAError(e.getCode(), e.getMsg(), e.getData()));
            return response;
        } catch (Exception e) {
            log.error("{} error", method, e);
            response.setError(new AAError(0, e.getMessage(), null));
            return response;
        }

        return response;
    }

}
