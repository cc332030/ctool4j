package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.doc.openapi2.config.CDocOpenApi2Config;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CNotEmptyAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.util.CSpringFoxUtils;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import io.swagger.annotations.Api;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: COpenApi2Configuration
 * </p>
 *
 * @since 2025/12/16
 */
@Configuration
@Import(value = {
    BeanValidatorPluginsConfiguration.class
})
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

    /**
     * 避免 springfox 报空指针
     */
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                    .filter(mapping -> mapping.getPatternParser() == null)
                    .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            @SneakyThrows
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                if (null == field) {
                    return Collections.emptyList();
                }
                field.setAccessible(true);
                return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
            }
        };
    }

}
