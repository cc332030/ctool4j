package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import com.c332030.ctool4j.feign.targets.CTarget;
import com.c332030.ctool4j.feign.targets.ICTarget;
import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Target;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CFeignConfiguration
 * </p>
 *
 * @since 2025/9/21
 */
@Configuration
public class CFeignConfiguration {

    @Bean
    @ConditionalOnMissingBean(Targeter.class)
    public Targeter cTargeter() {
        return (target, feign, context, factory) -> {

            val icTarget = CTarget.<>builder()
                .type(target.getType())
                .build();

            // 核心逻辑：将默认Target替换为自定义的动态Target
            Target<?> dynamicTarget = new DynamicFeignTarget<>(target.type(), target.name());
            // 调用默认Targeter的逻辑，传入动态Target
            return new Default().target(dynamicTarget, feign, context, factory);
        };
    }

    @Bean
    public Feign.Builder cFeignBuilder() {
        return Feign.builder()
            .targeter((target, feign) -> {
                return feign.newInstance(new ICTarget<Object>() {
                    feign.Types
                });
            });
    }

    @Bean
    public CFeignInterceptor cFeignInterceptor() {
        return new CFeignInterceptor();
    }

//    @Bean
    public CFeignClient feignClient(Client client, CFeignClientLogConfig feignLogConfig) {
        return new CFeignClient(client, feignLogConfig);
    }

    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level cFeignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @ConditionalOnMissingBean(Logger.class)
    public Logger cFeignLogger(CFeignClientLogConfig feignLogConfig) {
        return new CFeignLogger(feignLogConfig);
    }

}
