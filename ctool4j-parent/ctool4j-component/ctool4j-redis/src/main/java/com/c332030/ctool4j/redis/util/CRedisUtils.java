package com.c332030.ctool4j.redis.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.c332030.ctool4j.core.util.CIdUtils;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    public String getKey(Class<?> clazz, ICOperate icOperate, Object key) {
        return getKey(clazz, icOperate.getName(), key);
    }

    public String getKey(Class<?> clazz, Object... keys) {

        val keyList = new ArrayList<>();
        keyList.add(CSpringUtils.getApplicationName());
        keyList.add(clazz.getSimpleName());
        keyList.addAll(Arrays.asList(keys));

        return StrUtil.join(
            KEY_SEPARATOR,
            keyList
        );
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

        if (null == value) {
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
            + "if current ~= false and current == ARGV[1] then "
            + "    local ttl = tonumber(ARGV[3])"
            + "    if ttl and ttl > 0 then "
            + "         redis.call('set', KEYS[1], ARGV[2], 'EX', ttl)"
            + "    else "
            + "         redis.call('set', KEYS[1], ARGV[2])"
            + "    end "
            + "    return 1 "
            + "else "
            + "    return 0 "
            + "end";

    private final RedisScript<Long> COMPARE_AND_SETSCRIPT = new DefaultRedisScript<>(COMPARE_AND_SET, Long.class);

    public boolean compareAndSet(String key, Object expectedValue, Object newValue, long ttl) {

        if (null == expectedValue
            || null == newValue
        ) {
            return false;
        }

        val result = getRedisTemplate().execute(
            COMPARE_AND_SETSCRIPT,
            Collections.singletonList(key),
            expectedValue,
            newValue,
            String.valueOf(ttl)
        );
        return result == 1;
    }

    public boolean compareAndSet(String key, Object expectedValue, Object newValue) {
        return compareAndSet(key, expectedValue, newValue, 0L);
    }

    private final String SET_IF_NOT_EQUALS =
        "local current = redis.call('get', KEYS[1])"
            + "if current == false or current ~= ARGV[1] then "
            + "    local ttl = tonumber(ARGV[3])"
            + "    if ttl and ttl > 0 then "
            + "         redis.call('set', KEYS[1], ARGV[2], 'EX', ttl) "
            + "    else "
            + "         redis.call('set', KEYS[1], ARGV[2]) "
            + "    end "
            + "    return 1 "
            + "else "
            + "    return 0 "
            + "end";

    private final RedisScript<Long> SET_IF_NOT_EQUALS_SETSCRIPT = new DefaultRedisScript<>(SET_IF_NOT_EQUALS, Long.class);

    public boolean setIfNotEquals(String key, Object newValue, long ttl) {

        if (null == newValue) {
            return false;
        }

        val result = getRedisTemplate().execute(
            SET_IF_NOT_EQUALS_SETSCRIPT,
            Collections.singletonList(key),
            newValue,
            String.valueOf(ttl)
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

        if (setIfAbsent(key, cacheDuration)) {
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

    public Long incr(String key) {
        return redisService.incr(key);
    }

    public Long incr(String key, long delta) {
        return redisService.incr(key, delta);
    }

    public final String INCR_EXPIRE =
        "local current = redis.call('incrby', KEYS[1], ARGV[1]) "
            + "if current == tonumber(ARGV[1]) then "
            + "    redis.call('expire', KEYS[1], ARGV[2]) "
            + "end "
            + "return current"
        ;

    private final RedisScript<Long> INCR_EXPIRE_SCRIPT = new DefaultRedisScript<>(INCR_EXPIRE, Long.class);

    /**
     * 自增带超时时间
     * @param key key
     * @param delta 步长
     * @param duration 超时时间
     * @return 自增值
     */
    public Long incrExpire(String key, long delta, Duration duration) {
        return getRedisTemplate().execute(
            INCR_EXPIRE_SCRIPT,
            Collections.singletonList(key),
            String.valueOf(delta),
            String.valueOf(duration.getSeconds())
        );
    }

    /**
     * 自增带超时时间
     * @param key key
     * @param duration 超时时间
     * @return 自增值
     */
    public Long incrExpire(String key, Duration duration) {
        return incrExpire(key, 1, duration);
    }

    public final String BIZ_ID_INCR_KEY = "{}:biz_id:incr:{}";

    /**
     * 获取自增的业务id
     *
     * @param keyBefore  前缀
     * @param incrLength 自增id长度
     * @return 业务id
     */
    public String getIncrBizId(String keyBefore, int incrLength) {

        val incrKey = StrUtil.format(BIZ_ID_INCR_KEY, SpringUtil.getApplicationName(), keyBefore);
        val incrValue = incr(incrKey);

        val keyAfter = StrUtil.fillBefore(String.valueOf(incrValue), '0', incrLength);
        return keyBefore + keyAfter;
    }

    /**
     * 获取自增的业务id
     *
     * @param keyBefore  前缀
     * @param incrLength 自增id长度
     * @return 业务id
     */
    public String getIncrExpireBizId(String keyBefore, Duration duration, int incrLength) {

        val incrKey = StrUtil.format(BIZ_ID_INCR_KEY, SpringUtil.getApplicationName(), keyBefore);
        val incrValue = incrExpire(incrKey, duration);

        val keyAfter = StrUtil.fillBefore(String.valueOf(incrValue), '0', incrLength);
        return keyBefore + keyAfter;
    }

    private static final Duration DATE_INCR_EXPIRE_DURATION = Duration.ofDays(3);

    /**
     * 获取日期+自增的业务id
     *
     * @param entityClass 实体类
     * @param incrLength  自增id长度
     * @return 业务id
     */
    public String getDateIncrBizId(Class<?> entityClass, int incrLength) {

        val keyBefore = CIdUtils.getPrefix(entityClass)
            + CDateUtils.formatPureDate(Instant.now());
        return getIncrExpireBizId(
            keyBefore,
            DATE_INCR_EXPIRE_DURATION,
            incrLength
        );
    }

    private static final Duration DATETIME_INCR_EXPIRE_DURATION = Duration.ofSeconds(3);

    /**
     * 获取日期时间+自增的业务id
     *
     * @param entityClass 实体类
     * @param incrLength  自增id长度
     * @return 业务id
     */
    public String getDateTimeIncrBizId(Class<?> entityClass, int incrLength) {

        val keyBefore = CIdUtils.getPrefix(entityClass)
            + CDateUtils.formatPureDateTime(Instant.now());
        return getIncrExpireBizId(
            keyBefore,
            DATETIME_INCR_EXPIRE_DURATION,
            incrLength
        );
    }

}
