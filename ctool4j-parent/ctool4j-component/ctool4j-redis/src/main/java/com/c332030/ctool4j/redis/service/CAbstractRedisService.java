package com.c332030.ctool4j.redis.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * <p>
 * Description: CAbstractRedisService
 * </p>
 *
 * @since 2024/3/8
 */
public abstract class CAbstractRedisService<K, V> implements ICRedisService<K, V> {

    @Getter
    @Setter(onMethod = @__({@Autowired}))
    RedisTemplate<K, V> redisTemplate;

}
