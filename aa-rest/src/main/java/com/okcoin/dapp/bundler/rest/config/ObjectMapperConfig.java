package com.okcoin.dapp.bundler.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.okcoin.dapp.bundler.rest.api.resp.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ObjectMapperConfig {

    @Autowired
    private ObjectMapper objectMapper; // 已有的ObjectMapper

    @Autowired
    private NullClassSerializer nullClassSerializer; // 自定义序列化器

    @PostConstruct
    public void postConstruct() {
        // 创建并注册模块
        SimpleModule module = new SimpleModule();
        module.addSerializer(Null.class, nullClassSerializer);
        objectMapper.registerModule(module);
    }
}
