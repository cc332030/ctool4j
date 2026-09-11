package com.c332030.ctool4j.redis.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CLockServiceTests
 * </p>
 *
 * <p>
 * 通过 Mockito 模拟 RedissonClient / RLock，验证加锁-执行-解锁模板的
 * 正常路径（加锁成功执行并解锁）与异常路径（加锁失败走 onLockFail、不执行业务）。
 * </p>
 *
 * <p>
 * 是 {@link CLockService} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过 mock RedissonClient/RLock 隔离外部 Redis，覆盖 lock 格式化 key、execute（成功/失败）、</li>
 *   <li>tryLock（毫秒/秒/空 waitDuration）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 lock 构建器、execute 加锁-执行-解锁、tryLock 语义的约定。</li>
 *   <li>依据白盒/黑盒原则：execute 锁成功/失败、tryLock 各时间单位、空 waitDuration 异常路径均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：lock 格式化 key、execute 锁成功执行并解锁、锁失败触发 onLockFail、execute 有返回值、</li>
 *   <li>锁失败返回 null、tryLock 毫秒/秒/空 waitDuration 抛异常。</li>
 *   <li>未覆盖：真实 Redisson 分布式锁在 Redis 上的竞争行为（依赖环境）；unlockDelay 延迟释放细节。</li>
 * </ul>
 * <h2>lock 构建器</h2>
 * <ul>
 *   <li>1.1 格式化 key（lock_formattedKey）</li>
 * </ul>
 * <h2>execute 加锁-执行-解锁</h2>
 * <ul>
 *   <li>2.1 锁成功：执行业务并解锁（execute_lockSuccess_runsAndUnlocks）</li>
 *   <li>2.2 锁失败：触发 onLockFail 回调（execute_lockFail_invokesOnLockFail）</li>
 *   <li>2.3 有返回值：返回业务结果（execute_supplier_returnsValue）</li>
 *   <li>2.4 锁失败有返回值：返回 null（execute_supplier_lockFail_returnsNull）</li>
 * </ul>
 * <h2>tryLock</h2>
 * <ul>
 *   <li>3.1 毫秒级 waitTime（tryLock_duration_millis）</li>
 *   <li>3.2 秒级 waitTime（tryLock_duration_seconds）</li>
 *   <li>3.3 空 waitDuration 抛异常（tryLock_duration_null_throws）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/8/16
 * @version 1.0
 */
class CLockServiceTests {

    private RedissonClient redissonClient;
    private RLock lock;
    private CLockService lockService;

    @BeforeEach
    void setUp() {
        redissonClient = Mockito.mock(RedissonClient.class);
        lock = Mockito.mock(RLock.class);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        lockService = new CLockService(redissonClient);
    }

    /**
     * 正常路径：lock(format, args) 按 StrUtil.format 生成锁 key，执行时按该 key 加锁
     */
    /**
     * 对应测试用例 1.1：格式化 key
     */
    @Test
    void lock_formattedKey() throws InterruptedException {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        lockService.lock("sign:report:{}:{}", 1, 2).execute(() -> { });

        Mockito.verify(redissonClient).getLock("sign:report:1:2");
    }

    /**
     * 正常路径：加锁成功后执行业务并解锁
     */
    /**
     * 对应测试用例 2.1：锁成功：执行业务并解锁
     */
    @Test
    void execute_lockSuccess_runsAndUnlocks() throws InterruptedException {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        AtomicBoolean executed = new AtomicBoolean(false);
        lockService.lock("key").execute(() -> executed.set(true));

        Assertions.assertTrue(executed.get());
        Mockito.verify(lock).unlock();
    }

    /**
     * 异常路径：加锁失败时不执行业务，且执行 onLockFail 回调
     */
    /**
     * 对应测试用例 2.2：锁失败：触发 onLockFail 回调
     */
    @Test
    void execute_lockFail_invokesOnLockFail() throws InterruptedException {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        AtomicBoolean executed = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        lockService.lock("key")
            .onLockFail(l -> failed.set(true))
            .execute(() -> executed.set(true));

        Assertions.assertFalse(executed.get());
        Assertions.assertTrue(failed.get());
        Mockito.verify(lock, Mockito.never()).unlock();
    }

    /**
     * 正常路径：execute(Supplier) 加锁成功后返回业务返回值并解锁
     */
    /**
     * 对应测试用例 2.3：有返回值：返回业务结果
     */
    @Test
    void execute_supplier_returnsValue() throws InterruptedException {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);

        String result = lockService.lock("key").execute(() -> "done");

        Assertions.assertEquals("done", result);
        Mockito.verify(lock).unlock();
    }

    /**
     * 异常路径：execute(Supplier) 加锁失败返回 null，不执行业务
     */
    /**
     * 对应测试用例 2.4：锁失败有返回值：返回 null
     */
    @Test
    void execute_supplier_lockFail_returnsNull() throws InterruptedException {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        AtomicReference<String> result = new AtomicReference<>();
        String value = lockService.lock("key").execute(() -> {
            result.set("executed");
            return "x";
        });

        Assertions.assertNull(value);
        Assertions.assertNull(result.get());
    }

    /**
     * 正常路径：tryLock(RLock, Duration) 带毫秒精度的时长转为毫秒加锁
     */
    /**
     * 对应测试用例 3.1：毫秒级 waitTime
     */
    @Test
    void tryLock_duration_millis() throws InterruptedException {
        Duration wait = Duration.ofMillis(1500).plusNanos(1);
        Mockito.when(lock.tryLock(1500L, TimeUnit.MILLISECONDS)).thenReturn(true);

        boolean acquired = lockService.tryLock(lock, wait);

        Assertions.assertTrue(acquired);
        Mockito.verify(lock).tryLock(1500L, TimeUnit.MILLISECONDS);
    }

    /**
     * 正常路径：tryLock(RLock, Duration) 秒级时长转为秒加锁
     */
    /**
     * 对应测试用例 3.2：秒级 waitTime
     */
    @Test
    void tryLock_duration_seconds() throws InterruptedException {
        Mockito.when(lock.tryLock(3L, TimeUnit.SECONDS)).thenReturn(true);

        boolean acquired = lockService.tryLock(lock, Duration.ofSeconds(3));

        Assertions.assertTrue(acquired);
        Mockito.verify(lock).tryLock(3L, TimeUnit.SECONDS);
    }

    /**
     * 异常路径：tryLock(RLock, Duration) 传入 null 抛 NullPointerException
     */
    /**
     * 对应测试用例 3.3：空 waitDuration 抛异常
     */
    @Test
    void tryLock_duration_null_throws() {
        Assertions.assertThrowsExactly(NullPointerException.class,
            () -> lockService.tryLock(lock, null));
    }
}
