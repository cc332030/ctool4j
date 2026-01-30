package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.interfaces.ICOperate;
import com.c332030.ctool4j.redis.service.impl.CObjectValueRedisService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;

/**
 * <p>
 * Description: CRedisUtils
 * </p>
 *
 * @since 2025/11/10
 */
@CustomLog
@UtilityClass
public class CRedisUtils {

    public final String KEY_SEPARATOR = ":";

    @CAutowired
    CObjectValueRedisService redisService;

    public RedisTemplate<? super String, Object> getRedisTemplate() {
        return redisService.getRedisTemplate();
    }

    public String getKey(Class<?> clazz, ICOperate ICOperate, Object key) {
        return CSpringUtils.getApplicationName()
                + KEY_SEPARATOR
                + clazz.getSimpleName()
                + KEY_SEPARATOR
                + ICOperate.getName()
                + KEY_SEPARATOR
                + key
                ;
    }

    private final String SET_IF_LAGER = "local current = redis.call('GET',  KEYS[1])\n" +
        "local currentNum = 0  -- 默认值\n" +
        "if current ~= false then  -- 判断非空 [3]()\n" +
        "    currentNum = tonumber(current)  -- 显式转换 [2]()\n" +
        "end\n" +
        "if tonumber(ARGV[1]) > currentNum then\n" +
        "    redis.call('SET',  KEYS[1], ARGV[1])\n" +
        "    return 1  -- 更新成功\n" +
        "end\n" +
        "return 0";

    private final RedisScript<Long> SET_IF_LAGER_SCRIPT = new DefaultRedisScript<>(SET_IF_LAGER, Long.class);

    public boolean setIfLager(String key, Number value) {

        if(null == value) {
            return false;
        }

        val result = getRedisTemplate().execute(
            SET_IF_LAGER_SCRIPT,
            Collections.singletonList(key),
            value
        );
        return result == 1;
    }

    private final String COMPARE_AND_SET =
        "local current = redis.call('get', KEYS[1])"
            + "if current == false or current == ARGV[1] then"
            + "    redis.call('set', KEYS[1], ARGV[2]) "
            + "    local ttl = tonumber(ARGV[3])"
            + "    if ttl and ttl > 0 then "
            + "         redis.call('expire', KEYS[1], ARGV[3]) "
            + "    end "
            + "    return 1 "
            + "else "
            + "    return 0 "
            + "end";

    private final RedisScript<Long> COMPARE_AND_SETSCRIPT = new DefaultRedisScript<>(COMPARE_AND_SET, Long.class);

    public boolean compareAndSet(String key, Object expectedValue, Object newValue, long ttl) {

        if(null == expectedValue
            || null == newValue
        ) {
            return false;
        }

        val result = getRedisTemplate().execute(
            COMPARE_AND_SETSCRIPT,
            Collections.singletonList(key),
            expectedValue,
            newValue,
            ttl
        );
        return result == 1;
    }

    public boolean compareAndSet(String key, Object expectedValue, Object newValue) {
        return compareAndSet(key, expectedValue, newValue, 0L);
    }

    private final String SET_IF_NOT_EQUALS =
        "local current = redis.call('get', KEYS[1])"
            + "if current == false or current ~= ARGV[1] then "
            + "    redis.call('set', KEYS[1], ARGV[2]) "
            + "    local ttl = tonumber(ARGV[3])"
            + "    if ttl and ttl > 0 then "
            + "         redis.call('expire', KEYS[1], ARGV[2]) "
            + "    end "
            + "    return 1 "
            + "else "
            + "    return 0 "
            + "end";

    private final RedisScript<Long> SET_IF_NOT_EQUALS_SETSCRIPT = new DefaultRedisScript<>(SET_IF_NOT_EQUALS, Long.class);

    public boolean setIfNotEquals(String key, Object newValue, long ttl) {

        if(null == newValue) {
            return false;
        }

        val result = getRedisTemplate().execute(
            SET_IF_NOT_EQUALS_SETSCRIPT,
            Collections.singletonList(key),
            newValue,
            ttl
        );
        return result == 1;
    }

    public boolean setIfNotEquals(String key, Object newValue) {
        return setIfNotEquals(key, newValue, 0L);
    }

    public boolean setIfAbsent(String key) {
        return redisService.setIfAbsent(key, 1);
    }

    public boolean setIfAbsent(String key, Duration timeout) {
        return redisService.setIfAbsent(key, 1, timeout);
    }

    public boolean tryDoOnce(String key, Duration cacheDuration, CRunnable runnable) {

        if(setIfAbsent(key, cacheDuration)) {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.info("delete key because of exception: {}", key);
                redisService.delete(key);
                throw e;
            }
            return true;
        }

        return false;
    }

}
