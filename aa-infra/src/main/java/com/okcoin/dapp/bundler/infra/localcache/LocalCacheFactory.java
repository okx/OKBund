package com.okcoin.dapp.bundler.infra.localcache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author fanweiqiang
 * @create 2023/10/24 14:27
 */
@Slf4j
public class LocalCacheFactory {

    enum ExpireType {
        EXPIRE_AFTER_WRITE,
        EXPIRE_AFTER_ACCESS
    }

    public static <K, V> LoadingCache<K, V> buildCache(Integer maxSize, ExpireType expireType, Integer expire, TimeUnit timeUnit, Function<K, V> function) {
        CacheBuilder<K, V> cacheBuilder = (CacheBuilder<K, V>) CacheBuilder.newBuilder();
        if (maxSize != null) {
            cacheBuilder.maximumSize(maxSize);
        }
        buildExpireInfo(cacheBuilder, expireType, expire, timeUnit);
        return CacheBuilder.newBuilder().build(
                new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        if (function == null) {
                            log.info("not find String local cache by key: {}", key);
                            return null;
                        }
                        return function.apply(key);
                    }
                });
    }

    private static void buildExpireInfo(CacheBuilder cacheBuilder, ExpireType expireType, Integer expire, TimeUnit timeUnit) {
        if (expireType == null || expire == null || timeUnit == null) {
            return;
        }
        switch (expireType) {
            case EXPIRE_AFTER_WRITE:
                cacheBuilder.expireAfterWrite(expire, timeUnit);
                break;
            default:
                cacheBuilder.expireAfterAccess(expire, timeUnit);
        }

    }

}
