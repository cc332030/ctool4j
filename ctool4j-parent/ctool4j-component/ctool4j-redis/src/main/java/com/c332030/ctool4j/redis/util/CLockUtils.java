package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.interfaces.IOperate;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * <p>
 * Description: LockUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/20
 */
@UtilityClass
@CAutowired
public class CLockUtils {

    public static final String LOCK_STR = "lock";

    @CAutowired
    CLockService lockService;

    /**
     * 获取锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    public static String getLockKey(String key) {
        return key
                + CRedisUtils.KEY_SEPARATOR
                +  LOCK_STR;
    }

    /**
     * 获取锁 key
     * @param clazz 业务类
     * @param iOperate 操作
     * @param key 业务 key
     * @return 锁 key
     */
    public static String getLockKey(Class<?> clazz, IOperate iOperate, Object key) {
        return getLockKey(CRedisUtils.getKey(clazz, iOperate, key));
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 锁成功操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, int waitSeconds, CSupplier<T> valueSupplier) {
        return tryLockThenRun(key, Duration.ofSeconds(waitSeconds), valueSupplier);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param valueSupplier 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return tryLockThenRun(key, null, valueSupplier, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, int waitSeconds, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return tryLockThenRun(key, Duration.ofSeconds(waitSeconds), valueSupplier, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param valueSupplier 锁成功操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, Duration waitDuration, CSupplier<T> valueSupplier) {
        return tryLockThenRun(key, waitDuration, valueSupplier, null);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param valueSupplier 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, Duration waitDuration, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return lockService.tryLockThenRun(key, waitDuration, valueSupplier, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param runnable 锁成功操作
     */
    public void tryLockThenRun(String key, int waitSeconds, CRunnable runnable) {
        tryLockThenRun(key, Duration.ofSeconds(waitSeconds), runnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     */
    public void tryLockThenRun(String key, CRunnable runnable, CRunnable failureRunnable) {
        tryLockThenRun(key, null, runnable, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     */
    public void tryLockThenRun(String key, int waitSeconds, CRunnable runnable, CRunnable failureRunnable) {
        tryLockThenRun(key, Duration.ofSeconds(waitSeconds), runnable, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param runnable 锁成功操作
     */
    public void tryLockThenRun(String key, Duration waitDuration, CRunnable runnable) {
        tryLockThenRun(key, waitDuration, runnable, null);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     */
    public void tryLockThenRun(String key, Duration waitDuration, CRunnable runnable, CRunnable failureRunnable) {
        lockService.tryLockThenRun(key, waitDuration, runnable, failureRunnable);
    }

}
