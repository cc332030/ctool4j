package com.c332030.ctool4j.core.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CLocalCacheUtils
 * </p>
 *
 * @since 2026/6/17
 */
@UtilityClass
public class CLocalCacheUtils {

    @SuppressWarnings("unchecked")
    public <K, V> Caffeine<K, V> cacheBuilder() {
        return (Caffeine<K, V>)Caffeine.newBuilder()
            .weakKeys()
            .softValues()
            ;
    }

    public <K, V> Cache<K, V> buildCache() {
        return cacheBuilder()
            .build();
    }

}
