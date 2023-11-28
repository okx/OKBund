package com.okcoin.dapp.bundler.rest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("rest.estimate")
@Data
public class RestConfig {

    private int fixed = 21000;

    private long perUserOp = 19700;

    private int perUserOpWord = 4;

    private int zeroByte = 4;

    private int nonZeroByte = 16;

    private int bundleSize = 1;

    private boolean openEstimateGasFromNode = true;
}
