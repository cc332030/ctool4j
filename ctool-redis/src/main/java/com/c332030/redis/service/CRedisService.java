package com.c332030.redis.service;

import cn.hutool.core.util.StrUtil;
import com.c332030.core.util.CObjUtils;
import com.c332030.redis.util.CLockUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CRedisService
 * </p>
 *
 * @since 2024/3/8
 */
@CustomLog
@Service
@AllArgsConstructor
public class CRedisService {

    RedissonClient redissonClient;

    StringRedisTemplate stringRedisTemplate;

    RedisTemplate<Object, Object> redisTemplate;

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

    /**
     * 同步获取值
     */
    public <T> T computeIfAbsent(String key, Duration waitDuration,
                                 Function<T, Duration> expireDurationFunction, Supplier<T> valueSupplier) {

        T t = getValue(key);
        if(null != t) {
            return t;
        }

        return tryLockThenRun(CLockUtils.getLockKey(key), waitDuration, () -> {

            T tNew = getValue(key);
            if(null != tNew) {
                log.info("computeIfAbsent skip because exists value of key: {}", key);
                return tNew;
            }

            tNew = valueSupplier.get();
            Assert.notNull(tNew, "valueSupplier got null");

            Duration expireDuration = expireDurationFunction.apply(tNew);
            setValue(key, tNew, expireDuration);
            log.info("computeIfAbsent setValue successfully, key: {}, value: {}, expireDuration: {}",
                    key, tNew, expireDuration);

            return tNew;
        });
    }

    public ValueOperations<Object, Object> opsForValue() {
        return redisTemplate.opsForValue();
    }

    public ValueOperations<String, String> opsForStringValue() {
        return stringRedisTemplate.opsForValue();
    }

    public void setValue(String key, Object value) {
        opsForValue().set(key, value);
    }
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        opsForValue().set(key, value, timeout, unit);
    }
    public void setValue(String key, Object value, Duration timeout) {
        opsForValue().set(key, value, timeout);
    }

    public void setStringValue(String key, String value) {
        opsForStringValue().set(key, value);
    }
    public void setStringValue(String key, String value, long timeout, TimeUnit unit) {
        opsForStringValue().set(key, value, timeout, unit);
    }
    public void setStringValue(String key, String value, Duration timeout) {
        opsForStringValue().set(key, value, timeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T)opsForValue().get(key);
    }

    public String getStringValue(String key) {
        return opsForStringValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
    public void deleteString(String key) {
        stringRedisTemplate.delete(key);
    }

    public void delete(String key, Object... params) {
        delete(StrUtil.format(key, params));
    }
    public void delete(String key, Map<String, Object> params) {
        delete(StrUtil.format(key, params));
    }
    public void deleteString(String key, Object... params) {
        deleteString(StrUtil.format(key, params));
    }
    public void deleteString(String key, Map<String, Object> params) {
        deleteString(StrUtil.format(key, params));
    }

    public void setValue(String template, Object value, Object... params) {
        setValue(StrUtil.format(template, params), value);
    }
    public <T> T getValue(String template, Object... params) {
        return getValue(StrUtil.format(template, params));
    }
    public void setStringValue(String template, String value, Object... params) {
        setStringValue(StrUtil.format(template, params), value);
    }
    public String getStringValue(String template, Object... params) {
        return getStringValue(StrUtil.format(template, params));
    }

    public void setValue(String template, Object value, Map<String, Object> params) {
        setValue(StrUtil.format(template, params), value);
    }
    public <T> T getValue(String template, Map<String, Object> params) {
        return getValue(StrUtil.format(template, params));
    }

    public void setStringValue(String template, String value, Map<String, Object> params) {
        setStringValue(StrUtil.format(template, params), value);
    }
    public String getStringValue(String template, Map<String, Object> params) {
        return getStringValue(StrUtil.format(template, params));
    }

}
