package com.okcoin.dapp.bundler.rest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Yukino.Xin on 2023/10/28 16:06
 */
@Component
@ConfigurationProperties("rest.debug")
@Data
public class DebugConfig {

    private boolean debugOpen = true;

}
