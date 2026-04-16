package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.spring.util.CRestTemplateUtils;
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
 */
@Configuration
@ComponentScan(CTool4jConstants.BASE_PACKAGE)
@ConfigurationPropertiesScan(CTool4jConstants.BASE_PACKAGE)
public class CSpringConfiguration {

    @Lazy
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate cRestTemplate() {
        return CRestTemplateUtils.REST_TEMPLATE;
    }

}
