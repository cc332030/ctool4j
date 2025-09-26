package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.redis.service.CRedisService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CCacheService
 * </p>
 *
 * @since 2025/9/26
 */
@CustomLog
@Service
@AllArgsConstructor
public class CCacheService {

    CRedisService redisService;

}
