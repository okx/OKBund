package com.okcoin.dapp.bundler.pool.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pool.core")
@Data
public class PoolConfig {

    private boolean safeMode;

    private Long chainId;

    private boolean eip1559;

    private String rpc;

    private String entrypoint;

    private long maxMempoolSize;

    private int autoBundleInterval;

    private String entrypointRuntimeCodeV6;

    private Integer blockTime;

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint.toLowerCase();
    }

}
