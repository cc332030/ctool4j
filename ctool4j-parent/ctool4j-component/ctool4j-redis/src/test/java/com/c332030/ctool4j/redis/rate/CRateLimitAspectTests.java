package com.c332030.ctool4j.redis.rate;

import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Method;
import java.util.List;

/**
 * <p>
 * Description: CRateLimitAspectTests
 * </p>
 * <p>
 * 测试 {@link CRateLimitAspect}：基于 mock 的 Redis 计数验证限流放行、超阈值拦截、
 * 业务 id 表达式解析、参数校验等。不依赖真实 Redis。
 * </p>
 * <p>
 * 是 {@link CRateLimitAspect} 的测试用例（对应测试文档
 * <code>doc/design/redis/CRateLimitAspectTests.adoc</code>）。
 * </p>
 *
 * @since 2026/9/8
 */
class CRateLimitAspectTests {

    private final CRateLimitAspect aspect = new CRateLimitAspect();

    private CStringStringRedisService redisService;
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisService = Mockito.mock(CStringStringRedisService.class);
        redisTemplate = Mockito.mock(RedisTemplate.class);
        Mockito.when(redisService.getRedisTemplate()).thenReturn(redisTemplate);
        CRedisUtils.setStringStringRedisService(redisService);

        CSpringApplicationConfig config = new CSpringApplicationConfig();
        config.setGroup("grp");
        config.setName("name");
        CRedisUtils.setSpringApplicationConfig(config);
    }

    @AfterEach
    void tearDown() {
        CRedisUtils.setStringStringRedisService(null);
        CRedisUtils.setSpringApplicationConfig(null);
    }

    // ===== 供反射取带 @CRateLimit 的方法 =====

    @CRateLimit(count = 3, interval = 60)
    static String limited(Long userId) {
        return "ok";
    }

    @CRateLimit(count = 3, interval = 60, id = "userId")
    static String limitedById(Long userId) {
        return "ok";
    }

    @CRateLimit(count = 3, interval = 60, id = "  ")
    static String limitedByBlankId(Long userId) {
        return "ok";
    }

    @CRateLimit(count = 3, interval = 60, useMethodName = false)
    static String limitedWithoutMethodName(Long userId) {
        return "ok";
    }

    @CRateLimit(count = 3, interval = 60, useMethodName = false, id = "userId")
    static String limitedWithoutMethodNameById(Long userId) {
        return "ok";
    }

    @CRateLimit(count = 0, interval = 60)
    static String invalidCount(Long userId) {
        return "ok";
    }

    @CRateLimit(count = 3, interval = 0)
    static String invalidInterval(Long userId) {
        return "ok";
    }

    private Method method(String name) {
        for (Method m : CRateLimitAspectTests.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalStateException("method not found: " + name);
    }

    private ProceedingJoinPoint joinPoint(Method method, Object[] args) throws Throwable {
        ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(pjp.getSignature()).thenReturn(signature);
        Mockito.when(pjp.getArgs()).thenReturn(args);
        Mockito.when(pjp.proceed(Mockito.any())).thenReturn("ok");
        return pjp;
    }

    private void stubCurrentCounts(long... counts) {
        for (long c : counts) {
            Mockito.when(redisTemplate.execute(
                Mockito.any(RedisScript.class),
                Mockito.anyList(),
                Mockito.any(),
                Mockito.any()
            )).thenReturn(c);
        }
    }

    /**
     * 对应测试用例 1.1：窗口内未超阈值放行
     */
    @Test
    void rateLimit_withinThreshold_allowed() throws Throwable {
        stubCurrentCounts(1L);
        Method m = method("limited");
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 1L })));
    }

    /**
     * 对应测试用例 1.2：达到阈值仍放行（current == count 允许）
     */
    @Test
    void rateLimit_atThreshold_allowed() throws Throwable {
        stubCurrentCounts(3L);
        Method m = method("limited");
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 1L })));
    }

    /**
     * 对应测试用例 1.3：超过阈值拦截抛限流异常
     */
    @Test
    void rateLimit_overThreshold_blocked() {
        stubCurrentCounts(4L);
        Method m = method("limited");
        Assertions.assertThrows(CRateLimitException.class,
            () -> aspect.rateLimit(joinPoint(m, new Object[] { 1L })));
    }

    /**
     * 对应测试用例 1.4：计数异常抛出的消息取自注解 message（默认）
     */
    @Test
    void rateLimit_message_default() {
        stubCurrentCounts(4L);
        Method m = method("limited");
        CRateLimitException ex = Assertions.assertThrows(CRateLimitException.class,
            () -> aspect.rateLimit(joinPoint(m, new Object[] { 1L })));
        Assertions.assertEquals("请求过于频繁，请稍后再试", ex.getMessage());
    }

    /**
     * 对应测试用例 1.5：业务 id 表达式取指定参数，限流 key 含业务维度
     */
    @Test
    void rateLimit_bizId_inKey() throws Throwable {
        Method m = method("limitedById");
        stubCurrentCounts(1L);
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 10L })));

        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(redisTemplate).execute(
            Mockito.any(RedisScript.class),
            keysCaptor.capture(),
            Mockito.any(),
            Mockito.any()
        );
        Assertions.assertEquals("grp:CRateLimitAspectTests:limitedById:10", keysCaptor.getValue().get(0));
    }

    /**
     * 对应测试用例 1.6：id 为空（未配置）时按方法全局限流，key 不含业务维度
     */
    @Test
    void rateLimit_noBizId_limitGlobalKey() throws Throwable {
        Method m = method("limited");
        stubCurrentCounts(1L);
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 10L })));

        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(redisTemplate).execute(
            Mockito.any(RedisScript.class),
            keysCaptor.capture(),
            Mockito.any(),
            Mockito.any()
        );
        Assertions.assertEquals("grp:CRateLimitAspectTests:limited", keysCaptor.getValue().get(0));
    }

    /**
     * 对应测试用例 1.9：id 为空白字符串时按方法全局限流，key 不含业务维度
     */
    @Test
    void rateLimit_blankBizId_limitGlobalKey() throws Throwable {
        Method m = method("limitedByBlankId");
        stubCurrentCounts(1L);
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 10L })));

        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(redisTemplate).execute(
            Mockito.any(RedisScript.class),
            keysCaptor.capture(),
            Mockito.any(),
            Mockito.any()
        );
        Assertions.assertEquals("grp:CRateLimitAspectTests:limitedByBlankId", keysCaptor.getValue().get(0));
    }

    /**
     * 对应测试用例 1.10：useMethodName=false 时 key 不含方法名段（共享限流桶）
     */
    @Test
    void rateLimit_withoutMethodName_shareKey() throws Throwable {
        Method m = method("limitedWithoutMethodName");
        stubCurrentCounts(1L);
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 10L })));

        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(redisTemplate).execute(
            Mockito.any(RedisScript.class),
            keysCaptor.capture(),
            Mockito.any(),
            Mockito.any()
        );
        Assertions.assertEquals("grp:CRateLimitAspectTests", keysCaptor.getValue().get(0));
    }

    /**
     * 对应测试用例 1.11：useMethodName=false 且带业务 id 时，key 不含方法名段、含业务 id
     */
    @Test
    void rateLimit_withoutMethodName_bizIdInKey() throws Throwable {
        Method m = method("limitedWithoutMethodNameById");
        stubCurrentCounts(1L);
        Assertions.assertEquals("ok", aspect.rateLimit(joinPoint(m, new Object[] { 10L })));

        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(redisTemplate).execute(
            Mockito.any(RedisScript.class),
            keysCaptor.capture(),
            Mockito.any(),
            Mockito.any()
        );
        Assertions.assertEquals("grp:CRateLimitAspectTests:10", keysCaptor.getValue().get(0));
    }

    /**
     * 对应测试用例 1.7：count 非法（<=0）抛 IllegalArgumentException
     */
    @Test
    void rateLimit_invalidCount_throws() {
        Method m = method("invalidCount");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> aspect.rateLimit(joinPoint(m, new Object[] { 1L })));
    }

    /**
     * 对应测试用例 1.8：interval 非法（<=0）抛 IllegalArgumentException
     */
    @Test
    void rateLimit_invalidInterval_throws() {
        Method m = method("invalidInterval");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> aspect.rateLimit(joinPoint(m, new Object[] { 1L })));
    }

}
