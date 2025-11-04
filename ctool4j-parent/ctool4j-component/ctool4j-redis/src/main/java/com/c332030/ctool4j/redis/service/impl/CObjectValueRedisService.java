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

    RedisTemplate<? super String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public RedisTemplate<Object, Object> getRedisTemplate() {
        return (RedisTemplate<Object, Object>)redisTemplate;
    }

}
