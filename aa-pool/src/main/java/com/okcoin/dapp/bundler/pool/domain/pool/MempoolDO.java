package com.okcoin.dapp.bundler.pool.domain.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author yukino.xin
 * @date 2023/10/25 15:54
 */
@Slf4j
public class MempoolDO {

    private final ConcurrentMap<MempoolEntryKey, MempoolEntry> pool = Maps.newConcurrentMap();

    private final ConcurrentSkipListSet<MempoolEntryKey> priority = new ConcurrentSkipListSet<>();

    public MempoolEntry add(MempoolEntry entry) {
        MempoolEntryKey key = entry.buildKey();
        priority.add(key);
        return pool.put(key, entry);
    }

    public void replace(MempoolEntry entry, MempoolEntry oldEntry) {
        MempoolEntryKey key = entry.buildKey();
        pool.replace(key, oldEntry, entry);
        priority.add(key);
        priority.remove(oldEntry.buildKey());
    }

    public MempoolEntry get(String sender, BigInteger nonce) {
        MempoolEntryKey key = new MempoolEntryKey(sender, nonce, BigInteger.ZERO);
        return pool.get(key);
    }

    public MempoolEntry remove(String sender, BigInteger nonce) {
        MempoolEntryKey key = new MempoolEntryKey(sender, nonce, BigInteger.ZERO);
        MempoolEntry entry = pool.remove(key);
        if (entry != null) {
            priority.remove(entry.buildKey());
        }
        return entry;
    }

    public void clearState() {
        pool.clear();
        priority.clear();
    }

    public int getMempoolSize() {
        return pool.size();
    }

    public List<MempoolEntry> getAllEntityByPriority() {
        List<MempoolEntry> mempoolEntryList = Lists.newArrayList();
        for (MempoolEntryKey mempoolEntryKey : priority.descendingSet()) {
            MempoolEntry entry = pool.get(mempoolEntryKey);
            if (entry == null) {
                continue;
            }
            mempoolEntryList.add(entry);
        }

        return mempoolEntryList;
    }
}
