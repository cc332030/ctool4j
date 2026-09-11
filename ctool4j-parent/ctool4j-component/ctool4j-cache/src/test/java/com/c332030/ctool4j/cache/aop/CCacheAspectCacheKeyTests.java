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
 * 是 {@link CCacheAspect#getCacheKey} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>仅测试纯逻辑方法 {@code getCacheKey}，不依赖 Spring 容器与 Redis。</li>
 *   <li>覆盖入参对象形态：JDK 类（String/Integer）、带 {@code @CCacheId} 字段的 POJO、无 {@code @CCacheId} 字段的 POJO、null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code getCacheKey} 的约定：JDK 类 cacheId 为 null、带 {@code @CCacheId} 取字段值、</li>
 *   <li>无 {@code @CCacheId} cacheId 为 null（退 object.toString）、object 为 null 返回 null。</li>
 *   <li>依据白盒/黑盒原则：覆盖 JDK 类、POJO（有无 @CCacheId）、null 边界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：JDK String/Integer、POJO 带/不带 @CCacheId、null 对象。</li>
 *   <li>未覆盖：多 {@code @CCacheId} 字段（只取第一个，语义由实现保证）；复合 key 组装（由 idConverter 承担）。</li>
 * </ul>
 * <h2>getCacheKey 缓存 key 生成</h2>
 * <ul>
 *   <li>1.1 JDK 类 String：key 为对象字符串（testGetCacheKey_jdkClassString）</li>
 *   <li>1.2 JDK 类 Integer：key 为对象字符串（testGetCacheKey_jdkClassInteger）</li>
 *   <li>1.3 POJO 带 @CCacheId：取字段值作为 cacheId（testGetCacheKey_pojoWithCacheId）</li>
 *   <li>1.4 POJO 无 @CCacheId 且未配 key()：报错（testGetCacheKey_pojoWithoutCacheId_throws）</li>
 *   <li>1.5 null 对象：返回 null（testGetCacheKey_cacheIdNullButObjectNull_returnsNull）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/8/14
 * @version 1.0
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

    /**
     * 用于反射读取 {@code @CCacheable} 注解与形参名的夹具方法
     */
    @CCacheable(namespace = Namespace.class)
    public void annotatedMethod() {
    }

    /**
     * 对应测试用例 1.1：JDK 类 String 作 key
     */
    @Test
    void testGetCacheKey_jdkClassString() {
        CCacheable cacheable = cacheable();
        String key = aspect.getCacheKey("hello", cacheable);
        Assertions.assertEquals("hello", key);
    }

    /**
     * 对应测试用例 1.2：JDK 类 Integer 作 key
     */
    @Test
    void testGetCacheKey_jdkClassInteger() {
        CCacheable cacheable = cacheable();
        String key = aspect.getCacheKey(123, cacheable);
        Assertions.assertEquals("123", key);
    }

    /**
     * 对应测试用例 1.3：POJO 带 @CCacheId 取字段值
     */
    @Test
    void testGetCacheKey_pojoWithCacheId() {
        CCacheable cacheable = cacheable();
        UserWithId user = new UserWithId(42L, "name");
        String key = aspect.getCacheKey(user, cacheable);
        // cacheId = 42，默认转换器取 key 即 cacheId
        Assertions.assertEquals("42", key);
    }

    /**
     * 对应测试用例 1.4：POJO 无 @CCacheId 且未配 key()，应报错（需显式配 key() 或 @CCacheId）
     */
    @Test
    void testGetCacheKey_pojoWithoutCacheId_throws() {
        CCacheable cacheable = cacheable();
        UserWithoutId user = new UserWithoutId(42L, "name");
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> aspect.getCacheKey(user, cacheable));
    }

    /**
     * 对应测试用例 1.5：object 为 null 返回 null
     */
    @Test
    void testGetCacheKey_cacheIdNullButObjectNull_returnsNull() {
        // getCacheKey 对 null object 返回 null（由调用方保证不写入缓存）
        Assertions.assertNull(aspect.getCacheKey(null, cacheable()));
    }
}
