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
 * 是 {@link CCacheService.CCacheBuilder} 的测试用例（对应测试文档
 * <code>doc/design/cache/CCacheBuilderTests.adoc</code>）。
 * </p>
 */
public class CCacheBuilderTests {

    private CLockService lockService;
    private CStringStringRedisService redisService;
    private CCacheService.CCacheBuilder<String> builder;

    @BeforeEach
    public void setUp() {
        lockService = Mockito.mock(CLockService.class);
        redisService = Mockito.mock(CStringStringRedisService.class);

        CCacheService cacheService = new CCacheService(lockService, redisService);
        builder = cacheService.cacheBuilder("myKey", String.class);
    }

    /** 对应测试用例 1.1：链式配置默认值 */
    @Test
    public void defaultValues() {
        Assertions.assertEquals(Duration.ofSeconds(1), getFieldValue(builder, "waitTime"));
        Assertions.assertEquals(Duration.ofMinutes(5), getFieldValue(builder, "refreshWindow"));
        Assertions.assertEquals(Duration.ofHours(23), getFieldValue(builder, "expireDuration"));
        Assertions.assertNotNull(getFieldValue(builder, "onLockFail"));
    }

    /** 对应测试用例 1.2：waitTime(long) 链式 */
    @Test
    public void waitTime_long_chainable() {
        Assertions.assertSame(builder, builder.waitTime(10L));
        Assertions.assertEquals(Duration.ofSeconds(10), getFieldValue(builder, "waitTime"));
    }

    /** 对应测试用例 1.3：waitTime(Duration) 链式 */
    @Test
    public void waitTime_duration_chainable() {
        Assertions.assertSame(builder, builder.waitTime(Duration.ofSeconds(30)));
        Assertions.assertEquals(Duration.ofSeconds(30), getFieldValue(builder, "waitTime"));
    }

    /** 对应测试用例 1.4：onLockFail 链式 */
    @Test
    public void onLockFail_chainable() {
        Assertions.assertSame(builder, builder.onLockFail(lock -> {}));
        Assertions.assertNotNull(getFieldValue(builder, "onLockFail"));
    }

    /** 对应测试用例 1.5：expireDuration(Duration) 链式 */
    @Test
    public void expireDuration_duration_chainable() {
        Assertions.assertSame(builder, builder.expireDuration(Duration.ofMinutes(1)));
        Assertions.assertEquals(Duration.ofMinutes(1), getFieldValue(builder, "expireDuration"));
    }

    /** 对应测试用例 1.6：expireDuration(Function) 链式 */
    @Test
    public void expireDuration_function_chainable() {
        Assertions.assertSame(builder, builder.expireDuration(value -> Duration.ofMinutes(1)));
        Assertions.assertNotNull(getFieldValue(builder, "expireDurationFunction"));
    }

    /** 对应测试用例 1.7：refreshWindow 链式 */
    @Test
    public void refreshWindow_chainable() {
        Assertions.assertSame(builder, builder.refreshWindow(Duration.ofMinutes(1)));
        Assertions.assertEquals(Duration.ofMinutes(1), getFieldValue(builder, "refreshWindow"));
    }

    /** 对应测试用例 1.8：key/tClass 存储 */
    @Test
    public void keyAndTClass_stored() throws Exception {
        Assertions.assertEquals("myKey", getFieldValue(builder, "key"));
        Assertions.assertEquals(String.class, getFieldValue(builder, "tClass"));
    }

    /** 对应测试用例 2.1：永久缓存直接返回不抢锁 */
    @Test
    public void computeIfAbsent_permanentCache_returnsDirectly() {
        Mockito.when(redisService.getValueWithTtl("myKey", String.class))
            .thenReturn(new CValueWithTtl<>("cached", -1L));

        String result = builder.computeIfAbsent(() -> "computed");

        Assertions.assertEquals("cached", result);
        Mockito.verify(lockService, Mockito.never()).lock(Mockito.anyString());
    }

    /** 对应测试用例 2.2：已过期读-算-写 */
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
