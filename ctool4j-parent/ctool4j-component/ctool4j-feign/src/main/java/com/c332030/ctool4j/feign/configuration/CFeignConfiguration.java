package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.client.CFeignClient;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import feign.Client;
import feign.Logger;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CFeignConfiguration
 * </p>
 *
 * @see "doc/design/feign/CFeignConfiguration.adoc"
 * @since 2025/9/21
 */
@CustomLog
@Configuration
public class CFeignConfiguration {

    /**
     * Feign 拦截器
     *
     * @return Feign 拦截器
     */
    @Bean
    public CFeignInterceptor cFeignInterceptor() {
        return new CFeignInterceptor();
    }

//    @Bean
    /**
     * Feign 客户端（带日志）
     *
     * @param client         底层客户端
     * @param feignLogConfig 日志配置
     * @return Feign 客户端
     */
    public CFeignClient cFeignClient(Client client, CFeignClientLogConfig feignLogConfig) {
        return new CFeignClient(client, feignLogConfig);
    }

    /**
     * Feign 日志级别（默认 FULL）
     *
     * @return 日志级别
     */
    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level cFeignLoggerLevel() {
        // 默认 FULL：打印 feign http 请求与响应全文（无自定义 Logger.Level 时启用），便于排查，注意流量与敏感信息
        log.debug("feign Logger.Level 默认 FULL，未自定义 Logger.Level Bean 时将打印 http 请求/响应全文");
        return Logger.Level.FULL;
    }

    /**
     * Feign 日志实现
     *
     * @param feignLogConfig 日志配置
     * @return 日志实现
     */
    @Bean
    @ConditionalOnMissingBean(Logger.class)
    public Logger cFeignLogger(CFeignClientLogConfig feignLogConfig) {
        return new CFeignLogger(feignLogConfig);
    }

}
