package com.okcoin.dapp.bundler.pool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yukino.Xin on 2023/10/28 15:55
 */
@Configuration
@ConfigurationProperties(prefix = "pool.validation")
@Data
public class ValidationConfig {

    private long validUntilFutureSeconds = 30;

}
