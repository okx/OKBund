package com.okcoin.dapp.bundler.pool.bundler;

import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.domain.TxAndOpHashMappingDO;
import com.okcoin.dapp.bundler.pool.mem.MempoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Yukino.Xin on 2023/10/28 21:29
 */

@Component
public class ExecutionService {

    @Autowired
    private IBundleService bundleService;

    @Autowired
    private MempoolService mempoolService;

    @Autowired
    private PoolConfig poolConfig;

    public TxAndOpHashMappingDO attemptBundle(boolean force) {
        if (force || mempoolService.count() >= poolConfig.getMaxMempoolSize()) {
            TxAndOpHashMappingDO ret = bundleService.sendNextBundle();
            if (poolConfig.getMaxMempoolSize() == 0) {
                bundleService.handlePastEvents();
            }
            return ret;
        }

        return null;

    }

    public void setAutoBundler(int autoBundleInterval, int maxMempoolSize) {
        poolConfig.setAutoBundleInterval(autoBundleInterval);
        poolConfig.setMaxMempoolSize(maxMempoolSize);
    }

}
