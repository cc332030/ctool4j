package com.c332030.ctool4j.redis.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLockUtilsTests
 * </p>
 * <p>
 * 仅测试纯逻辑方法 {@link CLockUtils#getLockKey(String)}，
 * 不依赖 Spring 容器与 Redis 连接。
 * </p>
 *
 * @since 2026/8/14
 */
class CLockUtilsTests {

    @Test
    void testGetLockKey_normal() {
        Assertions.assertEquals("biz:lock", CLockUtils.getLockKey("biz"));
    }

    @Test
    void testGetLockKey_withColon() {
        Assertions.assertEquals("a:b:lock", CLockUtils.getLockKey("a:b"));
    }

    @Test
    void testGetLockKey_empty() {
        Assertions.assertEquals(":lock", CLockUtils.getLockKey(""));
    }

    @Test
    void testGetLockKey_specialChars() {
        Assertions.assertEquals("key:1:lock", CLockUtils.getLockKey("key:1"));
    }
}
