package com.c332030.ctool4j.redis.service.impl;

import com.c332030.ctool4j.redis.service.ICRedisService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CStringStringRedisService
 * </p>
 *
 * @since 2025/11/4
 */
@Service
@AllArgsConstructor
public class CObjectValueRedisService implements ICRedisService<Object, Object> {

    /**
     * RedisTemplate ，兼容 String 和 Object 类型的 key
     * key 基本上全是 String，暂不考虑 key 不是 String 的情况
     */
    RedisTemplate<? super String, Object> redisTemplate;

    /**
     * 获取 RedisTemplate
     * @return RedisTemplate 通用
     */
    @Override
    @SuppressWarnings("unchecked")
    public RedisTemplate<Object, Object> getRedisTemplate() {
        return (RedisTemplate<Object, Object>)redisTemplate;
    }

}
