package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.redis.service.CAbstractRedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
        setValue(key, CJsonUtils.toJson(value));
    }

    public <T> T getValue(String key, Class<T> valueClass) {
        if(isInvalidKey(key)) {
            return null;
        }
        return getValue(key, value -> CJsonUtils.fromJson(value, valueClass));
    }

    public <T> T getValue(String key, TypeReference<T> typeReference) {
        if(isInvalidKey(key)) {
            return null;
        }
        return getValue(key, value -> CJsonUtils.fromJson(value, typeReference));
    }

}
