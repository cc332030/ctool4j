package com.c332030.ctool4j.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: MybatisPlusConfiguration
 * </p>
 *
 * @since 2025/12/29
 */
@Configuration
public class MybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean(PaginationInterceptor.class)
    public PaginationInterceptor mybatisPlusInterceptor() {
        return new PaginationInterceptor();
    }

}
