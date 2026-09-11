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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>仅测试纯逻辑方法 {@code getLockKey(String)}，不依赖 Spring 容器与 Redis 连接。</li>
 *   <li>覆盖普通 key、含冒号 key、空 key、含特殊字符 key。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code getLockKey(key)} 返回 {@code key + ":" + "lock"} 的约定。</li>
 *   <li>依据白盒/黑盒原则：取普通、含分隔符、空、特殊字符代表性输入。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：普通、含冒号、空、特殊字符。</li>
 *   <li>未覆盖：{@code lock()}/{@code tryLockThenRun}（依赖注入与 Redis，未单测）。</li>
 * </ul>
 * <h2>getLockKey 锁 key 生成</h2>
 * <ul>
 *   <li>1.1 普通 key：{@code biz} → {@code biz:lock}（testGetLockKey_normal）</li>
 *   <li>1.2 含冒号 key：{@code a:b} → {@code a:b:lock}（testGetLockKey_withColon）</li>
 *   <li>1.3 空 key：{@code ""} → {@code :lock}（testGetLockKey_empty）</li>
 *   <li>1.4 含特殊字符：{@code key:1} → {@code key:1:lock}（testGetLockKey_specialChars）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CLockUtilsTests {

    /**
     * 对应测试用例 1.1：普通 key：{@code biz} → {@code biz:lock}
     */
    @Test
    void testGetLockKey_normal() {
        Assertions.assertEquals("biz:lock", CLockUtils.getLockKey("biz"));
    }

    /**
     * 对应测试用例 1.2：含冒号 key：{@code a:b} → {@code a:b:lock}
     */
    @Test
    void testGetLockKey_withColon() {
        Assertions.assertEquals("a:b:lock", CLockUtils.getLockKey("a:b"));
    }

    /**
     * 对应测试用例 1.3：空 key：{@code ""} → {@code :lock}
     */
    @Test
    void testGetLockKey_empty() {
        Assertions.assertEquals(":lock", CLockUtils.getLockKey(""));
    }

    /**
     * 对应测试用例 1.4：含特殊字符：{@code key:1} → {@code key:1:lock}
     */
    @Test
    void testGetLockKey_specialChars() {
        Assertions.assertEquals("key:1:lock", CLockUtils.getLockKey("key:1"));
    }
}
