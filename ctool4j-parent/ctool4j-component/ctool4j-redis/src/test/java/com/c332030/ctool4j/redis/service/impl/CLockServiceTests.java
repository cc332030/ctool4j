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
 * @since 2026/8/16
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
    @Test
    void lock_formattedKey() throws InterruptedException {
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        lockService.lock("sign:report:{}:{}", 1, 2).execute(() -> { });

        Mockito.verify(redissonClient).getLock("sign:report:1:2");
    }

    /**
     * 正常路径：加锁成功后执行业务并解锁
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
    @Test
    void tryLock_duration_null_throws() {
        Assertions.assertThrowsExactly(NullPointerException.class,
            () -> lockService.tryLock(lock, null));
    }
}
