package com.c332030.ctool4j.redis.configuration;

import com.c332030.ctool4j.redis.service.CAbstractObjectValueRedisService;
import com.c332030.ctool4j.redis.service.impl.CObjectObjectRedisService;
import com.c332030.ctool4j.redis.service.impl.CStringObjectRedisService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * <p>
 * Description: CRedisConfiguration
 * </p>
 *
 * @since 2025/11/4
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class CRedisConfiguration {

    @Setter(onMethod_ = @Autowired(required = false))
    private RedisTemplate<Object, Object> objectObjectRedisTemplate;

    @Setter(onMethod_ = @Autowired(required = false))
    private RedisTemplate<String, Object> stringObjectRedisTemplate;

    @Bean
    @ConditionalOnBean(name = "redisTemplate")
    public CAbstractObjectValueRedisService<?> objectObjectRedisService() {

        if(null != objectObjectRedisTemplate) {
            return new CObjectObjectRedisService();
        }

        if(null != stringObjectRedisTemplate) {
            return new CStringObjectRedisService();
        }

        throw new RuntimeException("no bean name redisTemplate");

    }

}
