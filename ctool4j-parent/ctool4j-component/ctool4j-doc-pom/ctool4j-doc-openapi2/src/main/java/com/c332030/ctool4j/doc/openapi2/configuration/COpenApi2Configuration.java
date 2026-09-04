package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.doc.openapi2.config.CDocOpenApi2Config;
import com.c332030.ctool4j.doc.openapi2.plugins.operation.impl.COperationAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.operation.impl.CTagAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CNotEmptyAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CParameterAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CSchemaAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.property.impl.CSchemaAnnotationModelPropertyPlugin;
import com.c332030.ctool4j.doc.openapi2.util.CSpringFoxUtils;
import com.c332030.ctool4j.web.doc.annotation.CTag;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: COpenApi2Configuration
 * </p>
 *
 * <p>
 * 本模块基于 springfox（OpenAPI2/Swagger 注解形态）实现。springfox 已停止维护，Knife4j 4.x 建议迁移 OpenAPI3
 * （springdoc-openapi + Knife4j 4.x），保留此实现是为兼容老项目对 OpenAPI2 的依赖，不重复造轮子，
 * 新项目建议直接使用 OpenAPI3；迁移属大工程需排期
 * </p>
 *
 * @see "doc/design/openapi2/COpenApi2Configuration.adoc"
 * @since 2025/12/16
 */
@CustomLog
@Configuration
@Import(value = {
    BeanValidatorPluginsConfiguration.class
})
public class COpenApi2Configuration {

    /**
     * 非空注解插件
     *
     * @return 插件
     */
    @Bean
    public CNotEmptyAnnotationPlugin cExpanderNotEmpty() {
        return new CNotEmptyAnnotationPlugin();
    }

    /**
     * 必填注解插件（@CSchema，方法参数）
     *
     * @return 插件
     */
    @Bean
    public CSchemaAnnotationPlugin cExpanderCSchema() {
        return new CSchemaAnnotationPlugin();
    }

    /**
     * 必填注解 model 属性插件（@CSchema，字段/getter 描述与必填标记）
     *
     * @return 插件
     */
    @Bean
    public CSchemaAnnotationModelPropertyPlugin cModelPropertyCSchema() {
        return new CSchemaAnnotationModelPropertyPlugin();
    }

    /**
     * 操作注解插件（@COperation，方法摘要/说明/operationId）
     *
     * @return 插件
     */
    @Bean
    public COperationAnnotationPlugin cOperationCOperation() {
        return new COperationAnnotationPlugin();
    }

    /**
     * 分组注解插件（@CTag，类级分组 tag）
     *
     * @return 插件
     */
    @Bean
    public CTagAnnotationPlugin cOperationCTag() {
        return new CTagAnnotationPlugin();
    }

    /**
     * 参数注解插件（@CParameter，方法参数 name/description/required/example）
     *
     * @return 插件
     */
    @Bean
    public CParameterAnnotationPlugin cParameterCParameter() {
        return new CParameterAnnotationPlugin();
    }

    /**
     * Swagger Docket（收集标注 {@code @CTag} 注解的接口，替代原生 {@code @Api}）
     *
     * @param config 配置
     * @return Docket
     */
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
            .apis(RequestHandlerSelectors.withClassAnnotation(CTag.class))
            .build()
            ;
    }

    /**
     * 避免 springfox 报空指针
     *
     * @return BeanPostProcessor
     */
    @Bean
    public static BeanPostProcessor cSpringfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            /**
             * 初始化后处理：修复 springfox 的 handlerMappings 空指针问题
             *
             * @param bean     后置处理对象
             * @param beanName Bean 名称
             * @return 处理后的对象
             */
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    try {
                        customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                    } catch (Throwable ex) {
                        log.debug("could not customize springfox handler mappings", ex);
                    }
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                val copy = mappings.stream()
                    .filter(mapping -> mapping.getPatternParser() == null)
                    .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            @SneakyThrows
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                val field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                if (null == field) {
                    return Collections.emptyList();
                }
                field.setAccessible(true);
                return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
            }
        };
    }

}
