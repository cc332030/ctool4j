package com.c332030.ctool4j.redis.idempotent;

import com.c332030.ctool4j.redis.service.impl.CLockService;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CIdempotentAspectTests
 * </p>
 * <p>
 * 测试 {@link CIdempotentAspect}：基于 mock 的 RedissonClient / RLock 验证幂等加锁成功执行业务、
 * 加锁失败抛幂等异常、业务 id 表达式解析、方法名开关、参数校验等。不依赖真实 Redis。
 * </p>
 * <p>
 * 是 {@link CIdempotentAspect} 的测试用例（对应测试文档
 * <code>doc/design/redis/CIdempotentAspectTests.adoc</code>）。
 * </p>
 *
 * @since 2026/9/9
 */
class CIdempotentAspectTests {

    private CLockService lockService;
    private CIdempotentAspect aspect;
    private RedissonClient redissonClient;
    private RLock lock;

    @BeforeEach
    void setUp() {
        redissonClient = Mockito.mock(RedissonClient.class);
        lock = Mockito.mock(RLock.class);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        lockService = new CLockService(redissonClient);
        aspect = new CIdempotentAspect(lockService);

        CSpringApplicationConfig config = new CSpringApplicationConfig();
        config.setGroup("grp");
        config.setName("name");
        CRedisUtils.setSpringApplicationConfig(config);
    }

    @AfterEach
    void tearDown() {
        CRedisUtils.setSpringApplicationConfig(null);
    }

    // ===== 供反射取带 @CIdempotent 的方法 =====

    @CIdempotent(group = CIdempotentAspectTests.class)
    static String doOnce(Long userId) {
        return "ok";
    }

    @CIdempotent(group = CIdempotentAspectTests.class, id = "userId")
    static String doOnceById(Long userId) {
        return "ok";
    }

    @CIdempotent(group = CIdempotentAspectTests.class, id = "  ")
    static String doOnceByBlankId(Long userId) {
        return "ok";
    }

    @CIdempotent(group = CIdempotentAspectTests.class, useMethodName = false)
    static String doOnceWithoutMethodName(Long userId) {
        return "ok";
    }

    @CIdempotent(group = CIdempotentAspectTests.class, useMethodName = false, id = "userId")
    static String doOnceWithoutMethodNameById(Long userId) {
        return "ok";
    }

    @CIdempotent(group = CIdempotentAspectTests.class, message = "custom msg")
    static String doOnceCustomMessage(Long userId) {
        return "ok";
    }

    private Method method(String name) {
        for (Method m : CIdempotentAspectTests.class.getDeclaredMethods()) {
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

    private String capturedKey() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(redissonClient).getLock(captor.capture());
        return captor.getValue();
    }

    /** 对应测试用例 1.1：加锁成功时执行业务并返回结果，且释放锁 */
    @Test
    void idempotent_lockSuccess_executesAndReturns() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnce");
        Assertions.assertEquals("ok", aspect.idempotent(joinPoint(m, new Object[] { 1L })));
        Mockito.verify(lock).unlock();
    }

    /** 对应测试用例 1.2：加锁失败（重复请求）抛幂等异常 */
    @Test
    void idempotent_lockFail_throws() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        Method m = method("doOnce");
        Assertions.assertThrows(CIdempotentException.class,
            () -> aspect.idempotent(joinPoint(m, new Object[] { 1L })));
    }

    /** 对应测试用例 1.3：幂等异常消息取注解 message 默认值 */
    @Test
    void idempotent_message_default() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        Method m = method("doOnce");
        CIdempotentException ex = Assertions.assertThrows(CIdempotentException.class,
            () -> aspect.idempotent(joinPoint(m, new Object[] { 1L })));
        Assertions.assertEquals("重复请求，请勿重复提交", ex.getMessage());
    }

    /** 对应测试用例 1.4：幂等异常消息取注解 message 自定义值 */
    @Test
    void idempotent_message_custom() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        Method m = method("doOnceCustomMessage");
        CIdempotentException ex = Assertions.assertThrows(CIdempotentException.class,
            () -> aspect.idempotent(joinPoint(m, new Object[] { 1L })));
        Assertions.assertEquals("custom msg", ex.getMessage());
    }

    /** 对应测试用例 1.5：无业务 id 时 key 为 应用前缀:分组类简单名:方法名 */
    @Test
    void idempotent_noBizId_key() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnce");
        aspect.idempotent(joinPoint(m, new Object[] { 10L }));
        Assertions.assertEquals("grp:CIdempotentAspectTests:doOnce", capturedKey());
    }

    /** 对应测试用例 1.6：业务 id 表达式取参数，key 含业务维度 */
    @Test
    void idempotent_bizId_key() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnceById");
        aspect.idempotent(joinPoint(m, new Object[] { 10L }));
        Assertions.assertEquals("grp:CIdempotentAspectTests:doOnceById:10", capturedKey());
    }

    /** 对应测试用例 1.7：id 为空白字符串时 key 不含业务维度 */
    @Test
    void idempotent_blankBizId_key() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnceByBlankId");
        aspect.idempotent(joinPoint(m, new Object[] { 10L }));
        Assertions.assertEquals("grp:CIdempotentAspectTests:doOnceByBlankId", capturedKey());
    }

    /** 对应测试用例 1.8：useMethodName=false 时 key 不含方法名段 */
    @Test
    void idempotent_withoutMethodName_key() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnceWithoutMethodName");
        aspect.idempotent(joinPoint(m, new Object[] { 10L }));
        Assertions.assertEquals("grp:CIdempotentAspectTests", capturedKey());
    }

    /** 对应测试用例 1.9：useMethodName=false 且带业务 id 时，key 不含方法名段、含业务 id */
    @Test
    void idempotent_withoutMethodName_bizId_key() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnceWithoutMethodNameById");
        aspect.idempotent(joinPoint(m, new Object[] { 10L }));
        Assertions.assertEquals("grp:CIdempotentAspectTests:10", capturedKey());
    }

    /** 对应测试用例 1.10：锁内业务异常向上传播且释放锁 */
    @Test
    void idempotent_bizException_propagatesAndUnlocks() throws Throwable {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        Method m = method("doOnce");
        ProceedingJoinPoint pjp = joinPoint(m, new Object[] { 1L });
        Mockito.when(pjp.proceed(Mockito.any())).thenThrow(new IllegalStateException("biz error"));

        Assertions.assertThrows(IllegalStateException.class,
            () -> aspect.idempotent(pjp));
        Mockito.verify(lock).unlock();
    }

}
