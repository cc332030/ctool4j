package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CNotEmptyAnnotationPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;

/**
 * <p>
 * Description: COpenApi2Configuration
 * </p>
 *
 * @since 2025/12/16
 */
@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class COpenApi2Configuration {

    @Bean
    public CNotEmptyAnnotationPlugin cNotEmptyAnnotationPlugin() {
        return new CNotEmptyAnnotationPlugin();
    }

}
