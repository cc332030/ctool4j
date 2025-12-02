package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "feign.log", name = "enable", havingValue = "true")
public class CFeignConfiguration {

//    @Bean
    public RequestInterceptor requestInterceptor(CFeignLogConfig feignLogConfig) {
        return new CFeignInterceptor(feignLogConfig);
    }

//    @Bean
    public CFeignClient feignClient(Client client, CFeignLogConfig feignLogConfig) {
        return new CFeignClient(client, feignLogConfig);
    }

    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Logger logger(CFeignLogConfig feignLogConfig) {
        return new CFeignLogger(feignLogConfig);
    }

}
