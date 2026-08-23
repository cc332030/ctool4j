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
 * @see "doc/design/core/CLocalCacheUtils.adoc"
 * @see "doc/design/core/CLocalCacheUtilsTests.adoc"
 */
@UtilityClass
public class CLocalCacheUtils {

    /**
     * 获取本地缓存构建器（内存不足时释放缓存）
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    @SuppressWarnings("unchecked")
    public <K, V> Caffeine<K, V> cacheBuilder() {
        return (Caffeine<K, V>)Caffeine.newBuilder()
            // 内存不足释放缓存
            .softValues()
            ;
    }

    /**
     * 构建本地缓存
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 本地缓存
     */
    public <K, V> Cache<K, V> buildCache() {
        return cacheBuilder()
            .build();
    }

}
