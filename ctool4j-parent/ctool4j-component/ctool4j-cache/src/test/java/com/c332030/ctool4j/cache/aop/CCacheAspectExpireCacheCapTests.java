package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.annotation.CCacheUpdate;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
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
 *   <li>覆盖：未达上限时按 expire 各建一个（互不复用）、达到并超过上限后实例数不再增长、超限时复用不抛异常、
 *   <b>超限复用的确实是过期时间最长的分组</b>、<b>复用对象仍可正常读写</b>、
 *   <b>经读/写两个公开入口触达时上限同样生效</b>、<b>达上限后持续新增 expire 无泄露</b>。</li>
 *   <li>未覆盖：{@code estimatedSize()} 在并发下的瞬时偏差（Caffeine 统计值特性，非本守卫的逻辑）。</li>
 * </ul>
 *
 * <p><b>用例前提（软引用值须强引用持有）</b>：{@code CLocalCacheUtils.cacheBuilder()} 配置了
 * {@code softValues()}，namespace 分组缓存与 {@code expire → Cache} 分组缓存中的 <b>Cache 实例本身都是
 * 软引用值</b>——一旦被 JVM 回收，{@code getCache} 会重新创建实例并存入。故凡以 {@code assertSame}
 * 比较两个 Cache 实例身份的用例（1.3、1.4），都把参与比较的实例赋给本类的静态字段
 * （{@link #HELD_CACHES}）持强引用，避免 GC 使 {@code assertSame} 因重建实例而失败（那是环境噪声、
 * 不是被测逻辑的缺陷）；断言对象身份的用例不得只把实例留在局部变量里。</p>
 * <h2>getCache 分组上限</h2>
 * <ul>
 *   <li>1.1 未达上限：不同 expire 各得不同 Cache 实例（testGetCache_belowCap_createsDistinct）</li>
 *   <li>1.2 超过上限：实例数封顶为 MAX_EXPIRE_CACHES_PER_NAMESPACE（testGetCache_overCap_capped）</li>
 *   <li>1.3 已存在相同 expire：返回同一实例（testGetCache_sameExpire_returnsSame）</li>
 *   <li>1.4 超限复用：复用的就是过期时间最长的那个 Cache（testGetCache_overCap_reusesLongestExpire）</li>
 *   <li>1.5 超限复用后仍可读写：复用 Cache 正常承载新 expire 的值（testGetCache_overCap_fallbackUsable）</li>
 *   <li>1.6 读路径公开入口触达：实例数同样封顶（testGetLocalCache_overCap_capped）</li>
 *   <li>1.7 写路径公开入口触达：实例数同样封顶（testUpdateCache_overCap_capped）</li>
 *   <li>1.8 达上限后持续新增 expire 无泄露（testGetCache_overCap_noLeakOnRepeat）</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.1
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
     * 用例 1.4 用 namespace：本类独有，避免与其它用例共享静态缓存
     */
    static class LongestExpireNamespace {
    }

    /**
     * 用例 1.5 用 namespace：本类独有
     */
    static class FallbackUsableNamespace {
    }

    /**
     * 用例 1.6 用 namespace：本类独有
     */
    static class ReadPathNamespace {
    }

    /**
     * 用例 1.7 用 namespace：本类独有
     */
    static class WritePathNamespace {
    }

    /**
     * 用例 1.8 用 namespace：本类独有
     */
    static class NoLeakNamespace {
    }

    /**
     * 用例强引用持有的 Cache 实例（软引用值的兜底，见类级 javadoc「用例前提」）
     *
     * <p>凡把 Cache 实例留在局部变量、再用 {@code assertSame} 比较身份的用例，须把实例存入本集合，
     * 使该实例在用例执行期间始终有强引用、不被 {@code softValues()} 语义下的 GC 回收后由
     * {@code getCache} 重建（否则 {@code assertSame} 会因"值被回收"这一环境噪声偶发失败）。</p>
     */
    private static final List<Object> HELD_CACHES = new ArrayList<>();

    /**
     * 把 Cache 实例存入强引用集合并原样返回（供断言身份前调用）
     */
    private static Object hold(Object cache) {
        HELD_CACHES.add(cache);
        return cache;
    }

    /**
     * 上限常量值，经反射读取（与实现保持单一来源，不在测试里写死数字）
     */
    private static int maxExpireCachesPerNamespace() throws Exception {
        Field field = CCacheAspect.class.getDeclaredField("MAX_EXPIRE_CACHES_PER_NAMESPACE");
        field.setAccessible(true);
        return (Integer)field.get(null);
    }

    /**
     * 构造读写路径共用的 join point：方法指向样例方法、参数为 {@code null}。
     *
     * <p>参数为 null 时 el 取不到 key（{@code resolveCacheKey} 返回 null）→ 读/写路径都会
     * 「跳过缓存读写、但仍先取（创建）expire 分组」——正好把被测点收敛到「分组实例的创建上限」，
     * 不掺入缓存读写副作用。</p>
     */
    private static ProceedingJoinPoint joinPoint(Method method) {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[] { null });
        return point;
    }

    /**
     * 取样例方法上的 {@code @CCacheable}（形参名与注解均依赖编译期保留）
     */
    private static CCacheable cacheable(Method method) {
        return method.getAnnotation(CCacheable.class);
    }

    /**
     * 取样例方法上的 {@code @CCacheUpdate}
     */
    private static CCacheUpdate cacheUpdate(Method method) {
        return method.getAnnotation(CCacheUpdate.class);
    }

    /**
     * 读路径样例方法：{@code key} 走 el，参数为 null 时 key 为 null → 取分组但不写缓存
     */
    @CCacheable(namespace = ReadPathNamespace.class, expire = 1, key = "user.id")
    static String readPathSample(Object user) {
        return null;
    }

    /**
     * 写路径样例方法
     */
    @CCacheUpdate(namespace = WritePathNamespace.class, expire = 1, key = "user.id")
    static String writePathSample(Object user) {
        return null;
    }

    private static Method sampleMethod(Class<?> owner, String name) throws Exception {
        for (Method m : owner.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static Method getCacheMethod() throws Exception {
        Method method = CCacheAspect.class.getDeclaredMethod("getCache", Class.class, int.class);
        method.setAccessible(true);
        return method;
    }

    /**
     * 构造「指定 namespace + 指定 expire」的 {@code @CCacheable} 实例。
     *
     * <p>注解为 RUNTIME 保留，但无法直接 new，故用 {@link Mockito} 以样例方法上的注解为蓝本，
     * 按用例需要的 {@code expire} 覆写其 {@code namespace()} 与 {@code expire()}（其余属性沿用样例）。</p>
     */
    @SuppressWarnings("unchecked")
    private static CCacheable cacheableWithExpire(Class<?> namespace, int expire) throws Exception {
        val template = cacheable(sampleMethod(CCacheAspectExpireCacheCapTests.class, "readPathSample"));
        val stub = Mockito.mock(CCacheable.class);
        Mockito.doReturn(namespace).when(stub).namespace();
        Mockito.doReturn(expire).when(stub).expire();
        Mockito.doReturn(template.key()).when(stub).key();
        Mockito.doReturn(true).when(stub).local();
        Mockito.doReturn(template.idConverter()).when(stub).idConverter();
        return stub;
    }

    /**
     * 构造「指定 namespace + 指定 expire」的 {@code @CCacheUpdate} 实例（同 {@link #cacheableWithExpire}）
     */
    @SuppressWarnings("unchecked")
    private static CCacheUpdate cacheUpdateWithExpire(Class<?> namespace, int expire) throws Exception {
        val template = cacheUpdate(sampleMethod(CCacheAspectExpireCacheCapTests.class, "writePathSample"));
        val stub = Mockito.mock(CCacheUpdate.class);
        Mockito.doReturn(namespace).when(stub).namespace();
        Mockito.doReturn(expire).when(stub).expire();
        Mockito.doReturn(template.key()).when(stub).key();
        Mockito.doReturn(true).when(stub).local();
        Mockito.doReturn(template.idConverter()).when(stub).idConverter();
        return stub;
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

        val first = hold(method.invoke(aspect, SameExpireNamespace.class, 60));
        val second = hold(method.invoke(aspect, SameExpireNamespace.class, 60));

        Assertions.assertSame(first, second);
    }


    /**
     * 对应测试用例 1.4：超限时复用的 Cache 就是该 namespace 下「过期时间最长」的那个分组
     *
     * <p>回归点：守卫的兜底是「复用过期时间最长的 Cache」（{@code longestExpireCache} 取 key 最大者）。
     * 若该比较写反（取最短）或返回 null，1.2 仍会通过——它只断言「数量封顶」，不校验复用对象是谁。
     * 本用例请求 {@code max + 1} 个不同 expire，断言第 {@code max + 1} 次返回的实例，
     * 与先前 expire 最大者返回的实例**同一个**。</p>
     */
    @Test
    void testGetCache_overCap_reusesLongestExpire() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();

        Object longestExpireCache = null;
        for (int i = 1; i <= max; i++) {
            Object cache = hold(method.invoke(aspect, LongestExpireNamespace.class, i));
            if (i == max) {
                longestExpireCache = cache;
            }
        }

        // 新 expire（max+1）应被守卫拦截，复用 expire=max 的那个实例
        val reused = hold(method.invoke(aspect, LongestExpireNamespace.class, max + 1));

        Assertions.assertSame(longestExpireCache, reused,
            "超限时应复用过期时间最长的 Cache，而非新建或复用其它分组");
    }

    /**
     * 对应测试用例 1.5：超限复用后的 Cache 仍可正常读写（复用不是返回个不可用的空壳）
     */
    @Test
    void testGetCache_overCap_fallbackUsable() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();

        for (int i = 1; i <= max; i++) {
            method.invoke(aspect, FallbackUsableNamespace.class, i);
        }

        @SuppressWarnings("unchecked")
        val reused = (Cache<String, Object>)method.invoke(aspect, FallbackUsableNamespace.class, max + 1);
        reused.put("k", "v");

        Assertions.assertEquals("v", reused.getIfPresent("k"));
    }

    /**
     * 对应测试用例 1.6：经「读路径」公开入口 {@link CCacheAspect#getLocalCache} 触达时，上限同样生效
     *
     * <p>1.1–1.5 均直接反射调用 private 的 {@code getCache}；本用例改从真实调用点进入——
     * {@code getLocalCache} 是 {@code @CCacheable} 的读路径，生产上 expire 分组即由它带入。
     * 用例以不同 {@code expire} 反复走读路径，断言底层分组实例数仍封顶，
     * 排除「守卫只在被直调时生效」的可能。</p>
     */
    @Test
    void testGetLocalCache_overCap_capped() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();
        val sample = sampleMethod(CCacheAspectExpireCacheCapTests.class, "readPathSample");
        val point = joinPoint(sample);

        val requested = max * 2 + 1;
        for (int i = 1; i <= requested; i++) {
            aspect.getLocalCache(point, sample, cacheableWithExpire(ReadPathNamespace.class, i));
        }

        Map<Object, Boolean> distinct = new IdentityHashMap<>();
        for (int i = 1; i <= requested; i++) {
            distinct.put(method.invoke(aspect, ReadPathNamespace.class, i), Boolean.TRUE);
        }

        Assertions.assertTrue(distinct.size() <= max,
            "读路径请求 " + requested + " 个不同 expire，底层实例数 " + distinct.size() + " 超过上限 " + max);
    }

    /**
     * 对应测试用例 1.7：经「写路径」公开入口 {@link CCacheAspect#updateCache} 触达时，上限同样生效
     */
    @Test
    void testUpdateCache_overCap_capped() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();
        val sample = sampleMethod(CCacheAspectExpireCacheCapTests.class, "writePathSample");
        val point = joinPoint(sample);

        val requested = max * 2 + 1;
        for (int i = 1; i <= requested; i++) {
            aspect.updateCache(point, sample, cacheUpdateWithExpire(WritePathNamespace.class, i), "value");
        }

        Map<Object, Boolean> distinct = new IdentityHashMap<>();
        for (int i = 1; i <= requested; i++) {
            distinct.put(method.invoke(aspect, WritePathNamespace.class, i), Boolean.TRUE);
        }

        Assertions.assertTrue(distinct.size() <= max,
            "写路径请求 " + requested + " 个不同 expire，底层实例数 " + distinct.size() + " 超过上限 " + max);
    }

    /**
     * 对应测试用例 1.8：达到上限后持续请求新 expire，实例数不再增长（无泄露）
     *
     * <p>与 1.2 互补：1.2 只「超量请求一次」就断言；本用例到达上限后**分多轮**持续请求新 expire，
     * 确认守卫对每一次新请求都持续生效（而非只拦第一次、后续又放行）。</p>
     */
    @Test
    void testGetCache_overCap_noLeakOnRepeat() throws Exception {

        val max = maxExpireCachesPerNamespace();
        val aspect = new CCacheAspect(null);
        val method = getCacheMethod();

        for (int i = 1; i <= max; i++) {
            method.invoke(aspect, NoLeakNamespace.class, i);
        }

        for (int round = 1; round <= 3; round++) {
            for (int i = 1; i <= 10; i++) {
                method.invoke(aspect, NoLeakNamespace.class, max * 10 + round * 100 + i);
            }

            Map<Object, Boolean> distinct = new IdentityHashMap<>();
            for (int i = 1; i <= max + 30; i++) {
                distinct.put(method.invoke(aspect, NoLeakNamespace.class, i), Boolean.TRUE);
            }
            Assertions.assertEquals(max, distinct.size(),
                "第 " + round + " 轮持续新增 expire 后，实例数应仍为上限 " + max);
        }
    }

}
