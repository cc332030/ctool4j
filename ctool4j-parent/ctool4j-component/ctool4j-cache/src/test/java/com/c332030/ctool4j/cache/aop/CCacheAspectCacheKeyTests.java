package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: CCacheAspectCacheKeyTests
 * </p>
 * <p>
 * 仅测试纯逻辑方法 {@link CCacheAspect#getCacheKey}，
 * 不依赖 Spring 容器与 Redis。
 * </p>
 *
 * <p>
 * 是 {@link CCacheAspect#getCacheKey} 的测试用例（对应测试文档
 * <code>doc/design/cache/CCacheAspectCacheKeyTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
 */
class CCacheAspectCacheKeyTests {

    @Data
    @AllArgsConstructor
    static class Namespace {
    }

    @Data
    @AllArgsConstructor
    static class UserWithId {

        @CCacheId
        private Long id;

        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UserWithoutId {

        private Long id;

        private String name;
    }

    private final CCacheAspect aspect = new CCacheAspect(null);

    private CCacheable cacheable() {
        try {
            Method m = getClass().getMethod("annotatedMethod");
            return m.getAnnotation(CCacheable.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @CCacheable(namespace = Namespace.class)
    public void annotatedMethod() {
    }

    /** 对应测试用例 1.1：JDK 类 String 作 key */
    @Test
    void testGetCacheKey_jdkClassString() {
        CCacheable cacheable = cacheable();
        String key = aspect.getCacheKey("hello", cacheable);
        Assertions.assertEquals("hello", key);
    }

    /** 对应测试用例 1.2：JDK 类 Integer 作 key */
    @Test
    void testGetCacheKey_jdkClassInteger() {
        CCacheable cacheable = cacheable();
        String key = aspect.getCacheKey(123, cacheable);
        Assertions.assertEquals("123", key);
    }

    /** 对应测试用例 1.3：POJO 带 @CCacheId 取字段值 */
    @Test
    void testGetCacheKey_pojoWithCacheId() {
        CCacheable cacheable = cacheable();
        UserWithId user = new UserWithId(42L, "name");
        String key = aspect.getCacheKey(user, cacheable);
        // cacheId = 42，默认转换器取 key 即 cacheId
        Assertions.assertEquals("42", key);
    }

    /** 对应测试用例 1.4：POJO 无 @CCacheId 取 toString */
    @Test
    void testGetCacheKey_pojoWithoutCacheId_cacheIdNull() {
        CCacheable cacheable = cacheable();
        UserWithoutId user = new UserWithoutId(42L, "name");
        String key = aspect.getCacheKey(user, cacheable);
        // 无 @CCacheId 字段，cacheId 为 null，则取 object.toString()
        Assertions.assertEquals(user.toString(), key);
    }

    /** 对应测试用例 1.5：object 为 null 返回 null */
    @Test
    void testGetCacheKey_cacheIdNullButObjectNull_returnsNull() {
        // getCacheKey 对 null object 返回 null（由调用方保证不写入缓存）
        Assertions.assertNull(aspect.getCacheKey(null, cacheable()));
    }
}
