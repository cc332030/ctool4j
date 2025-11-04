package com.c332030.ctool4j.redis.service.impl;

import com.c332030.ctool4j.core.util.CObjUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CLockService
 * </p>
 *
 * @since 2025/11/3
 */
@CustomLog
@Service
@AllArgsConstructor
public class CLockService {

    RedissonClient redissonClient;

    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    public void lock(String key) {
        lock(getLock(key));
    }
    public void lock(RLock lock) {
        lock.lock();
    }

    public void unlock(String key) {
        unlock(getLock(key));
    }

    public void unlock(RLock lock) {
        lock.unlock();
    }

    public boolean tryLock(String key) {
        return tryLock(getLock(key));
    }

    public boolean tryLock(RLock lock) {
        return lock.tryLock();
    }

    public boolean tryLock(String key, long waitTime, TimeUnit timeUnit) {
        return tryLock(getLock(key), waitTime, timeUnit);
    }

    @SneakyThrows
    public boolean tryLock(RLock lock, long waitTime, TimeUnit timeUnit) {
        return lock.tryLock(waitTime, timeUnit);
    }

    public boolean tryLock(RLock lock, Duration waitDuration) {
        long timeout;
        TimeUnit timeUnit;
        if (TimeoutUtils.hasMillis(waitDuration)) {
            timeout = waitDuration.toMillis();
            timeUnit = TimeUnit.MILLISECONDS;
        } else {
            timeout = waitDuration.getSeconds();
            timeUnit = TimeUnit.SECONDS;
        }
        return tryLock(lock, timeout, timeUnit);
    }

    public boolean tryLockThenRun(String key, Runnable runnable) {
        return null != tryLockThenRun(key, CObjUtils.toSupplier(runnable), null);
    }

    public <T> T tryLockThenRun(String key, Supplier<T> valueSupplier) {
        return tryLockThenRun(key, valueSupplier, null);
    }

    public boolean tryLockThenRun(String key, Runnable runnable, Runnable failureRunnable) {
        return null != tryLockThenRun(key, 0, null, CObjUtils.toSupplier(runnable), failureRunnable);
    }
    public <T> T tryLockThenRun(String key, Supplier<T> valueSupplier, Runnable failureRunnable) {
        return tryLockThenRun(key, 0, null, valueSupplier, failureRunnable);
    }

    public boolean tryLockThenRun(String key, Duration waitDuration, Runnable runnable) {
        return null != tryLockThenRun(key, waitDuration, CObjUtils.toSupplier(runnable), null);
    }
    public <T> T tryLockThenRun(String key, Duration waitDuration, Supplier<T> valueSupplier) {
        return tryLockThenRun(key, waitDuration, valueSupplier, null);
    }

    public boolean tryLockThenRun(String key, Duration waitDuration,
                                  Runnable runnable, Runnable failureRunnable) {
        return null != tryLockThenRun(key, waitDuration, CObjUtils.toSupplier(runnable), failureRunnable);
    }

    public <T> T tryLockThenRun(String key, Duration waitDuration,
                                Supplier<T> valueSupplier, Runnable failureRunnable) {

        long timeout;
        TimeUnit timeUnit;

        if (TimeoutUtils.hasMillis(waitDuration)) {
            timeout = waitDuration.toMillis();
            timeUnit = TimeUnit.MILLISECONDS;
        } else {
            timeout = waitDuration.getSeconds();
            timeUnit = TimeUnit.SECONDS;
        }

        return tryLockThenRun(key, timeout, timeUnit, valueSupplier, failureRunnable);
    }

    public boolean tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                  Runnable runnable) {
        return null != tryLockThenRun(key, waitTime, timeUnit, CObjUtils.toSupplier(runnable), null);
    }
    public <T> T tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                Supplier<T> valueSupplier) {
        return tryLockThenRun(key, waitTime, timeUnit, valueSupplier, null);
    }

    public boolean tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                  Runnable runnable, Runnable failureRunnable) {
        return null != tryLockThenRun(key, waitTime, timeUnit, CObjUtils.toSupplier(runnable), failureRunnable);
    }

    public <T> T tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                Supplier<T> valueSupplier, Runnable failureRunnable) {

        RLock lock = getLock(key);

        boolean locked = Objects.isNull(timeUnit)
                ? tryLock(lock)
                : tryLock(lock, waitTime, timeUnit);
        if(locked) {
            log.info("tryLockThenRun lock success, key: {}", key);
            try {
                return valueSupplier.get();
            } finally {
                unlock(lock);
                log.info("tryLockThenRun unlock success, key: {}", key);
            }
        } else {
            log.info("tryLockThenRun wait timeout, key: {}, waitTime: {}, timeUnit: {}", key, waitTime, timeUnit);
            if(null != failureRunnable) {
                failureRunnable.run();
            }
        }

        return null;
    }

}
