package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRedisUtilsTests
 * </p>
 * <p>
 * 仅测试不依赖真实 Redis 连接的纯逻辑方法。
 * </p>
 *
 * <p>
 * 是 {@link CRedisUtils} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖应用前缀获取（分组优先、分组空退应用名、分组空白保留、config null 抛 NPE）。</li>
 *   <li>覆盖 key 生成（有/无/多 key 段、前缀来自应用名当分组空白）。</li>
 *   <li>覆盖原子操作入参 null 的短路返回（setIfLager/compareAndSet/setIfNotEquals 系列）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code getApplicationPrefix} 分组优先、{@code getKey} 拼接规则、原子操作 null 返回 false 的约定。</li>
 *   <li>依据白盒/黑盒原则：前缀来源、key 段数量、原子操作 null 入参均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：前缀获取（含 config null 抛 NPE）、key 生成（各 key 段形态）、原子操作 null 短路。</li>
 *   <li>未覆盖：Lua 脚本在真实 Redis 上的执行结果（依赖 Redis 环境，未单测）。</li>
 *   <li>脚本契约验证：{@code setIfNotEquals} 的 Lua 脚本参数约定（{@code ARGV[1]}=新值（比较）、{@code ARGV[2]}=新值（写入）、{@code ARGV[3]}=TTL）已在实现修复时用 Lua 解释器模拟验证——修复前写入的是 TTL 字符串、且未设置过期。</li>
 * </ul>
 * <h2>getApplicationPrefix 应用前缀</h2>
 * <ul>
 *   <li>1.1 分组优先（testGetApplicationPrefix_groupPreferred）</li>
 *   <li>1.2 分组空退应用名（testGetApplicationPrefix_groupEmpty_useName）</li>
 *   <li>1.3 分组空白保留（testGetApplicationPrefix_groupWhitespace_kept）</li>
 *   <li>1.4 config null 抛 NPE（testGetApplicationPrefix_configNull_throwsNPE）</li>
 * </ul>
 * <h2>getKey key 生成</h2>
 * <ul>
 *   <li>2.1 带前缀与单个 key（testGetKey_withPrefixAndKeys）</li>
 *   <li>2.2 无 key 段（testGetKey_noKeys）</li>
 *   <li>2.3 多 key 段（testGetKey_multiKeys）</li>
 *   <li>2.4 分组空白时前缀取自应用名（testGetKey_prefixFromNameWhenGroupBlank）</li>
 * </ul>
 * <h2>原子操作入参校验</h2>
 * <ul>
 *   <li>3.1 setIfLager null 值返回 false（testSetIfLager_nullValue_returnsFalse）</li>
 *   <li>3.2 compareAndSet 期望值 null 返回 false（testCompareAndSet_nullExpected_returnsFalse）</li>
 *   <li>3.3 compareAndSet 新值 null 返回 false（testCompareAndSet_nullNew_returnsFalse）</li>
 *   <li>3.4 compareAndSet 双 null 返回 false（testCompareAndSet_bothNull_returnsFalse）</li>
 *   <li>3.5 compareAndSet 期望值 null（带 ttl）返回 false（testCompareAndSet_nullExpectedWithTtl_returnsFalse）</li>
 *   <li>3.6 compareAndSet 新值 null（带 ttl）返回 false（testCompareAndSet_nullNewWithTtl_returnsFalse）</li>
 *   <li>3.7 setIfNotEquals 新值 null 返回 false（testSetIfNotEquals_nullValue_returnsFalse）</li>
 *   <li>3.8 setIfNotEquals 新值 null（带 ttl）返回 false（testSetIfNotEquals_nullValueWithTtl_returnsFalse）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CRedisUtilsTests {

    private static class User {

    }

    private void setConfig(String group, String name) {
        CSpringApplicationConfig config = new CSpringApplicationConfig();
        config.setGroup(group);
        config.setName(name);
        CRedisUtils.setSpringApplicationConfig(config);
    }

    @AfterEach
    void tearDown() {
        CRedisUtils.setSpringApplicationConfig(null);
    }

    // ---------- getApplicationPrefix ----------

    /**
     * 对应测试用例 1.1：分组优先
     */
    @Test
    void testGetApplicationPrefix_groupPreferred() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp", CRedisUtils.getApplicationPrefix());
    }

    /**
     * 对应测试用例 1.2：分组空退应用名
     */
    @Test
    void testGetApplicationPrefix_groupEmpty_useName() {
        setConfig("", "name");
        Assertions.assertEquals("name", CRedisUtils.getApplicationPrefix());
    }

    /**
     * 对应测试用例 1.3：分组空白保留
     */
    @Test
    void testGetApplicationPrefix_groupWhitespace_kept() {
        // hutool emptyToDefault 仅空串/空判空，纯空格按非空处理
        setConfig("  ", "name");
        Assertions.assertEquals("  ", CRedisUtils.getApplicationPrefix());
    }

    /**
     * 对应测试用例 1.4：config null 抛 NPE
     */
    @Test
    void testGetApplicationPrefix_configNull_throwsNPE() {
        CRedisUtils.setSpringApplicationConfig(null);
        Assertions.assertThrowsExactly(NullPointerException.class, CRedisUtils::getApplicationPrefix);
    }

    // ---------- getKey(Class, Object...) ----------

    /**
     * 对应测试用例 2.1：带前缀与单个 key
     */
    @Test
    void testGetKey_withPrefixAndKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User:1:2", CRedisUtils.getKey(User.class, 1, 2));
    }

    /**
     * 对应测试用例 2.2：无 key 段
     */
    @Test
    void testGetKey_noKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User", CRedisUtils.getKey(User.class));
    }

    /**
     * 对应测试用例 2.3：多 key 段
     */
    @Test
    void testGetKey_multiKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User:a:b", CRedisUtils.getKey(User.class, "a", "b"));
    }

    /**
     * 对应测试用例 2.4：分组空白时前缀取自应用名
     */
    @Test
    void testGetKey_prefixFromNameWhenGroupBlank() {
        setConfig("", "app");
        Assertions.assertEquals("app:User:1", CRedisUtils.getKey(User.class, 1));
    }

    // ---------- setIfLager ----------

    /**
     * 对应测试用例 3.1：setIfLager null 值返回 false
     */
    @Test
    void testSetIfLager_nullValue_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfLager("key", null));
    }

    // ---------- compareAndSet ----------

    /**
     * 对应测试用例 3.2：compareAndSet 期望值 null 返回 false
     */
    @Test
    void testCompareAndSet_nullExpected_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, "new"));
    }

    /**
     * 对应测试用例 3.3：compareAndSet 新值 null 返回 false
     */
    @Test
    void testCompareAndSet_nullNew_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", "old", null));
    }

    /**
     * 对应测试用例 3.4：compareAndSet 双 null 返回 false
     */
    @Test
    void testCompareAndSet_bothNull_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, null));
    }

    /**
     * 对应测试用例 3.5：compareAndSet 期望值 null（带 ttl）返回 false
     */
    @Test
    void testCompareAndSet_nullExpectedWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, "new", 100L));
    }

    /**
     * 对应测试用例 3.6：compareAndSet 新值 null（带 ttl）返回 false
     */
    @Test
    void testCompareAndSet_nullNewWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", "old", null, 100L));
    }

    // ---------- setIfNotEquals ----------

    /**
     * 对应测试用例 3.7：setIfNotEquals 新值 null 返回 false
     */
    @Test
    void testSetIfNotEquals_nullValue_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfNotEquals("key", null));
    }

    /**
     * 对应测试用例 3.8：setIfNotEquals 新值 null（带 ttl）返回 false
     */
    @Test
    void testSetIfNotEquals_nullValueWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfNotEquals("key", null, 100L));
    }
}
