package com.c332030.ctool4j.redis;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * <p>
 * Description: CRedisTestConfiguration
 * </p>
 *
 * @since 2026/6/16
 */
@Configuration
public class CRedisTestConfiguration {

    @Bean
    @Primary
    public RedissonClient mockRedissonClient() {
        return Mockito.mock(RedissonClient.class);
    }

}
