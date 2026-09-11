package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.redis.model.CValueWithTtl;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CCacheBuilderTests
 * </p>
 *
 * <p>
 * 是 {@link CCacheService.CCacheBuilder} 的测试用例。
 * </p>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>用 Mockito mock {@code CLockService}/{@code CStringStringRedisService} 构造 {@code CCacheService} 与 builder，隔离外部依赖。</li>
 *   <li>覆盖链式配置方法的可链性（返回 this）与字段赋值；覆盖 {@code computeIfAbsent} 的永久缓存直接返回与</li>
 *   <li>过期后读-算-写两条核心路径。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 builder 链式配置与 TTL 分流的约定（永久缓存直接返回、已过期阻塞加锁读-算-写）。</li>
 *   <li>依据白盒原则：验证字段默认值、各配置方法链式返回与字段写入、核心 computeIfAbsent 分支。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认值、waitTime/onLockFail/expireDuration/refreshWindow 链式配置、key/tClass 存储、</li>
 *   <li>永久缓存直接返回（不抢锁）、过期后读-算-写。</li>
 *   <li>未覆盖：快到期异步刷新分支（依赖真实 Redis/并发环境，由集成场景验证）；锁竞争失败回调实际执行。</li>
 * </ul>
 * <h2>链式配置</h2>
 * <ul>
 *   <li>1.1 默认值：waitTime=1s、refreshWindow=5m、expireDuration=23h、onLockFail 非空（defaultValues）</li>
 *   <li>1.2 waitTime(long)：链式 + 秒转 Duration（waitTime_long_chainable）</li>
 *   <li>1.3 waitTime(Duration)：链式 + 赋值（waitTime_duration_chainable）</li>
 *   <li>1.4 onLockFail：链式 + 赋值（onLockFail_chainable）</li>
 *   <li>1.5 expireDuration(Duration)：链式 + 赋值（expireDuration_duration_chainable）</li>
 *   <li>1.6 expireDuration(Function)：链式 + 赋值（expireDuration_function_chainable）</li>
 *   <li>1.7 refreshWindow：链式 + 赋值（refreshWindow_chainable）</li>
 *   <li>1.8 key/tClass 存储：构造参数正确保存（keyAndTClass_stored）</li>
 * </ul>
 * <h2>computeIfAbsent 缓存策略</h2>
 * <ul>
 *   <li>2.1 永久缓存（TTL=-1）：直接返回缓存值，不抢锁（computeIfAbsent_permanentCache_returnsDirectly）</li>
 *   <li>2.2 已过期：阻塞加锁读-算-写，写缓存并返回值（computeIfAbsent_expired_computesAndWrites）</li>
 * </ul>
 * <h2>getCache 加锁双重检查</h2>
 * <ul>
 *   <li>3.1 未命中：锁内计算并写缓存（getCache_lockDoubleCheck_computesAndWrites）</li>
 *   <li>3.2 锁内已被写入：直接返回缓存值，不再写（getCache_lockDoubleCheck_hitInLock）</li>
 *   <li>3.3 计算值 null：不写缓存、直接返回 null（getCache_nullValue_notCached）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 1.0
 * @version 1.0
 */
public class CCacheBuilderTests {

    private CLockService lockService;
    private CStringStringRedisService redisService;
    private CCacheService cacheService;
    private CCacheService.CCacheBuilder<String> builder;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        lockService = Mockito.mock(CLockService.class);
        redisService = Mockito.mock(CStringStringRedisService.class);

        cacheService = new CCacheService(lockService, redisService);
        builder = cacheService.cacheBuilder("myKey", String.class);
    }

    /**
     * 对应测试用例 1.1：链式配置默认值
     */
    @Test
    public void defaultValues() {
        Assertions.assertEquals(Duration.ofSeconds(1), getFieldValue(builder, "waitTime"));
        Assertions.assertEquals(Duration.ofMinutes(5), getFieldValue(builder, "refreshWindow"));
        Assertions.assertEquals(Duration.ofHours(23), getFieldValue(builder, "expireDuration"));
        Assertions.assertNotNull(getFieldValue(builder, "onLockFail"));
    }

    /**
     * 对应测试用例 1.2：waitTime(long) 链式
     */
    @Test
    public void waitTime_long_chainable() {
        Assertions.assertSame(builder, builder.waitTime(10L));
        Assertions.assertEquals(Duration.ofSeconds(10), getFieldValue(builder, "waitTime"));
    }

    /**
     * 对应测试用例 1.3：waitTime(Duration) 链式
     */
    @Test
    public void waitTime_duration_chainable() {
        Assertions.assertSame(builder, builder.waitTime(Duration.ofSeconds(30)));
        Assertions.assertEquals(Duration.ofSeconds(30), getFieldValue(builder, "waitTime"));
    }

    /**
     * 对应测试用例 1.4：onLockFail 链式
     */
    @Test
    public void onLockFail_chainable() {
        Assertions.assertSame(builder, builder.onLockFail(lock -> {}));
        Assertions.assertNotNull(getFieldValue(builder, "onLockFail"));
    }

    /**
     * 对应测试用例 1.5：expireDuration(Duration) 链式
     */
    @Test
    public void expireDuration_duration_chainable() {
        Assertions.assertSame(builder, builder.expireDuration(Duration.ofMinutes(1)));
        Assertions.assertEquals(Duration.ofMinutes(1), getFieldValue(builder, "expireDuration"));
    }

    /**
     * 对应测试用例 1.6：expireDuration(Function) 链式
     */
    @Test
    public void expireDuration_function_chainable() {
        Assertions.assertSame(builder, builder.expireDuration(value -> Duration.ofMinutes(1)));
        Assertions.assertNotNull(getFieldValue(builder, "expireDurationFunction"));
    }

    /**
     * 对应测试用例 1.7：refreshWindow 链式
     */
    @Test
    public void refreshWindow_chainable() {
        Assertions.assertSame(builder, builder.refreshWindow(Duration.ofMinutes(1)));
        Assertions.assertEquals(Duration.ofMinutes(1), getFieldValue(builder, "refreshWindow"));
    }

    /**
     * 对应测试用例 1.8：key/tClass 存储
     */
    @Test
    public void keyAndTClass_stored() throws Exception {
        Assertions.assertEquals("myKey", getFieldValue(builder, "key"));
        Assertions.assertEquals(String.class, getFieldValue(builder, "tClass"));
    }

    /**
     * 对应测试用例 2.1：永久缓存直接返回不抢锁
     */
    @Test
    public void computeIfAbsent_permanentCache_returnsDirectly() {
        Mockito.when(redisService.getValueWithTtl("myKey", String.class))
            .thenReturn(CValueWithTtl.<String>builder()
                .value("cached")
                .ttl(-1L)
                .build());

        String result = builder.computeIfAbsent(() -> "computed");

        Assertions.assertEquals("cached", result);
        Mockito.verify(lockService, Mockito.never()).lock(Mockito.anyString());
    }

    /**
     * 对应测试用例 2.2：已过期读-算-写
     */
    @Test
    public void computeIfAbsent_expired_computesAndWrites() {
        Mockito.when(redisService.getValueWithTtl("myKey", String.class)).thenReturn(null);
        Mockito.when(redisService.getValue("myKey", String.class)).thenReturn(null);

        CLockService.CLockBuilder lockBuilder = Mockito.mock(CLockService.CLockBuilder.class);
        Mockito.when(lockService.lock(Mockito.anyString())).thenReturn(lockBuilder);
        Mockito.when(lockBuilder.waitTime(Mockito.any(Duration.class))).thenReturn(lockBuilder);
        Mockito.when(lockBuilder.onLockFail(Mockito.any())).thenReturn(lockBuilder);
        Mockito.when(lockBuilder.execute(Mockito.any(Supplier.class)))
            .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

        String result = builder.computeIfAbsent(() -> "computed");

        Assertions.assertEquals("computed", result);
        // 第二参数用 Object 匹配类方法 setValue(String, Object, Duration)；
        // 用 String 字面量会解析到接口默认方法 setValue(String, String, Duration)（computeAndWrite 中泛型 T 实际走类方法）
        Mockito.verify(redisService).setValue(
            Mockito.eq("myKey"), Mockito.any(Object.class), Mockito.any(Duration.class));
    }

    /**
     * 打桩 lockBuilder：等待超时/锁失败回调链式返回自身，execute 直接执行 callable（模拟加锁成功）
     */
    private CLockService.CLockBuilder stubLockBuilder() {
        CLockService.CLockBuilder lockBuilder = Mockito.mock(CLockService.CLockBuilder.class);
        Mockito.when(lockService.lock(Mockito.anyString())).thenReturn(lockBuilder);
        Mockito.when(lockBuilder.waitTime(Mockito.any(Duration.class))).thenReturn(lockBuilder);
        Mockito.when(lockBuilder.onLockFail(Mockito.any())).thenReturn(lockBuilder);
        Mockito.when(lockBuilder.execute(Mockito.any(Supplier.class)))
            .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
        return lockBuilder;
    }

    /**
     * 对应测试用例 3.1：getCache 加锁双重检查，未命中时锁内计算并写缓存（Q3 修复）
     */
    @Test
    public void getCache_lockDoubleCheck_computesAndWrites() {
        stubLockBuilder();
        // 首读 miss，锁内重读 miss
        Mockito.when(redisService.getValue("k", String.class)).thenReturn(null);

        String result = cacheService.getCache("k", String.class, 60, () -> "computed");

        Assertions.assertEquals("computed", result);
        // 写缓存
        Mockito.verify(redisService).setValue(
            Mockito.eq("k"), Mockito.any(Object.class), Mockito.any(Duration.class));
    }

    /**
     * 对应测试用例 3.2：getCache 加锁双重检查，锁内已被其他线程写入时直接返回缓存值（防击穿）
     */
    @Test
    public void getCache_lockDoubleCheck_hitInLock() {
        stubLockBuilder();
        // 首读 miss，锁内重读命中
        Mockito.when(redisService.getValue("k", String.class))
            .thenReturn(null)
            .thenReturn("cachedInLock");

        String result = cacheService.getCache("k", String.class, 60, () -> "computed");

        Assertions.assertEquals("cachedInLock", result);
        // 未再写缓存
        Mockito.verify(redisService, Mockito.never()).setValue(
            Mockito.anyString(), Mockito.any(Object.class), Mockito.any(Duration.class));
    }

    /**
     * 对应测试用例 3.3：getCache 计算值 null 时不写缓存、直接返回 null（保留语义）
     */
    @Test
    public void getCache_nullValue_notCached() {
        stubLockBuilder();
        Mockito.when(redisService.getValue("k", String.class)).thenReturn(null);

        String result = cacheService.getCache("k", String.class, 60, () -> null);

        Assertions.assertNull(result);
        Mockito.verify(redisService, Mockito.never()).setValue(
            Mockito.anyString(), Mockito.any(Object.class), Mockito.any(Duration.class));
    }

    private Object getFieldValue(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

}
