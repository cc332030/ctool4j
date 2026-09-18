package com.c332030.ctool4j.cache.aop;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CCacheAspectExpireCacheCapTests
 * </p>
 * <p>
 * 测试 {@link CCacheAspect#getCache(Class, int)} 的<b>分组上限守卫</b>：每个 namespace 下按不同
 * {@code expire} 维护的 Cache 实例数不得超过 {@code MAX_EXPIRE_CACHES_PER_NAMESPACE}，
 * 超限后复用已有 Cache，防止无界增长。
 * </p>
 *
 * <p>
 * 是 {@link CCacheAspect#getCache} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>{@code getCache} 为 private，经反射调用；不依赖 Spring 容器与 Redis（{@code new CCacheAspect(null)}）。</li>
 *   <li>以「请求远超上限个数的不同 expire 值 → 统计返回的 Cache 实例数」验证上限是否生效，
 *   用身份集合（{@link IdentityHashMap}）计数，避免 {@code equals} 干扰。</li>
 *   <li>每个用例用<b>独立的 namespace 类</b>，避免用例间经 {@code NAMESPACE_CACHES}（静态）互相污染。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 {@code CCacheAspect#getCache} 的 javadoc「兜底设计」：最多创建
 *   {@code MAX_EXPIRE_CACHES_PER_NAMESPACE} 个不同 expire 的 Cache 实例，超过阈值时复用已有 Cache。</li>
 *   <li>依据需求：该守卫为"防无界增长"的防御性兜底，须可验证（ISO/IEC Directives Part 2 要求可验证）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：未达上限时按 expire 各建一个（互不复用）、达到并超过上限后实例数不再增长、超限时复用不抛异常。</li>
 *   <li>未覆盖：{@code estimatedSize()} 在并发下的瞬时偏差（Caffeine 统计值特性，非本守卫的逻辑）；复用对象的过期时间取值细节（按最长 expire 复用，属取舍、由实现注释记录）。</li>
 * </ul>
 * <h2>getCache 分组上限</h2>
 * <ul>
 *   <li>1.1 未达上限：不同 expire 各得不同 Cache 实例（testGetCache_belowCap_createsDistinct）</li>
 *   <li>1.2 超过上限：实例数封顶为 MAX_EXPIRE_CACHES_PER_NAMESPACE（testGetCache_overCap_capped）</li>
 *   <li>1.3 已存在相同 expire：返回同一实例（testGetCache_sameExpire_returnsSame）</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.0
 * @see CCacheAspect
 */
class CCacheAspectExpireCacheCapTests {

    /**
     * 用例 1.1 / 1.2 用 namespace：本类独有，避免与其它用例共享静态缓存
     */
    static class CapNamespace {
    }

    /**
     * 用例 1.3 用 namespace：本类独有
     */
    static class SameExpireNamespace {
    }

    /**
     * 上限常量值，经反射读取（与实现保持单一来源，不在测试里写死数字）
     */
    private static int maxExpireCachesPerNamespace() throws Exception {
        Field field = CCacheAspect.class.getDeclaredField("MAX_EXPIRE_CACHES_PER_NAMESPACE");
        field.setAccessible(true);
        return (Integer)field.get(null);
    }

    private static Method getCacheMethod() throws Exception {
        Method method = CCacheAspect.class.getDeclaredMethod("getCache", Class.class, int.class);
        method.setAccessible(true);
        return method;
    }

    /**
     * 对应测试用例 1.1：未达上限时不同 expire 各得不同 Cache 实例
     */
    @Test
    void testGetCache_belowCap_createsDistinct() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();

        // 请求个数小于上限的不同 expire，应各得一个独立 Cache
        val count = Math.max(1, max - 1);
        Map<Object, Boolean> distinct = new IdentityHashMap<>();
        for (int i = 1; i <= count; i++) {
            distinct.put(method.invoke(aspect, CapNamespace.class, i), Boolean.TRUE);
        }

        Assertions.assertEquals(count, distinct.size());
    }

    /**
     * 对应测试用例 1.2：请求数远超上限时，实例数封顶
     *
     * <p>回归点：该守卫一度在 Caffeine 迁移（提交 "feat: caffeine"）中丢失——常量与 javadoc 仍在、
     * 可执行代码却只做 {@code getIfPresent + get}，无任何上限判断，不同 expire 的 Cache 实例无界累积。
     * 本用例请求远超上限个数的 expire 值，断言返回的 distinct Cache 实例数不超过上限。</p>
     */
    @Test
    void testGetCache_overCap_capped() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();

        // 请求数取上限的 2 倍以上，确保必然触发守卫
        val requested = max * 2 + 1;
        Map<Object, Boolean> distinct = new IdentityHashMap<>();
        for (int i = 1; i <= requested; i++) {
            distinct.put(method.invoke(aspect, CapNamespace.class, i), Boolean.TRUE);
        }

        Assertions.assertTrue(distinct.size() <= max,
            "请求 " + requested + " 个不同 expire，实际 Cache 实例数 " + distinct.size() + " 超过上限 " + max);
        // 守卫生效时仍应保留已达上限的实例，不应退化为 0
        Assertions.assertEquals(max, distinct.size());
    }

    /**
     * 对应测试用例 1.3：相同 expire 重复获取返回同一实例
     */
    @Test
    void testGetCache_sameExpire_returnsSame() throws Exception {

        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();

        val first = method.invoke(aspect, SameExpireNamespace.class, 60);
        val second = method.invoke(aspect, SameExpireNamespace.class, 60);

        Assertions.assertSame(first, second);
    }

}
