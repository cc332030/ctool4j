package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.web.validation.annotation.CParameter;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: CParameterAnnotationPlugin：识别方法参数上的 @CParameter 注解，
 * 将 name/description/required/example 写入 springfox 的 operation 参数
 * （替代原生 {@code @ApiParam}）
 * </p>
 *
 * @see "doc/design/openapi2/CParameterAnnotationPlugin.adoc"
 * @see "doc/design/openapi2/CParameterAnnotationPluginTests.adoc"
 * @author c332030
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CParameterAnnotationPlugin implements ParameterBuilderPlugin {

    @Override
    public void apply(@NonNull ParameterContext context) {

        val annotationOpt = context.resolvedMethodParameter().findAnnotation(CParameter.class);
        annotationOpt.ifPresent(cParameter -> {
            val parameterBuilder = context.parameterBuilder();

            if (hasText(cParameter.value())) {
                parameterBuilder.description(cParameter.value());
            }
            if (hasText(cParameter.name())) {
                parameterBuilder.name(cParameter.name());
            }
            if (cParameter.required()) {
                parameterBuilder.required(true);
            }
            if (hasText(cParameter.example())) {
                parameterBuilder.scalarExample(cParameter.example());
            }
        });
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
