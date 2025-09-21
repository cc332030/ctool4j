package com.c332030.ctool.feign.configuration;

import com.c332030.ctool.feign.client.CFeignClient;
import com.c332030.ctool.feign.config.CFeignLogConfig;
import com.c332030.ctool.feign.interceptor.CFeignInterceptor;
import feign.Client;
import feign.RequestInterceptor;
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
    public CFeignClient feignClient(Client client, CFeignLogConfig feignLogConfig) {
        return new CFeignClient(client, feignLogConfig);
    }

}
