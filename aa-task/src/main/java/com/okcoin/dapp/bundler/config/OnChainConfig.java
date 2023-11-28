package com.okcoin.dapp.bundler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;

/**
 * @author Yukino.Xin on 2023/10/28 15:46
 */

@Configuration
@ConfigurationProperties(prefix = "bundler.on-chain")
@Data
public class OnChainConfig {

    private String privateKey;


    private Credentials bundlerCredential;

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        bundlerCredential = Credentials.create(privateKey);
    }
}
