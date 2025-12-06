package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignClientHeaderConfig;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    public RequestInterceptor requestInterceptor(CFeignClientHeaderConfig headerConfig) {
        return new CFeignInterceptor(headerConfig);
    }

//    @Bean
    public CFeignClient feignClient(Client client, CFeignClientLogConfig feignLogConfig) {
        return new CFeignClient(client, feignLogConfig);
    }

    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @ConditionalOnMissingBean(Logger.class)
    public Logger logger(CFeignClientLogConfig feignLogConfig) {
        return new CFeignLogger(feignLogConfig);
    }

}
