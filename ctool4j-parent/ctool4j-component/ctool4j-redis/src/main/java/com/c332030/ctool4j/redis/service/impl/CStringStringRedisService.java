package com.c332030.ctool4j.redis.service.impl;

import com.c332030.ctool4j.redis.service.CAbstractRedisService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CStringStringRedisService
 * </p>
 *
 * @since 2025/11/4
 */
@Service
@ConditionalOnBean(StringRedisTemplate.class)
public class CStringStringRedisService extends CAbstractRedisService<String, String> {

}
