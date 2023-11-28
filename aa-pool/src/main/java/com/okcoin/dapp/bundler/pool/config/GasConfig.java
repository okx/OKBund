package com.okcoin.dapp.bundler.pool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yukino.Xin on 2023/10/28 15:58
 */

@Configuration
@ConfigurationProperties(prefix = "pool.gas")
@Data
public class GasConfig {

    private Double rewardPercentile = 50D;

    private Long gasPriceCacheTimeSecond = 5L;
}
