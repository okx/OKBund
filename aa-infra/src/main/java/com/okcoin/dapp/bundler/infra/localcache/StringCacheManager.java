package com.okcoin.dapp.bundler.infra.localcache;

import com.google.common.cache.LoadingCache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author fanweiqiang
 * @create 2023/10/24 15:29
 */
public class StringCacheManager {

    public enum CacheExpireType {
        UN_EXPIRE,
        ONE_DAY_EXPIRE
    }

    private static Map<CacheExpireType, LoadingCache<String, String>> instanceHolder;

    static {
        instanceHolder = new HashMap<>();
        instanceHolder.put(CacheExpireType.UN_EXPIRE, LocalCacheFactory.buildCache(null, null, null, null, null));
        instanceHolder.put(CacheExpireType.ONE_DAY_EXPIRE, LocalCacheFactory.buildCache(null, LocalCacheFactory.ExpireType.EXPIRE_AFTER_ACCESS, 1, TimeUnit.DAYS, null));
    }

    public static LoadingCache<String, String> getStringCacheInstance(CacheExpireType cacheExpireType) {
        return instanceHolder.get(cacheExpireType);
    }
}
