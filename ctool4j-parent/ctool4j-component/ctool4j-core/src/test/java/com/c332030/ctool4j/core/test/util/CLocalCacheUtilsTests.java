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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构建器 / 直接构建」两个维度组织。</li>
 *   <li>cacheBuilder：验证返回非空构建器，且 build 后能正常 put/get。</li>
 *   <li>buildCache：验证返回可用缓存，put 后 getIfPresent 命中、缺失键返回 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对缓存可用的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：缓存读写、缺失键。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：cacheBuilder 可构建可用缓存；buildCache 读写与缺失键返回 null。</li>
 *   <li>未覆盖：内存不足时软引用的实际回收行为（依赖 JVM GC，单测不构造内存压力）。</li>
 * </ul>
 * <h2>构建器（cacheBuilder）</h2>
 * <ul>
 *   <li>1.1 构建器可用：build 后 put/get 正常（cacheBuilder）</li>
 * </ul>
 * <h2>直接构建（buildCache）</h2>
 * <ul>
 *   <li>2.1 缓存可用：put 后命中，缺失键返回 null（buildCache）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CLocalCacheUtilsTests {

    /**
     * 对应测试用例 1.1：构建器可用：build 后 put/get 正常
     */
    @Test
    public void cacheBuilder() {

        Caffeine<String, Integer> builder = CLocalCacheUtils.cacheBuilder();

        Assertions.assertNotNull(builder);

        Cache<String, Integer> cache = builder.build();
        cache.put("k", 1);
        Assertions.assertEquals(1, cache.getIfPresent("k"));

    }

    /**
     * 对应测试用例 2.1：缓存可用：put 后命中，缺失键返回 null
     */
    @Test
    public void buildCache() {

        Cache<String, Integer> cache = CLocalCacheUtils.buildCache();

        Assertions.assertNotNull(cache);

        cache.put("a", 100);
        Assertions.assertEquals(100, cache.getIfPresent("a"));
        Assertions.assertNull(cache.getIfPresent("missing"));

    }

}
