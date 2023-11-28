package com.okcoin.dapp.bundler.pool.domain.pool;

import com.google.common.collect.Maps;
import com.okcoin.dapp.bundler.pool.util.AddressUtil;

import java.util.Map;
import java.util.Set;

import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.FACTORY_TITLE;
import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.PAYMASTER_TITLE;

/**
 * @author yukino.xin
 * @date 2023/10/25 18:07
 */
public class AddressCountDO {

    private final Map<String, Integer> senderEntryCount = Maps.newConcurrentMap();

    private final Map<String, Integer> paymasterEntryCount = Maps.newConcurrentMap();

    private final Map<String, Integer> factoryEntryCount = Maps.newConcurrentMap();


    public int entryCount(String addr) {
        if (AddressUtil.isEmpty(addr)) {
            return 0;
        }
        return senderEntryCount.getOrDefault(addr, 0) + paymasterEntryCount.getOrDefault(addr, 0)
                + factoryEntryCount.getOrDefault(addr, 0);
    }

    public Set<String> knownAddresses(String title) {
        Map<String, Integer> entryCount;
        if (PAYMASTER_TITLE.equals(title)) {
            entryCount = paymasterEntryCount;
        } else if (FACTORY_TITLE.equals(title)) {
            entryCount = factoryEntryCount;
        } else {
            entryCount = senderEntryCount;
        }

        return entryCount.keySet();
    }

    public void incrementEntryCount(String title, String addr) {
        if (AddressUtil.isEmpty(addr)) {
            return;
        }

        Map<String, Integer> entryCount;
        if (PAYMASTER_TITLE.equals(title)) {
            entryCount = paymasterEntryCount;
        } else if (FACTORY_TITLE.equals(title)) {
            entryCount = factoryEntryCount;
        } else {
            entryCount = senderEntryCount;
        }

        entryCount.compute(addr, (k, v) -> {
            if (v == null) {
                return 1;
            } else {
                return v + 1;
            }
        });
    }

    public void decrementEntryCount(String title, String addr) {
        if (AddressUtil.isEmpty(addr)) {
            return;
        }

        Map<String, Integer> entryCount;
        if (PAYMASTER_TITLE.equals(title)) {
            entryCount = paymasterEntryCount;
        } else if (FACTORY_TITLE.equals(title)) {
            entryCount = factoryEntryCount;
        } else {
            entryCount = senderEntryCount;
        }

        entryCount.computeIfPresent(addr, (k, v) -> {
            int newV = v - 1;
            if (newV <= 0) {
                return null;
            } else {
                return newV;
            }
        });
    }

    public void clearState() {
        senderEntryCount.clear();
        paymasterEntryCount.clear();
        factoryEntryCount.clear();
    }


}
