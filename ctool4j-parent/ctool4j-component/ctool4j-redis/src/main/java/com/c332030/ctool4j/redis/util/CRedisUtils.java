package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.definition.interfaces.ICOperate;
import com.c332030.ctool4j.redis.service.impl.CObjectValueRedisService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

/**
 * <p>
 * Description: CRedisUtils
 * </p>
 *
 * @since 2025/11/10
 */
@UtilityClass
public class CRedisUtils {

    public final String KEY_SEPARATOR = ":";

    @CAutowired
    CObjectValueRedisService redisService;

    public static String getKey(Class<?> clazz, ICOperate ICOperate, Object key) {
        return CSpringUtils.getApplicationName()
                + KEY_SEPARATOR
                + clazz.getSimpleName()
                + KEY_SEPARATOR
                + ICOperate.getName()
                + KEY_SEPARATOR
                + key
                ;
    }

    private static final String SET_IF_LAGER = "local current = redis.call('GET',  KEYS[1])\n" +
        "local currentNum = 0  -- 默认值\n" +
        "if current ~= false then  -- 判断非空 [3]()\n" +
        "    currentNum = tonumber(current)  -- 显式转换 [2]()\n" +
        "end\n" +
        "if tonumber(ARGV[1]) > currentNum then\n" +
        "    redis.call('SET',  KEYS[1], ARGV[1])\n" +
        "    return 1  -- 更新成功\n" +
        "end\n" +
        "return 0";

    private static final RedisScript<Long> SET_IF_LAGER_SCRIPT = new DefaultRedisScript<>(SET_IF_LAGER, Long.class);

    public boolean setIfLager(String key, Number value) {

        if(null == value) {
            return false;
        }

        val result = redisService.getRedisTemplate().execute(
            SET_IF_LAGER_SCRIPT,
            Collections.singletonList(key),
            value
        );
        return result == 1;
    }

}
