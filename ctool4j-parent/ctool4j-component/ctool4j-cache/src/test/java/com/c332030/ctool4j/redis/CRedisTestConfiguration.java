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
 * <h2>功能说明</h2>
 * <p>{@code CRedisTestConfiguration} 为 Redis 测试辅助配置（{@code @Configuration}），提供 mock 的 {@code RedissonClient}（{@code @Bean @Primary}），避免测试依赖真实 Redis。</p>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>Redis/cache 功能测试，用 mock 的 {@code RedissonClient} 替代真实 Redis。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助配置；mock 实现不验证真实 Redis 行为。</li>
 * </ul>
 *
 * @since 2026/6/16
 * @version 1.0
 */
@Configuration
public class CRedisTestConfiguration {

    /**
     * 提供 mock 的 RedissonClient，避免测试依赖真实 Redis
     *
     * @return mock 的 RedissonClient
     */
    @Bean
    @Primary
    public RedissonClient mockRedissonClient() {
        return Mockito.mock(RedissonClient.class);
    }

}
