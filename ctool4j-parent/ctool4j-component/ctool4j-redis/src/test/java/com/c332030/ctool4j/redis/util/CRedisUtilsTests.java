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
 * 是 {@link CRedisUtils} 的测试用例（对应测试文档
 * <code>doc/design/redis/CRedisUtilsTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
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

    /** 对应测试用例 1.1 */
    @Test
    void testGetApplicationPrefix_groupPreferred() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp", CRedisUtils.getApplicationPrefix());
    }

    /** 对应测试用例 1.2 */
    @Test
    void testGetApplicationPrefix_groupEmpty_useName() {
        setConfig("", "name");
        Assertions.assertEquals("name", CRedisUtils.getApplicationPrefix());
    }

    /** 对应测试用例 1.3 */
    @Test
    void testGetApplicationPrefix_groupWhitespace_kept() {
        // hutool emptyToDefault 仅空串/空判空，纯空格按非空处理
        setConfig("  ", "name");
        Assertions.assertEquals("  ", CRedisUtils.getApplicationPrefix());
    }

    /** 对应测试用例 1.4 */
    @Test
    void testGetApplicationPrefix_configNull_throwsNPE() {
        CRedisUtils.setSpringApplicationConfig(null);
        Assertions.assertThrowsExactly(NullPointerException.class, CRedisUtils::getApplicationPrefix);
    }

    // ---------- getKey(Class, Object...) ----------

    /** 对应测试用例 2.1 */
    @Test
    void testGetKey_withPrefixAndKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User:1:2", CRedisUtils.getKey(User.class, 1, 2));
    }

    /** 对应测试用例 2.2 */
    @Test
    void testGetKey_noKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User", CRedisUtils.getKey(User.class));
    }

    /** 对应测试用例 2.3 */
    @Test
    void testGetKey_multiKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User:a:b", CRedisUtils.getKey(User.class, "a", "b"));
    }

    /** 对应测试用例 2.4 */
    @Test
    void testGetKey_prefixFromNameWhenGroupBlank() {
        setConfig("", "app");
        Assertions.assertEquals("app:User:1", CRedisUtils.getKey(User.class, 1));
    }

    // ---------- setIfLager ----------

    /** 对应测试用例 3.1 */
    @Test
    void testSetIfLager_nullValue_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfLager("key", null));
    }

    // ---------- compareAndSet ----------

    /** 对应测试用例 3.2 */
    @Test
    void testCompareAndSet_nullExpected_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, "new"));
    }

    /** 对应测试用例 3.3 */
    @Test
    void testCompareAndSet_nullNew_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", "old", null));
    }

    /** 对应测试用例 3.4 */
    @Test
    void testCompareAndSet_bothNull_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, null));
    }

    /** 对应测试用例 3.5 */
    @Test
    void testCompareAndSet_nullExpectedWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, "new", 100L));
    }

    /** 对应测试用例 3.6 */
    @Test
    void testCompareAndSet_nullNewWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", "old", null, 100L));
    }

    // ---------- setIfNotEquals ----------

    /** 对应测试用例 3.7 */
    @Test
    void testSetIfNotEquals_nullValue_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfNotEquals("key", null));
    }

    /** 对应测试用例 3.8 */
    @Test
    void testSetIfNotEquals_nullValueWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfNotEquals("key", null, 100L));
    }
}
