package com.okcoin.dapp.bundler.domain;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.manager.TransactionGas;
import com.okcoin.dapp.bundler.pool.domain.debug.SlotMap;
import com.okcoin.dapp.bundler.pool.domain.pool.MempoolEntry;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author yukino.xin
 * @date 2023/10/26 14:30
 */
@Getter
public class UopBundleDO {

    public static final UopBundleDO NULL = new UopBundleDO(Lists.newArrayList(), null, null);

    private final List<MempoolEntry> mempoolEntryList;

    private final Map<String, SlotMap> storageMap;

    private final TransactionGas transactionGas;

    public UopBundleDO(List<MempoolEntry> mempoolEntryList, Map<String, SlotMap> storageMap, TransactionGas transactionGas) {
        this.mempoolEntryList = mempoolEntryList;
        this.storageMap = storageMap;
        this.transactionGas = transactionGas;
    }
}
