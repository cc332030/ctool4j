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
 * Description: CCacheAspectResolveCacheKeyTests
 * </p>
 * <p>
 * 测试纯逻辑方法 {@link CCacheAspect#resolveCacheKey}（key() 表达式与默认 @CCacheId 逻辑的
 * 统一分派），不依赖 Spring 容器与 Redis。
 * </p>
 *
 * <p>
 * 是 {@link CCacheAspect#resolveCacheKey} 的测试用例（对应测试文档
 * <code>doc/design/cache/CCacheAspectResolveCacheKeyTests.adoc</code>）。
 * </p>
 *
 * @since 2026/9/8
 */
class CCacheAspectResolveCacheKeyTests {

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

    // ===== 供反射取方法签名与注解（形参名依赖 -parameters 保留）=====

    @CCacheable(namespace = Namespace.class, key = "user.id")
    public String cacheElById(UserWithId user) {
        return null;
    }

    @CCacheable(namespace = Namespace.class, key = "tag")
    public String cacheElSecondParam(UserWithId req, String tag) {
        return null;
    }

    @CCacheable(namespace = Namespace.class, key = "user.id")
    public String cacheElNullId(UserWithId user) {
        return null;
    }

    @CCacheable(namespace = Namespace.class, key = "  ")
    public String cacheElBlank(UserWithId user) {
        return null;
    }

    @CCacheable(namespace = Namespace.class)
    public String cacheDefaultId(UserWithId user) {
        return null;
    }

    @CCacheable(namespace = Namespace.class)
    public String cacheDefaultNoId(UserWithoutId user) {
        return null;
    }

    @CCacheable(namespace = Namespace.class)
    public String cacheNoArgs() {
        return null;
    }

    @CCacheable(namespace = Namespace.class)
    public String cacheDefaultJdk(String name) {
        return null;
    }

    private Method method(String name) {
        for (Method m : CCacheAspectResolveCacheKeyTests.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalStateException("no method: " + name);
    }

    private CCacheable cacheable(Method m) {
        return m.getAnnotation(CCacheable.class);
    }

    /** 对应测试用例 2.1：key() 表达式取值并经默认 idConverter 生成 key（多级取业务 id） */
    @Test
    void testResolveCacheKey_elPropertyValue() {
        Method m = method("cacheElById");
        UserWithId user = new UserWithId(42L, "name");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        Assertions.assertEquals("42", key);
    }

    /** 对应测试用例 2.2：key() 表达式参数为 null 返回 null（跳过缓存） */
    @Test
    void testResolveCacheKey_elParamNull_returnsNull() {
        Method m = method("cacheElById");
        String key = aspect.resolveCacheKey(new Object[] { null }, m, cacheable(m));
        Assertions.assertNull(key);
    }

    /** 对应测试用例 2.3：key() 表达式属性值为 null 返回 null（跳过缓存） */
    @Test
    void testResolveCacheKey_elPropNull_returnsNull() {
        Method m = method("cacheElNullId");
        UserWithId user = new UserWithId(null, "name");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        Assertions.assertNull(key);
    }

    /** 对应测试用例 2.4：key() 表达式引用第二个参数 */
    @Test
    void testResolveCacheKey_elSecondParam() {
        Method m = method("cacheElSecondParam");
        UserWithId req = new UserWithId(1L, "req");
        String key = aspect.resolveCacheKey(new Object[] { req, "TAG" }, m, cacheable(m));
        Assertions.assertEquals("TAG", key);
    }

    /** 对应测试用例 2.5：key() 为空白视为未配置，走默认 @CCacheId 逻辑（不报错） */
    @Test
    void testResolveCacheKey_elBlank_goesDefault() {
        Method m = method("cacheElBlank");
        UserWithId user = new UserWithId(1L, "n");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        // 空白 key 视为"未配 el"，回落到默认逻辑取 @CCacheId
        Assertions.assertEquals("1", key);
    }

    /** 对应测试用例 2.6：key() 为空走默认逻辑，@CCacheId 取字段 */
    @Test
    void testResolveCacheKey_defaultId() {
        Method m = method("cacheDefaultId");
        UserWithId user = new UserWithId(42L, "name");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        Assertions.assertEquals("42", key);
    }

    /** 对应测试用例 2.7：key() 为空、默认逻辑无 @CCacheId 时报错 */
    @Test
    void testResolveCacheKey_defaultNoId_throws() {
        Method m = method("cacheDefaultNoId");
        UserWithoutId user = new UserWithoutId(42L, "name");
        Assertions.assertThrowsExactly(IllegalStateException.class,
            () -> aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m)));
    }

    /** 对应测试用例 2.8：key() 为空、默认逻辑 JDK 类参数直接作 key */
    @Test
    void testResolveCacheKey_defaultJdk() {
        Method m = method("cacheDefaultJdk");
        String key = aspect.resolveCacheKey(new Object[] { "hello" }, m, cacheable(m));
        Assertions.assertEquals("hello", key);
    }

    /** 对应测试用例 2.9：方法无参数返回 null（跳过缓存） */
    @Test
    void testResolveCacheKey_noArgs_returnsNull() {
        Method m = method("cacheNoArgs");
        String key = aspect.resolveCacheKey(new Object[0], m, cacheable(m));
        Assertions.assertNull(key);
    }

    /** 对应测试用例 2.10：方法无参数但 key() 表达式引用参数，首次使用抛异常 */
    @Test
    void testResolveCacheKey_elNoArgs_throws() {
        // 用带 key() 但引用不存在参数名的方法（无参），参数名必然找不到
        Method m = method("cacheNoArgs");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> aspect.resolveCacheKey(new Object[0], m,
                cacheable(method("cacheElById"))));
    }

}
