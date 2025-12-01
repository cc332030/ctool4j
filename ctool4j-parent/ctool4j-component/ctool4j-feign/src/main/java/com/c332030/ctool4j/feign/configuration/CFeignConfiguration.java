package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import feign.Client;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * Description: CFeignConfiguration
 * </p>
 *
 * @since 2025/9/21
 */
//@Configuration
public class CFeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor(CFeignLogConfig feignLogConfig) {
        return new CFeignInterceptor(feignLogConfig);
    }

    @Bean
    public CFeignClient feignClient(CFeignLogConfig feignLogConfig) {

//        val client = Opt.ofNullable(httpClient)
//                .map(e -> (Client)new ApacheHttpClient(e))
//                .orElseGet(() -> new Client.Default(null, null));

        return new CFeignClient(new Client.Default(null, null), feignLogConfig);
    }

}
