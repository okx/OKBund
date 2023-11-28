package com.okcoin.dapp.bundler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @ClassName BundlerConfig
 * @Author qunqin
 * @Date 2023/10/25
 **/
@Configuration
@ConfigurationProperties(prefix = "bundler.bundle")
@Data
public class BundleConfig {

    private BigInteger maxBundleGas;

    private BigDecimal baseFeeMinCoefficient;

    private BigDecimal maxPriorityFeePerGasMinCoefficient;


}
