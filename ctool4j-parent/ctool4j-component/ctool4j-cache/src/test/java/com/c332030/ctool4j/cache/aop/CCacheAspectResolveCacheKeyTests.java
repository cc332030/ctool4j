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
 * 是 {@link CCacheAspect#resolveCacheKey} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>仅测试纯逻辑方法 {@code resolveCacheKey}，不依赖 Spring 容器与 Redis（{@code new CCacheAspect(null)}）。</li>
 *   <li>通过反射取带 {@code @CCacheable} 注解与形参名（{@code -parameters}）的测试方法及其注解，覆盖 el 分支与默认分支的分派。</li>
 *   <li>分派规则：{@code key()} 非空白走 el；空白/为空走默认（第一参数 + {@code @CCacheId}）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 {@code CCacheAspect.resolveCacheKey} 与 {@code CCacheable.key()} 约定。</li>
 *   <li>依据需求：key() 非空走 el；为空走默认；默认逻辑下 POJO 无 @CCacheId 报错。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：el 多级取属性、el 参数 null、el 属性值 null、el 引用第二参数、el 空白回落默认、默认 @CCacheId、默认无 @CCacheId 报错、默认 JDK 参数、无参数跳过、el 无参数报错、
 *   <strong>同一方法被两个不同表达式解析时 key 不串用</strong>。</li>
 *   <li>未覆盖：真实 Spring AOP 拦截的端到端缓存命中/未命中（集成测试 CCacheAspectTests）；本地/Redis 缓存写读。</li>
 * </ul>
 * <h2>resolveCacheKey 分派</h2>
 * <ul>
 *   <li>2.1 key() 表达式多级取属性并经默认 idConverter 生成 key（testResolveCacheKey_elPropertyValue）</li>
 *   <li>2.2 key() 参数为 null：返回 null（testResolveCacheKey_elParamNull_returnsNull）</li>
 *   <li>2.3 key() 属性值为 null：返回 null（testResolveCacheKey_elPropNull_returnsNull）</li>
 *   <li>2.4 key() 引用第二个参数（testResolveCacheKey_elSecondParam）</li>
 *   <li>2.5 key() 为空白：视为未配置，回落默认 @CCacheId 逻辑（testResolveCacheKey_elBlank_goesDefault）</li>
 *   <li>2.6 key() 为空走默认逻辑，@CCacheId 取字段（testResolveCacheKey_defaultId）</li>
 *   <li>2.7 key() 为空、默认逻辑无 @CCacheId：报错（testResolveCacheKey_defaultNoId_throws）</li>
 *   <li>2.8 key() 为空、默认逻辑 JDK 类参数直接作 key（testResolveCacheKey_defaultJdk）</li>
 *   <li>2.9 方法无参数：返回 null（testResolveCacheKey_noArgs_returnsNull）</li>
 *   <li>2.10 方法无参数但 key() 引用参数：报错（testResolveCacheKey_elNoArgs_throws）</li>
 *   <li>2.11 同一方法被两个不同表达式解析：各自生成不同 key（testResolveCacheKey_sameMethodTwoExprs_distinctKeys）</li>
 *   <li>2.12 同一方法两个表达式反复交替解析：key 始终各归各（testResolveCacheKey_sameMethodTwoExprs_repeat）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/9/8
 * @version 1.0
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

    /**
     * cacheElById
     */
    @CCacheable(namespace = Namespace.class, key = "user.id")
    public String cacheElById(UserWithId user) {
        return null;
    }

    /**
     * cacheSameMethodTwoExprs：同方法两个表达式用例（2.11/2.12）的样例方法。
     *
     * <p>取两个不同的、都能在该方法参数上取到值的表达式用于「同一 Method」：
     * {@code "user.id"} 与 {@code "user.name"}。生产上同一 {@code Method} 承载多个表达式，
     * 正是 {@code @CCacheable.key()} 与 {@code @CCacheRemove.key()}/{@code @CCacheUpdate.key()}
     * 各配一套（如缓存放 {@code id}、删除按 {@code name}）的实际形态。</p>
     */
    @CCacheable(namespace = Namespace.class, key = "user.id")
    public String cacheSameMethodTwoExprs(UserWithId user) {
        return null;
    }

    /**
     * cacheElSecondParam
     */
    @CCacheable(namespace = Namespace.class, key = "tag")
    public String cacheElSecondParam(UserWithId req, String tag) {
        return null;
    }

    /**
     * cacheElNullId
     */
    @CCacheable(namespace = Namespace.class, key = "user.id")
    public String cacheElNullId(UserWithId user) {
        return null;
    }

    /**
     * cacheElBlank
     */
    @CCacheable(namespace = Namespace.class, key = "  ")
    public String cacheElBlank(UserWithId user) {
        return null;
    }

    /**
     * cacheDefaultId
     */
    @CCacheable(namespace = Namespace.class)
    public String cacheDefaultId(UserWithId user) {
        return null;
    }

    /**
     * cacheDefaultNoId
     */
    @CCacheable(namespace = Namespace.class)
    public String cacheDefaultNoId(UserWithoutId user) {
        return null;
    }

    /**
     * cacheNoArgs
     */
    @CCacheable(namespace = Namespace.class)
    public String cacheNoArgs() {
        return null;
    }

    /**
     * cacheDefaultJdk
     */
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

    /**
     * 对应测试用例 2.1：key() 表达式取值并经默认 idConverter 生成 key（多级取业务 id）
     */
    @Test
    void testResolveCacheKey_elPropertyValue() {
        Method m = method("cacheElById");
        UserWithId user = new UserWithId(42L, "name");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        Assertions.assertEquals("42", key);
    }

    /**
     * 对应测试用例 2.2：key() 表达式参数为 null 返回 null（跳过缓存）
     */
    @Test
    void testResolveCacheKey_elParamNull_returnsNull() {
        Method m = method("cacheElById");
        String key = aspect.resolveCacheKey(new Object[] { null }, m, cacheable(m));
        Assertions.assertNull(key);
    }

    /**
     * 对应测试用例 2.3：key() 表达式属性值为 null 返回 null（跳过缓存）
     */
    @Test
    void testResolveCacheKey_elPropNull_returnsNull() {
        Method m = method("cacheElNullId");
        UserWithId user = new UserWithId(null, "name");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        Assertions.assertNull(key);
    }

    /**
     * 对应测试用例 2.4：key() 表达式引用第二个参数
     */
    @Test
    void testResolveCacheKey_elSecondParam() {
        Method m = method("cacheElSecondParam");
        UserWithId req = new UserWithId(1L, "req");
        String key = aspect.resolveCacheKey(new Object[] { req, "TAG" }, m, cacheable(m));
        Assertions.assertEquals("TAG", key);
    }

    /**
     * 对应测试用例 2.5：key() 为空白视为未配置，走默认 @CCacheId 逻辑（不报错）
     */
    @Test
    void testResolveCacheKey_elBlank_goesDefault() {
        Method m = method("cacheElBlank");
        UserWithId user = new UserWithId(1L, "n");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        // 空白 key 视为"未配 el"，回落到默认逻辑取 @CCacheId
        Assertions.assertEquals("1", key);
    }

    /**
     * 对应测试用例 2.6：key() 为空走默认逻辑，@CCacheId 取字段
     */
    @Test
    void testResolveCacheKey_defaultId() {
        Method m = method("cacheDefaultId");
        UserWithId user = new UserWithId(42L, "name");
        String key = aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m));
        Assertions.assertEquals("42", key);
    }

    /**
     * 对应测试用例 2.7：key() 为空、默认逻辑无 @CCacheId 时报错
     */
    @Test
    void testResolveCacheKey_defaultNoId_throws() {
        Method m = method("cacheDefaultNoId");
        UserWithoutId user = new UserWithoutId(42L, "name");
        Assertions.assertThrowsExactly(IllegalStateException.class,
            () -> aspect.resolveCacheKey(new Object[] { user }, m, cacheable(m)));
    }

    /**
     * 对应测试用例 2.8：key() 为空、默认逻辑 JDK 类参数直接作 key
     */
    @Test
    void testResolveCacheKey_defaultJdk() {
        Method m = method("cacheDefaultJdk");
        String key = aspect.resolveCacheKey(new Object[] { "hello" }, m, cacheable(m));
        Assertions.assertEquals("hello", key);
    }

    /**
     * 对应测试用例 2.9：方法无参数返回 null（跳过缓存）
     */
    @Test
    void testResolveCacheKey_noArgs_returnsNull() {
        Method m = method("cacheNoArgs");
        String key = aspect.resolveCacheKey(new Object[0], m, cacheable(m));
        Assertions.assertNull(key);
    }

    /**
     * 对应测试用例 2.10：方法无参数但 key() 表达式引用参数，首次使用抛异常
     */
    @Test
    void testResolveCacheKey_elNoArgs_throws() {
        // 用带 key() 但引用不存在参数名的方法（无参），参数名必然找不到
        Method m = method("cacheNoArgs");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> aspect.resolveCacheKey(new Object[0], m,
                cacheable(method("cacheElById"))));
    }

    /**
     * 对应测试用例 2.11：同一方法被两个不同表达式解析时，各自生成不同的 key
     *
     * <p>回归点：el 解析器缓存曾只以 {@code Method} 为 key——同一方法先解析的表达式会成为其
     * 后续所有调用的解析器，后一个表达式静默沿用前一个，缓存 key / 限流与幂等业务 id 全部错位。
     * 本用例从**真实调用入口** {@link CCacheAspect#resolveCacheKey}(args, method, namespace, keyExpr, idConverterClass)
     * 出发，对**同一个 Method** 先后用 {@code "user.id"} 与 {@code "user.name"} 两个表达式取 key，
     * 断言两次结果分别对应各自表达式的取值（{@code "42"} 与 {@code "name"}），不得串用。</p>
     */
    @Test
    void testResolveCacheKey_sameMethodTwoExprs_distinctKeys() {

        Method m = method("cacheSameMethodTwoExprs");
        UserWithId user = new UserWithId(42L, "name");
        Object[] args = new Object[] { user };
        Class<? extends ICCacheIdConverter<?, ?>> converter = CDefaultCacheIdConverter.class;

        String keyById = aspect.resolveCacheKey(args, m, Namespace.class, "user.id", converter);
        String keyByName = aspect.resolveCacheKey(args, m, Namespace.class, "user.name", converter);

        Assertions.assertEquals("42", keyById);
        Assertions.assertEquals("name", keyByName);
        Assertions.assertNotEquals(keyById, keyByName);
    }

    /**
     * 对应测试用例 2.12：同一方法两个表达式反复交替解析，key 始终各归各
     *
     * <p>与 2.11 互补：2.11 只覆盖「先 A 后 B」一次；本用例反复交替，确认解析器缓存的**命中路径**
     * （{@code getIfPresent} 直接返回）也不会因二次命中而串用表达式。</p>
     */
    @Test
    void testResolveCacheKey_sameMethodTwoExprs_repeat() {

        Method m = method("cacheSameMethodTwoExprs");
        UserWithId user = new UserWithId(7L, "n7");
        Object[] args = new Object[] { user };
        Class<? extends ICCacheIdConverter<?, ?>> converter = CDefaultCacheIdConverter.class;

        for (int i = 0; i < 3; i++) {
            Assertions.assertEquals("7",
                aspect.resolveCacheKey(args, m, Namespace.class, "user.id", converter));
            Assertions.assertEquals("n7",
                aspect.resolveCacheKey(args, m, Namespace.class, "user.name", converter));
        }
    }

}
