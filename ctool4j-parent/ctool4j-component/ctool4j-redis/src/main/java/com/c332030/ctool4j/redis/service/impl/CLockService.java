package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.function.CSupplier;
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

    /**
     * 创建锁构建器
     *
     * @param lockKey 锁 key
     */
    public CLockBuilder lock(String lockKey) {
        return new CLockBuilder(lockKey);
    }

    /**
     * 创建锁构建器，支持 StrUtil.format 格式化 lockKey
     *
     * @param format 格式串，如 "sign:report:{}:{}"
     * @param args   格式化参数
     */
    public CLockBuilder lock(String format, Object... args) {
        return lock(StrUtil.format(format, args));
    }

    /**
     * 锁构建器：加锁-执行-解锁统一模板
     * <p>默认解锁时立即释放锁；可通过 unlockDelay 配置释放锁前延迟，
     * 用于写缓存后延迟释放锁，避免其他线程在刷新生效的瞬间窗口内读到旧数据</p>
     */
    public class CLockBuilder {

        final String lockKey;

        /**
         * 等待获取锁的超时时间，默认 0 不等待
         */
        Duration waitTime = Duration.ZERO;

        long leaseTime = -1;
        TimeUnit leaseTimeUnit = TimeUnit.SECONDS;

        /**
         * 释放锁前延迟时间，默认不延迟（立即释放）
         */
        Duration unlockDelay = Duration.ZERO;

        CConsumer<RLock> onLockFail = lock -> {};

        CLockBuilder(String lockKey) {
            this.lockKey = lockKey;
        }

        /**
         * 等待获取锁的超时时间（秒），默认 0 不等待
         */
        public CLockBuilder waitTime(long waitTime) {
            return waitTime(Duration.ofSeconds(waitTime));
        }

        /**
         * 等待获取锁的超时时间
         */
        public CLockBuilder waitTime(Duration waitDuration) {
            this.waitTime = waitDuration;
            return this;
        }

        /**
         * 持锁时间（秒），默认 -1 启用 watchdog 自动续期
         */
        public CLockBuilder leaseTime(long leaseTime) {
            return leaseTime(leaseTime, TimeUnit.SECONDS);
        }

        /**
         * 持锁时间
         */
        public CLockBuilder leaseTime(long leaseTime, TimeUnit timeUnit) {
            this.leaseTime = leaseTime;
            this.leaseTimeUnit = timeUnit;
            return this;
        }

        /**
         * 获取锁失败时的回调
         */
        public CLockBuilder onLockFail(CConsumer<RLock> onLockFail) {
            this.onLockFail = onLockFail;
            return this;
        }

        /**
         * 释放锁前延迟时间，默认不延迟
         * <p>用于写缓存后延迟释放锁，避免其他线程在刷新生效的瞬间窗口内读到旧数据</p>
         */
        public CLockBuilder unlockDelay(Duration unlockDelay) {
            this.unlockDelay = unlockDelay;
            return this;
        }

        /**
         * 执行无返回值业务
         */
        public void execute(Runnable runnable) {
            doExecute(() -> {
                runnable.run();
                return null;
            });
        }

        /**
         * 执行有返回值业务
         */
        public <T> T execute(Supplier<T> callable) {
            return doExecute(callable);
        }

        /**
         * 加锁-执行-解锁统一模板
         * <p>解锁走 unlockSafely：若配置了 unlockDelay 则延迟对应时长再释放锁，否则立即释放</p>
         */
        private <T> T doExecute(Supplier<T> supplier) {
            RLock lock = getLock(lockKey);
            if (!tryAcquire(lock)) {
                onLockFail.accept(lock);
                return null;
            }
            try {
                return supplier.get();
            } finally {
                unlockSafely(lock);
            }
        }

        private boolean tryAcquire(RLock lock) {
            try {
                boolean acquired;
                if (leaseTime > 0) {
                    acquired = tryLockWithLeaseTime(lock, waitTime, leaseTime, leaseTimeUnit);
                } else {
                    acquired = CLockService.this.tryLock(lock, waitTime);
                }
                if (acquired) {
                    log.info("加锁成功: {}", lockKey);
                }
                return acquired;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("加锁被中断: {}", lockKey, e);
                return false;
            }
        }

        private boolean tryLockWithLeaseTime(RLock lock, Duration waitDuration, long leaseTime, TimeUnit leaseTimeUnit) throws InterruptedException {
            if (TimeoutUtils.hasMillis(waitDuration)) {
                return lock.tryLock(waitDuration.toMillis(), leaseTime, leaseTimeUnit);
            }
            return lock.tryLock(waitDuration.getSeconds(), leaseTime, leaseTimeUnit);
        }

        /**
         * 安全释放锁
         * <p>仅当前线程持有锁时才释放；若配置了 unlockDelay（大于 0），
         * 先延迟对应时长再释放锁，否则立即释放。释放异常仅记录日志不抛出。</p>
         */
        private void unlockSafely(RLock lock) {
            if (lock.isHeldByCurrentThread()) {
                if (!unlockDelay.isZero() && !unlockDelay.isNegative()) {
                    try {
                        Thread.sleep(unlockDelay.toMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.info("延迟释放锁被中断: {}", lockKey, e);
                    }
                }
                try {
                    lock.unlock();
                    log.info("释放锁成功: {}", lockKey);
                } catch (Exception e) {
                    log.info("释放锁异常: {}", lockKey, e);
                }
            }
        }
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

    @Deprecated
    public boolean tryLockThenRun(String key, CRunnable runnable) {
        return null != tryLockThenRun(key, CObjUtils.toSupplier(runnable), null);
    }

    @Deprecated
    public <T> T tryLockThenRun(String key, Supplier<T> valueSupplier) {
        return tryLockThenRun(key, valueSupplier, null);
    }

    @Deprecated
    public boolean tryLockThenRun(String key, CRunnable runnable, CRunnable failureRunnable) {
        return null != tryLockThenRun(key, 0, null, CObjUtils.toSupplier(runnable), failureRunnable);
    }
    @Deprecated
    public <T> T tryLockThenRun(String key, Supplier<T> valueSupplier, CRunnable failureRunnable) {
        return tryLockThenRun(key, 0, null, valueSupplier, failureRunnable);
    }

    @Deprecated
    public boolean tryLockThenRun(String key, Duration waitDuration, CRunnable runnable) {
        return null != tryLockThenRun(key, waitDuration, CObjUtils.toSupplier(runnable), null);
    }
    @Deprecated
    public <T> T tryLockThenRun(String key, Duration waitDuration, CSupplier<T> valueSupplier) {
        return tryLockThenRun(key, waitDuration, valueSupplier, null);
    }

    /**
     * 尝试加锁并执行
     * @param key 锁key
     * @param waitDuration 等锁时长
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 操作结果
     */
    @Deprecated
    public boolean tryLockThenRun(String key, Duration waitDuration,
                                  CRunnable runnable, CRunnable failureRunnable) {
        return null != tryLockThenRun(key, waitDuration, CObjUtils.toSupplier(runnable), failureRunnable);
    }

    @Deprecated
    public <T> T tryLockThenRun(String key, Duration waitDuration,
                                CSupplier<T> valueSupplier, CRunnable failureRunnable) {

        long timeout = 0;
        TimeUnit timeUnit = null;

        if(null != waitDuration) {
            if (TimeoutUtils.hasMillis(waitDuration)) {
                timeout = waitDuration.toMillis();
                timeUnit = TimeUnit.MILLISECONDS;
            } else {
                timeout = waitDuration.getSeconds();
                timeUnit = TimeUnit.SECONDS;
            }
        }

        return tryLockThenRun(key, timeout, timeUnit, valueSupplier, failureRunnable);
    }

    @Deprecated
    public boolean tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                  CRunnable runnable) {
        return null != tryLockThenRun(key, waitTime, timeUnit, CObjUtils.toSupplier(runnable), null);
    }
    @Deprecated
    public <T> T tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                Supplier<T> valueSupplier) {
        return tryLockThenRun(key, waitTime, timeUnit, valueSupplier, null);
    }

    @Deprecated
    public boolean tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                  CRunnable runnable, CRunnable failureRunnable) {
        return null != tryLockThenRun(key, waitTime, timeUnit, CObjUtils.toSupplier(runnable), failureRunnable);
    }

    @Deprecated
    public <T> T tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                Supplier<T> valueSupplier, CRunnable failureRunnable) {

        RLock lock = getLock(key);

        boolean locked = Objects.isNull(timeUnit)
                ? tryLock(lock)
                : tryLock(lock, waitTime, timeUnit);
        if(locked) {
            log.info("tryLockThenRun lock success, key: {}", key);
            try {
                return valueSupplier.get();
            } finally {
                lock.unlock();
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
