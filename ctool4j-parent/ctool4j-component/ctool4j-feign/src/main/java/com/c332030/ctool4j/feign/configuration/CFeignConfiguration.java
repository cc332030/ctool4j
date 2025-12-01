package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import feign.RequestInterceptor;
import org.apache.http.client.HttpClient;
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
    public RequestInterceptor requestInterceptor(CFeignLogConfig feignLogConfig) {
        return new CFeignInterceptor(feignLogConfig);
    }

    @Bean
    public CFeignClient feignClient(HttpClient httpClient, CFeignLogConfig feignLogConfig) {
        return new CFeignClient(httpClient, feignLogConfig);
    }

}
