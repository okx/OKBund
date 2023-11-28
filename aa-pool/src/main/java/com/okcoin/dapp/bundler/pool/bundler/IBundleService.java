package com.okcoin.dapp.bundler.pool.bundler;

import com.okcoin.dapp.bundler.pool.domain.TxAndOpHashMappingDO;

/**
 * @author yukino.xin
 * @date 2023/10/26 14:14
 */
public interface IBundleService {

    void handlePastEvents();

    TxAndOpHashMappingDO sendNextBundle();

}
