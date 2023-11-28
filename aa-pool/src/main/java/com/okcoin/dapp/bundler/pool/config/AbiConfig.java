package com.okcoin.dapp.bundler.pool.config;

import com.alibaba.fastjson2.JSON;
import com.esaulpaugh.headlong.abi.ABIObject;
import com.esaulpaugh.headlong.abi.ContractError;
import com.esaulpaugh.headlong.abi.Function;
import com.esaulpaugh.headlong.abi.TupleType;
import com.google.common.collect.Maps;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
public class AbiConfig {

    private final Map<String, Function> FUNCTION_MAP = Maps.newHashMap();
    private final Map<String, ContractError> ERROR_MAP = Maps.newHashMap();
    @Autowired
    private ResourceLoader resourceLoader;

    public Function getFunc(String methodId) {
        return FUNCTION_MAP.get(methodId);
    }

    public ContractError getError(String methodId) {
        return ERROR_MAP.get(methodId);
    }

    @PostConstruct
    public void init() {
        resolveAbi("abi/core/IAccount.json");
        resolveAbi("abi/core/IEntryPoint.json");
        resolveAbi("abi/core/IPaymaster.json");
        resolveAbi("abi/core/SenderCreator.json");
        resolveAbi("abi/test/TestOpcodesAccount.json");
        resolveAbi("abi/test/TestOpcodesAccountFactory.json");
        resolveAbi("abi/test/TestStorageAccount.json");
        ERROR_MAP.put("0x08c379a0", new ContractError("ERROR", TupleType.of("string")));
    }

    private void resolveAbi(String abiPath) {
        String abiStr = readAbiResource(abiPath);
        List<String> abiList = JSON.parseArray(JSON.parseObject(abiStr).getString("abi"), String.class);
        for (String abiJson : abiList) {
            ABIObject abiObject = ABIObject.fromJson(abiJson);
            String methodId = CodecUtil.buildMethodId(abiObject.getCanonicalSignature());
            if (abiObject.isContractError()) {
                ContractError error = abiObject.asContractError();
                ERROR_MAP.put(methodId, error);
            } else if (abiObject.isFunction()) {
                Function function = abiObject.asFunction();
                FUNCTION_MAP.put(methodId, function);
            }
        }
    }

    @SneakyThrows
    private String readAbiResource(String abiPath) {
        Resource resource = resourceLoader.getResource("classpath:" + abiPath);
        byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
