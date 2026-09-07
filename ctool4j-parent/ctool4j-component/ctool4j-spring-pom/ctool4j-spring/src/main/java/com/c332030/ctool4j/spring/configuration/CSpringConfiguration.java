package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.spring.util.CRestTemplateUtils;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * Description: CSpringConfiguration
 * </p>
 *
 * @since 2025/9/11
 * @see "doc/design/spring/CSpringConfiguration.adoc"
 */
@CustomLog
@Configuration
@ComponentScan(CTool4jConstants.BASE_PACKAGE)
@ConfigurationPropertiesScan(CTool4jConstants.BASE_PACKAGE)
public class CSpringConfiguration {

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
