package com.c332030.ctool4j.redis.service.impl;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.redis.service.ICRedisService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CObjectValueRedisService
 * </p>
 *
 * @since 2025/11/4
 */
@Service
@AllArgsConstructor
public class CObjectValueRedisService implements ICRedisService<Object, Object> {

    /**
     * RedisTemplate ，兼容 String 和 Object 类型的 key
     * 有的项目 key 全是 String，兼容 String 类型的 key
     */
    RedisTemplate<? super String, Object> redisTemplate;

    /**
     * 获取 RedisTemplate
     * @return RedisTemplate 通用
     */
    @Override
    public RedisTemplate<Object, Object> getRedisTemplate() {
        return CObjUtils.anyType(redisTemplate);
    }

    public <T> T getValueForGenericType(Object key) {
        return CObjUtils.anyType(getValue(key));
    }

}
