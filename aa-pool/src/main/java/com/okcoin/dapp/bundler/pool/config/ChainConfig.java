package com.okcoin.dapp.bundler.pool.config;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.web3j.Web3jDebug;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.web3j.protocol.http.HttpService;

/**
 * @author Yukino.Xin on 2023/10/28 14:25
 */
@Component
@Getter
public class ChainConfig implements IChain {

    private static final int DEFAULT_BLOCK_TIME_MS = 1000;

    private final long chainId;

    private final boolean eip1559;

    private final int blockTime;

    @Setter
    private Web3jDebug web3j;

    public ChainConfig(PoolConfig poolConfig) {
        this.chainId = poolConfig.getChainId();
        this.eip1559 = poolConfig.isEip1559();
        this.blockTime = poolConfig.getBlockTime() == null ? DEFAULT_BLOCK_TIME_MS : poolConfig.getBlockTime();
        HttpService web3jService = new HttpService(poolConfig.getRpc(), true);
        this.web3j = Web3jDebug.build(web3jService);
    }

}
