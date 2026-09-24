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
 * <h2>能力目录</h2>
 * <p>{@code CFeignConfiguration}（{@code @Configuration}）注册 Feign 相关 Bean：</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>拦截器默认注册；日志级别/日志实现按缺省注册（{@code @ConditionalOnMissingBean} 可被覆盖）。</li>
 *   <li>{@code cFeignClient} 方法当前被注释，未注册为 Bean。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>日志级别/日志实现缺失时由本配置提供默认 FULL 级别与 CFeignLogger。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>引入 ctool4j-feign 时自动装配拦截器与日志。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>用户自定义 Logger/Level 时以用户 Bean 优先（ConditionalOnMissingBean）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code cFeignClient} 未启用，日志客户端需自行装配；响应日志能力通过 CFeignLogger 提供。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@CustomLog
@Configuration
public class CFeignConfiguration {

    /**
     * Feign 拦截器
     * <ul>
     *   <li>{@code cFeignInterceptor()}：{@code CFeignInterceptor}（请求拦截器）。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code cFeignClient(client, feignLogConfig)}：{@code CFeignClient}（带日志客户端，当前注释未启用）。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code cFeignLoggerLevel()}：{@code Logger.Level.FULL}（{@code @ConditionalOnMissingBean}）。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code cFeignLogger(feignLogConfig)}：{@code CFeignLogger} 日志实现（{@code @ConditionalOnMissingBean}）。</li>
     * </ul>
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
