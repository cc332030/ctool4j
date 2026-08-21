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
 * @see doc/design/redis/CLockService.adoc
 * @see doc/design/redis/CLockServiceTests.adoc
 * @since 2025/11/3
 */
@CustomLog
@Service
@AllArgsConstructor
public class CLockService {

    RedissonClient redissonClient;

    /**
     * 获取指定 key 的分布式锁
     * @param key 锁 key
     * @return 分布式锁
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * 创建锁构建器
     *
     * @param lockKey 锁 key
     * @return 锁构建器
     */
    public CLockBuilder lock(String lockKey) {
        return new CLockBuilder(lockKey);
    }

    /**
     * 创建锁构建器，支持 StrUtil.format 格式化 lockKey
     *
     * @param format 格式串，如 "sign:report:{}:{}"
     * @param args   格式化参数
     * @return 锁构建器
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

        /**
         * 锁 key
         */
        final String lockKey;

        /**
         * 等待获取锁的超时时间，默认不等待
         */
        Duration waitTime = Duration.ZERO;

        /**
         * 持锁时间，默认 -1 启用 watchdog 自动续期
         */
        long leaseTime = -1;

        /**
         * 持锁时间单位，默认秒
         */
        TimeUnit leaseTimeUnit = TimeUnit.SECONDS;

        /**
         * 释放锁前延迟时间，默认不延迟（立即释放）。
         * <p>注意：延迟期间锁仍被当前线程持有，期间其他线程获取锁将失败/等待，仅在确有需要时配置</p>
         */
        Duration unlockDelay = Duration.ZERO;

        /**
         * 获取锁失败回调，默认空操作
         */
        CConsumer<RLock> onLockFail = lock -> {};

        CLockBuilder(String lockKey) {
            this.lockKey = lockKey;
        }

        /**
         * 等待获取锁的超时时间（秒），默认不等待
         * @param waitTime 等待秒数
         * @return this
         */
        public CLockBuilder waitTime(long waitTime) {
            return waitTime(Duration.ofSeconds(waitTime));
        }

        /**
         * 等待获取锁的超时时间
         * @param waitDuration 等待时长
         * @return this
         */
        public CLockBuilder waitTime(Duration waitDuration) {
            this.waitTime = waitDuration;
            return this;
        }

        /**
         * 持锁时间（秒），默认 -1 启用 watchdog 自动续期
         * @param leaseTime 持锁秒数
         * @return this
         */
        public CLockBuilder leaseTime(long leaseTime) {
            return leaseTime(leaseTime, TimeUnit.SECONDS);
        }

        /**
         * 持锁时间
         * @param leaseTime 持锁时长
         * @param timeUnit 时间单位
         * @return this
         */
        public CLockBuilder leaseTime(long leaseTime, TimeUnit timeUnit) {
            this.leaseTime = leaseTime;
            this.leaseTimeUnit = timeUnit;
            return this;
        }

        /**
         * 获取锁失败时的回调
         * @param onLockFail 获取锁失败时执行的回调，参数为锁对象
         * @return this
         */
        public CLockBuilder onLockFail(CConsumer<RLock> onLockFail) {
            this.onLockFail = onLockFail;
            return this;
        }

        /**
         * 释放锁前延迟时间，默认不延迟
         * <p>延迟期间锁仍被当前线程持有，期间其他线程获取锁将失败/等待，仅在确有需要时配置</p>
         * @param unlockDelay 释放锁前延迟时长
         * @return this
         */
        public CLockBuilder unlockDelay(Duration unlockDelay) {
            this.unlockDelay = unlockDelay;
            return this;
        }

        /**
         * 执行无返回值业务
         * <p>获取锁失败时执行 {@link #onLockFail} 回调，是否抛异常由回调决定，默认空操作不抛</p>
         *
         * @param runnable 锁内执行的业务
         */
        public void execute(Runnable runnable) {
            doExecute(() -> {
                runnable.run();
                return null;
            });
        }

        /**
         * 执行有返回值业务
         * <p>获取锁失败时执行 {@link #onLockFail} 回调并返回 null，是否抛异常由回调决定，默认空操作不抛</p>
         *
         * @param callable 锁内执行的业务
         * @return 业务返回值，获取锁失败时返回 null
         * @param <T> 返回值泛型
         */
        public <T> T execute(Supplier<T> callable) {
            return doExecute(callable);
        }

        /**
         * 加锁-执行-解锁统一模板
         * <p>获取锁失败时执行 {@link #onLockFail} 回调并返回 null，是否抛异常由回调决定；
         * 解锁走 unlockSafely：若配置了 unlockDelay 则延迟对应时长再释放锁，否则立即释放</p>
         */
        private <T> T doExecute(Supplier<T> supplier) {
            RLock lock = getLock(lockKey);
            if (tryAcquireFailure(lock)) {
                onLockFail.accept(lock);
                return null;
            }
            try {
                return supplier.get();
            } finally {
                unlockSafely(lock);
            }
        }

        private boolean tryAcquireFailure(RLock lock) {
            try {
                boolean acquired;
                if (leaseTime > 0) {
                    acquired = tryLockWithLeaseTime(lock, waitTime, leaseTime, leaseTimeUnit);
                } else {
                    acquired = CLockService.this.tryLock(lock, waitTime);
                }
                if (acquired) {
                    log.debug("加锁成功: {}", lockKey);
                }
                return !acquired;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("加锁被中断: {}", lockKey, e);
                return true;
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
                    log.debug("释放锁成功: {}", lockKey);
                } catch (Exception e) {
                    log.error("释放锁异常: {}", lockKey, e);
                }
            }
        }
    }

    /**
     * 尝试加锁，不等待
     * @param key 锁 key
     * @return true 加锁成功
     */
    public boolean tryLock(String key) {
        return tryLock(getLock(key));
    }

    /**
     * 尝试加锁，不等待
     * @param lock 分布式锁
     * @return true 加锁成功
     */
    public boolean tryLock(RLock lock) {
        return lock.tryLock();
    }

    /**
     * 尝试加锁，等待指定时长
     * @param key 锁 key
     * @param waitTime 等待时长
     * @param timeUnit 时间单位
     * @return true 加锁成功
     */
    public boolean tryLock(String key, long waitTime, TimeUnit timeUnit) {
        return tryLock(getLock(key), waitTime, timeUnit);
    }

    /**
     * 尝试加锁，等待指定时长
     * @param lock 分布式锁
     * @param waitTime 等待时长
     * @param timeUnit 时间单位
     * @return true 加锁成功
     */
    @SneakyThrows
    public boolean tryLock(RLock lock, long waitTime, TimeUnit timeUnit) {
        return lock.tryLock(waitTime, timeUnit);
    }

    /**
     * 尝试加锁，等待指定时长
     * @param lock 分布式锁
     * @param waitDuration 等待时长
     * @return true 加锁成功
     */
    public boolean tryLock(RLock lock, Duration waitDuration) {
        Objects.requireNonNull(waitDuration, "waitDuration can't be null");
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

    /**
     * 尝试加锁并执行操作
     *
     * @param key     锁 key
     * @param runnable 锁成功后的操作
     * @return true 表示获取锁并执行成功
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public boolean tryLockThenRun(String key, CRunnable runnable) {
        return null != tryLockThenRun(key, CObjUtils.toSupplier(runnable), null);
    }

    /**
     * 尝试加锁并执行操作，返回操作结果
     *
     * @param key          锁 key
     * @param valueSupplier 锁成功后的操作
     * @param <T>          结果类型
     * @return 操作结果；未获取到锁时返回 null
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, Supplier<T> valueSupplier) {
        return tryLockThenRun(key, valueSupplier, null);
    }

    /**
     * 尝试加锁并执行操作，未获取到锁时执行失败回调
     *
     * @param key             锁 key
     * @param runnable        锁成功后的操作
     * @param failureRunnable 未获取到锁时的回调
     * @return true 表示获取锁并执行成功
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public boolean tryLockThenRun(String key, CRunnable runnable, CRunnable failureRunnable) {
        return null != tryLockThenRun(key, 0, null, CObjUtils.toSupplier(runnable), failureRunnable);
    }
    /**
     * 尝试加锁并执行操作，未获取到锁时执行失败回调
     *
     * @param key             锁 key
     * @param valueSupplier   锁成功后的操作
     * @param failureRunnable 未获取到锁时的回调
     * @param <T>             结果类型
     * @return 操作结果；未获取到锁时返回 null
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, Supplier<T> valueSupplier, CRunnable failureRunnable) {
        return tryLockThenRun(key, 0, null, valueSupplier, failureRunnable);
    }

    /**
     * 尝试加锁并等待指定时长，成功后执行操作
     *
     * @param key          锁 key
     * @param waitDuration 等待锁的时长
     * @param runnable     锁成功后的操作
     * @return true 表示获取锁并执行成功
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public boolean tryLockThenRun(String key, Duration waitDuration, CRunnable runnable) {
        return null != tryLockThenRun(key, waitDuration, CObjUtils.toSupplier(runnable), null);
    }
    /**
     * 尝试加锁并等待指定时长，成功后执行操作
     *
     * @param key           锁 key
     * @param waitDuration  等待锁的时长
     * @param valueSupplier 锁成功后的操作
     * @param <T>           结果类型
     * @return 操作结果；未获取到锁时返回 null
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
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

    /**
     * 尝试加锁并等待指定时长，成功后执行操作，未获取到锁时执行失败回调
     *
     * @param key             锁 key
     * @param waitDuration    等待锁的时长
     * @param valueSupplier   锁成功后的操作
     * @param failureRunnable 未获取到锁时的回调
     * @param <T>             结果类型
     * @return 操作结果；未获取到锁时返回 null
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
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

    /**
     * 尝试加锁并等待指定时长，成功后执行操作
     *
     * @param key      锁 key
     * @param waitTime 等待锁的时长数值
     * @param timeUnit 等待时长的时间单位
     * @param runnable 锁成功后的操作
     * @return true 表示获取锁并执行成功
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public boolean tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                  CRunnable runnable) {
        return null != tryLockThenRun(key, waitTime, timeUnit, CObjUtils.toSupplier(runnable), null);
    }
    /**
     * 尝试加锁并等待指定时长，成功后执行操作
     *
     * @param key           锁 key
     * @param waitTime      等待锁的时长数值
     * @param timeUnit      等待时长的时间单位
     * @param valueSupplier 锁成功后的操作
     * @param <T>           结果类型
     * @return 操作结果；未获取到锁时返回 null
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                Supplier<T> valueSupplier) {
        return tryLockThenRun(key, waitTime, timeUnit, valueSupplier, null);
    }

    /**
     * 尝试加锁并等待指定时长，成功后执行操作，未获取到锁时执行失败回调
     *
     * @param key             锁 key
     * @param waitTime        等待锁的时长数值
     * @param timeUnit        等待时长的时间单位
     * @param runnable        锁成功后的操作
     * @param failureRunnable 未获取到锁时的回调
     * @return true 表示获取锁并执行成功
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public boolean tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                  CRunnable runnable, CRunnable failureRunnable) {
        return null != tryLockThenRun(key, waitTime, timeUnit, CObjUtils.toSupplier(runnable), failureRunnable);
    }

    /**
     * 尝试加锁并等待指定时长，成功后执行操作，未获取到锁时执行失败回调
     *
     * @param key             锁 key
     * @param waitTime        等待锁的时长数值
     * @param timeUnit        等待时长的时间单位
     * @param valueSupplier   锁成功后的操作
     * @param failureRunnable 未获取到锁时的回调
     * @param <T>             结果类型
     * @return 操作结果；未获取到锁时返回 null
     * @deprecated 已废弃，建议使用 tryLock(String key) + Lock 的调用方式
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, long waitTime, TimeUnit timeUnit,
                                Supplier<T> valueSupplier, CRunnable failureRunnable) {

        RLock lock = getLock(key);

        boolean locked = Objects.isNull(timeUnit)
                ? tryLock(lock)
                : tryLock(lock, waitTime, timeUnit);
        if(locked) {
            log.debug("tryLockThenRun lock success, key: {}", key);
            try {
                return valueSupplier.get();
            } finally {
                lock.unlock();
                log.debug("tryLockThenRun unlock success, key: {}", key);
            }
        } else {
            log.debug("tryLockThenRun wait timeout, key: {}, waitTime: {}, timeUnit: {}", key, waitTime, timeUnit);
            if(null != failureRunnable) {
                failureRunnable.run();
            }
        }

        return null;
    }

}
