package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.util.CRestTemplateUtils;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * Description: CSpringConfiguration
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringConfiguration}：Spring 基础设施装配入口。</p>
 * <ul>
 *   <li>组件扫描：{@code @ComponentScan} 覆盖 {@code com.c332030.ctool4j} 基础包。</li>
 *   <li>属性扫描：{@code @ConfigurationPropertiesScan} 同范围。</li>
 *   <li>共享 {@code RestTemplate}：{@code cRestTemplate}（未自定义时提供懒加载实例）。</li>
 *   <li>上下文持有：把 {@code ApplicationContext} 写入 {@code CSpringConfigBeans}，供静态工具类读取。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>上下文写入放在 {@code Lifecycle} 的 {@code onRefresh} 阶段而非 {@code InitializingBean}：
 *   扫描类工具的调用点可能早于本配置类实例化，刷新阶段写入能让「上下文何时可用」覆盖整个 Bean 实例化期。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>{@code RestTemplate} 仅在容器内不存在同类型 Bean 时提供（{@code @ConditionalOnMissingBean}）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>由使用方显式引入（或经 {@code spring.factories} 自动配置）以启用框架能力。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需 Spring 容器；纯 Java 环境不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>扫描范围固定在 {@code com.c332030.ctool4j} 基础包，业务方包需自行扫描。</li>
 * </ul>
 *
 * @since 2025/9/11
 * @version 1.0
 */
@CustomLog
@Configuration
@ComponentScan(CTool4jConstants.BASE_PACKAGE)
@ConfigurationPropertiesScan(CTool4jConstants.BASE_PACKAGE)
public class CSpringConfiguration implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CSpringConfigBeans.setApplicationContext(applicationContext);
    }

    /**
     * 创建懒加载的 RestTemplate
     *
     * @return 共享的 RestTemplate 实例
     */
    @Lazy
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate cRestTemplate() {
        log.debug("默认装配共享懒加载 RestTemplate（未自定义 RestTemplate Bean 时提供）");
        return CRestTemplateUtils.REST_TEMPLATE;
    }

}
