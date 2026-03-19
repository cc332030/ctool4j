package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.redis.service.CAbstractRedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CStringStringRedisService
 * </p>
 *
 * @since 2025/11/4
 */
@Service
@AllArgsConstructor
public class CStringStringRedisService extends CAbstractRedisService<String, String> {

    /**
     * 判断 key 是否无效，重写 String 类型的 key
     * @param key key
     * @return 有效性
     */
    @Override
    public boolean isInvalidKey(String key) {
        return StrUtil.isBlank(key);
    }

    private String getValueStr(Object value) {
        return CJsonUtils.toJson(value);
    }

    private <T> T getValueObj(String value, Class<T> valueClass) {
        return CJsonUtils.fromJson(value, valueClass);
    }

    private <T> T getValueObj(String value, TypeReference<T> typeReference) {
        return CJsonUtils.fromJson(value, typeReference);
    }

    /**
     * 判断 value 是否无效，重写 String 值的判断
     * @param value 值
     * @return 有效性
     */
    @Override
    public boolean isInvalidValue(String value) {
        return StrUtil.isBlank(value);
    }

    public void setValue(String key, Object value) {
        if(isInvalidKey(key)
            || Objects.isNull(value)
        ) {
            return;
        }
        setValue(key, getValueStr(value));
    }

    public void setValue(String key, Object value, Duration duration) {
        if(isInvalidKey(key)
            || Objects.isNull(value)
        ) {
            return;
        }
        setValue(key, getValueStr(value), duration);
    }

    /**
     * 设置值
     * @param key key
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {

        if(isInvalidKey(key)
            || Objects.isNull(value)
            || timeout <= 0
        ) {
            return;
        }

        opsForValue().set(key, getValueStr(value), timeout, unit);
    }

    public <T> T getValue(String key, Class<T> valueClass) {
        if(isInvalidKey(key)) {
            return null;
        }
        return getValue(key, value -> getValueObj(value, valueClass));
    }

    public <T> T getValue(String key, TypeReference<T> typeReference) {
        if(isInvalidKey(key)) {
            return null;
        }
        return getValue(key, value -> getValueObj(value, typeReference));
    }

}
