package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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

}
