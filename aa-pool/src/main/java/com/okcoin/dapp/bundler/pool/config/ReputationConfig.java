package com.okcoin.dapp.bundler.pool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

/**
 * @author yukino.xin
 * @date 2023/10/26 22:02
 */

@Configuration
@ConfigurationProperties(prefix = "pool.reputation")
@Data
public class ReputationConfig {

    private int maxMempoolUserOpsPerSender = 4;

    private int throttledEntityMempoolCount = 4;

    private long minInclusionDenominator;

    private long banSlack;

    private long throttlingSlack;

    private BigInteger minStake = BigInteger.ONE;

    private Long minUnstakeDelay = 0L;

    private Double countDecreaseRate = 1d / 24d;

    int revertOpAddReputationSeen = 10000;


}
