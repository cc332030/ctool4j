package com.c332030.ctool4j.feign.configuration;

import cn.hutool.core.lang.Opt;
import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import feign.Client;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import lombok.val;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CFeignClient feignClient(
            @Autowired(required = false) HttpClient httpClient,
            CFeignLogConfig feignLogConfig
    ) {

        val client = Opt.ofNullable(httpClient)
                .map(e -> (Client)new ApacheHttpClient(e))
                .orElseGet(() -> new Client.Default(null, null));

        return new CFeignClient(client, feignLogConfig);
    }

}
