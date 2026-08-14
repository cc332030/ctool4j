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

    @Test
    void testGetApplicationPrefix_groupPreferred() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp", CRedisUtils.getApplicationPrefix());
    }

    @Test
    void testGetApplicationPrefix_groupEmpty_useName() {
        setConfig("", "name");
        Assertions.assertEquals("name", CRedisUtils.getApplicationPrefix());
    }

    @Test
    void testGetApplicationPrefix_groupWhitespace_kept() {
        // hutool emptyToDefault 仅空串/空判空，纯空格按非空处理
        setConfig("  ", "name");
        Assertions.assertEquals("  ", CRedisUtils.getApplicationPrefix());
    }

    @Test
    void testGetApplicationPrefix_configNull_throwsNPE() {
        CRedisUtils.setSpringApplicationConfig(null);
        Assertions.assertThrowsExactly(NullPointerException.class, CRedisUtils::getApplicationPrefix);
    }

    // ---------- getKey(Class, Object...) ----------

    @Test
    void testGetKey_withPrefixAndKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User:1:2", CRedisUtils.getKey(User.class, 1, 2));
    }

    @Test
    void testGetKey_noKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User", CRedisUtils.getKey(User.class));
    }

    @Test
    void testGetKey_multiKeys() {
        setConfig("grp", "name");
        Assertions.assertEquals("grp:User:a:b", CRedisUtils.getKey(User.class, "a", "b"));
    }

    @Test
    void testGetKey_prefixFromNameWhenGroupBlank() {
        setConfig("", "app");
        Assertions.assertEquals("app:User:1", CRedisUtils.getKey(User.class, 1));
    }

    // ---------- setIfLager ----------

    @Test
    void testSetIfLager_nullValue_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfLager("key", null));
    }

    // ---------- compareAndSet ----------

    @Test
    void testCompareAndSet_nullExpected_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, "new"));
    }

    @Test
    void testCompareAndSet_nullNew_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", "old", null));
    }

    @Test
    void testCompareAndSet_bothNull_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, null));
    }

    @Test
    void testCompareAndSet_nullExpectedWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", null, "new", 100L));
    }

    @Test
    void testCompareAndSet_nullNewWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.compareAndSet("key", "old", null, 100L));
    }

    // ---------- setIfNotEquals ----------

    @Test
    void testSetIfNotEquals_nullValue_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfNotEquals("key", null));
    }

    @Test
    void testSetIfNotEquals_nullValueWithTtl_returnsFalse() {
        Assertions.assertFalse(CRedisUtils.setIfNotEquals("key", null, 100L));
    }
}
