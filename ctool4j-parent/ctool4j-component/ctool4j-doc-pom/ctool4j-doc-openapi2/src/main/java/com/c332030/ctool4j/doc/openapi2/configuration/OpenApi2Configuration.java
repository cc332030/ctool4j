package com.c332030.ctool4j.doc.openapi2.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;

/**
 * <p>
 * Description: OpenApi2Configuration
 * </p>
 *
 * @since 2025/12/16
 */
@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class OpenApi2Configuration {

}
