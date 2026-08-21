package com.c332030.ctool4j.redis.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.c332030.ctool4j.core.util.CIdUtils;
import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.interfaces.ICOperate;
import com.c332030.ctool4j.redis.service.impl.CObjectValueRedisService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import lombok.CustomLog;
import lombok.Setter;
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
 * @see "doc/design/redis/CRedisUtils.adoc"
 * @see "doc/design/redis/CRedisUtilsTests.adoc"
 * @since 2025/11/10
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CRedisUtils {

    /**
     * key 各段的分隔符
     */
    public final String KEY_SEPARATOR = ":";

    @Setter
    @CAutowired
    CSpringApplicationConfig springApplicationConfig;

    @Setter
    @CAutowired
    CObjectValueRedisService redisService;

    @Setter
    @CAutowired
    CStringStringRedisService stringStringRedisService;

    /**
     * 获取对象值 RedisTemplate
     *
     * @return 对象值 RedisTemplate
     */
    public RedisTemplate<? super String, Object> getRedisTemplate() {
        return redisService.getRedisTemplate();
    }

    /**
     * 获取 String 值 RedisTemplate
     *
     * @return String 值 RedisTemplate
     */
    public RedisTemplate<String, String> getStringStringRedisTemplate() {
        return stringStringRedisService.getRedisTemplate();
    }

    /**
     * 获取应用前缀，优先取分组，其次取应用名
     *
     * @return 应用前缀
     */
    public String getApplicationPrefix() {
        return StrUtil.emptyToDefault(
            springApplicationConfig.getGroup(),
            springApplicationConfig.getName()
        );
    }

    /**
     * 生成带操作名的 key
     *
     * @param clazz     业务类
     * @param icOperate 操作
     * @param key       业务 key
     * @return 拼接后的 key
     */
    public String getKey(Class<?> clazz, ICOperate icOperate, Object key) {
        return getKey(clazz, icOperate.getName(), key);
    }

    /**
     * 生成由应用前缀、类简单名与业务 key 段拼接的 key
     *
     * @param clazz 业务类
     * @param keys  业务 key 段
     * @return 拼接后的 key
     */
    public String getKey(Class<?> clazz, Object... keys) {

        val keyList = new ArrayList<>();
        keyList.add(getApplicationPrefix());
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

    /**
     * 仅当新值大于当前值时设置，原子操作
     *
     * @param key   key
     * @param value 新值
     * @return true 表示设置成功
     */
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

    /**
     * 仅当当前值与期望值相等时更新，可指定过期时长，原子操作
     *
     * <p>期望值/新值统一按字符串比较（String.valueOf 转字符串），使用字符串序列化模板执行脚本，
     * 避免对象模板（JSON 带引号）与字符串存储字节不一致导致比较恒失败；
     * 存储的 key 需由字符串序列化模板（stringStringRedisService）写入，否则比较结果不受保证</p>
     *
     * @param key           key
     * @param expectedValue 期望的当前值（按字符串比较）
     * @param newValue      新值（按字符串比较）
     * @param ttl           过期时长（秒），小于等于 0 表示不设置过期
     * @return true 表示更新成功
     */
    public boolean compareAndSet(String key, Object expectedValue, Object newValue, long ttl) {

        if (null == expectedValue
            || null == newValue
        ) {
            return false;
        }

        val result = getStringStringRedisTemplate().execute(
            COMPARE_AND_SETSCRIPT,
            Collections.singletonList(key),
            String.valueOf(expectedValue),
            String.valueOf(newValue),
            String.valueOf(ttl)
        );
        return result == 1;
    }

    /**
     * 仅当当前值与期望值相等时更新，原子操作
     *
     * @param key           key
     * @param expectedValue 期望的当前值
     * @param newValue      新值
     * @return true 表示更新成功
     */
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

    /**
     * 仅当当前值与新值不相等时设置，可指定过期时长，原子操作
     *
     * @param key      key
     * @param newValue 新值
     * @param ttl      过期时长（秒），小于等于 0 表示不设置过期
     * @return true 表示设置成功
     */
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

    /**
     * 仅当当前值与新值不相等时设置，原子操作
     *
     * @param key      key
     * @param newValue 新值
     * @return true 表示设置成功
     */
    public boolean setIfNotEquals(String key, Object newValue) {
        return setIfNotEquals(key, newValue, 0L);
    }

    /**
     * 仅当 key 不存在时设置，原子操作
     *
     * @param key key
     * @return true 表示设置成功
     */
    public boolean setIfAbsent(String key) {
        return redisService.setIfAbsent(key, 1);
    }

    /**
     * 仅当 key 不存在时设置并指定过期时长，原子操作
     *
     * @param key     key
     * @param timeout 过期时长
     * @return true 表示设置成功
     */
    public boolean setIfAbsent(String key, Duration timeout) {
        return redisService.setIfAbsent(key, 1, timeout);
    }

    /**
     * 尝试仅执行一次指定操作，执行异常时回滚删除 key
     *
     * @param key           key
     * @param cacheDuration 缓存时长
     * @param runnable      要执行的操作
     * @return true 表示本次获取到执行权并执行
     */
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

    /**
     * 自增
     *
     * @param key key
     * @return 自增后的值
     */
    public Long incr(String key) {
        return redisService.incr(key);
    }

    /**
     * 按步长自增
     *
     * @param key   key
     * @param delta 步长
     * @return 自增后的值
     */
    public Long incr(String key, long delta) {
        return redisService.incr(key, delta);
    }

    /**
     * 自增脚本，首次自增时设置过期时间
     */
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
        return getStringStringRedisTemplate().execute(
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

    /**
     * 自增业务 id 的 key 格式串
     */
    public final String BIZ_ID_INCR_KEY = "{}:biz_id:incr:{}";

    /**
     * 获取自增的业务id
     *
     * @param keyBefore  前缀
     * @param incrLength 自增id长度
     * @return 业务id
     */
    public String getIncrBizId(String keyBefore, int incrLength) {

        val incrKey = StrUtil.format(BIZ_ID_INCR_KEY, getApplicationPrefix(), keyBefore);
        val incrValue = incr(incrKey);

        val keyAfter = StrUtil.fillBefore(String.valueOf(incrValue), '0', incrLength);
        return keyBefore + keyAfter;
    }

    /**
     * 获取自增的业务id
     *
     * @param keyBefore  前缀
     * @param duration   自增过期时长
     * @param incrLength 自增id长度
     * @return 业务id
     */
    public String getIncrExpireBizId(String keyBefore, Duration duration, int incrLength) {

        val incrKey = StrUtil.format(BIZ_ID_INCR_KEY, getApplicationPrefix(), keyBefore);
        val incrValue = incrExpire(incrKey, duration);

        val keyAfter = StrUtil.fillBefore(String.valueOf(incrValue), '0', incrLength);
        return keyBefore + keyAfter;
    }

    private static final Duration DATE_INCR_EXPIRE_DURATION = Duration.ofHours(25);

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

    private static final Duration DATETIME_INCR_EXPIRE_DURATION = Duration.ofMinutes(1);

    /**
     * 获取日期时间+自增的业务id
     *
     * @param entityClass 实体类
     * @param incrLength  自增id长度
     * @return 业务id
     */
    public String getDateTimeIncrBizId(Class<?> entityClass, int incrLength) {
        return getDateTimeIncrBizId(CIdUtils.getPrefix(entityClass), incrLength);
    }

    /**
     * 获取日期时间+自增的业务id
     *
     * @param prefix 前缀
     * @param incrLength  自增id长度
     * @return 业务id
     */
    public String getDateTimeIncrBizId(String prefix, int incrLength) {

        val keyBefore = prefix
            + CDateUtils.formatPureDateTime(Instant.now());
        return getIncrExpireBizId(
            keyBefore,
            DATETIME_INCR_EXPIRE_DURATION,
            incrLength
        );
    }

}
