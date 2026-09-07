package com.c332030.ctool4j.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CMybatisPlusConfiguration
 * </p>
 *
 * @since 2025/12/29
 * @see "doc/design/mybatisplus/CMybatisPlusConfiguration.adoc"
 */
@CustomLog
@Configuration
public class CMybatisPlusConfiguration {

    /**
     * 创建分页拦截器
     *
     * @return 分页拦截器
     */
    @Bean
    @ConditionalOnMissingBean(PaginationInterceptor.class)
    public PaginationInterceptor cPaginationInterceptor() {
        log.debug("默认装配分页拦截器 PaginationInterceptor（未自定义时自动分页）");
        return new PaginationInterceptor();
    }

}
