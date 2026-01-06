package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.doc.openapi2.config.CDocOpenApi2Config;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CNotEmptyAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.util.CSpringFoxUtils;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import io.swagger.annotations.Api;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spring.web.plugins.Docket;

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
    public CNotEmptyAnnotationPlugin cExpanderNotEmpty() {
        return new CNotEmptyAnnotationPlugin();
    }

    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket cDocket(CDocOpenApi2Config config) {
        return CSpringFoxUtils.getDocketBuilder()
            .groupName(null)
            .pathMapping(config.getPathMapping())
            .globalOperationParameters(CSpringFoxUtils.globalParameterList(CList.of(
                CRequestHeaderEnum.AUTHORIZATION
            )))
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            .build()
            ;
    }

}
