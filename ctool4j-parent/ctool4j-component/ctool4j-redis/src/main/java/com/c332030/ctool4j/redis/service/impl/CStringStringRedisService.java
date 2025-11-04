package com.c332030.ctool4j.redis.service.impl;

import com.c332030.ctool4j.redis.service.CAbstractRedisService;
import com.c332030.ctool4j.spring.annotation.ConditionalOnGenericBean;
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
@ConditionalOnGenericBean(
        type = RedisTemplate.class,
        genericTypes = {
                String.class,
                String.class,
        }
)
public class CStringStringRedisService extends CAbstractRedisService<String, String> {

}
