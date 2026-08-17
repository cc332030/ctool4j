package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CLocalCacheUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLocalCacheUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CLocalCacheUtilsTests {

    @Test
    public void cacheBuilder() {

        Caffeine<String, Integer> builder = CLocalCacheUtils.cacheBuilder();

        Assertions.assertNotNull(builder);

        Cache<String, Integer> cache = builder.build();
        cache.put("k", 1);
        Assertions.assertEquals(1, cache.getIfPresent("k"));

    }

    @Test
    public void buildCache() {

        Cache<String, Integer> cache = CLocalCacheUtils.buildCache();

        Assertions.assertNotNull(cache);

        cache.put("a", 100);
        Assertions.assertEquals(100, cache.getIfPresent("a"));
        Assertions.assertNull(cache.getIfPresent("missing"));

    }

}
