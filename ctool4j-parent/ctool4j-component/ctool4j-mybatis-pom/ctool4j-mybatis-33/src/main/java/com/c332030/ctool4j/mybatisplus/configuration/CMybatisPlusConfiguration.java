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
 * <h2>设计要点</h2>
 * <ul>
 *   <li>注册 SQL 注入器、配置 MyBatis-Plus 行为</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>MyBatis-Plus 配置</p>
 * <h2>不适用与边界场景</h2>
 * <p>@Configuration</p>
 * <h2>已知限制与取舍</h2>
 * <p>@Configuration</p>
 *
 * @since 2025/12/29
 * @version 1.0
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
