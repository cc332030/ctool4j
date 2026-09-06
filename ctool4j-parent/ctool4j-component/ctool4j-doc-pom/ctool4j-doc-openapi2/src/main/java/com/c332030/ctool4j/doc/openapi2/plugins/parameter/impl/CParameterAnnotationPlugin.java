package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.annotation.CParameter;
import com.c332030.ctool4j.web.validation.annotation.CNotRequired;
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
 * <p>
 * 必填语义：标注 {@code @CNotRequired} 即文档非必填（独立生效，无需同时标注 @CParameter）；
 * 标注 {@code @CParameter} 且未标 {@code @CNotRequired} 时默认必填（与 {@code @RequestParam} 默认一致）。
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

        val resolvedMethodParameter = context.resolvedMethodParameter();

        // 非必填标记独立生效：标注 @CNotRequired 即文档非必填（无需同时标注 @CParameter）
        val notRequired = resolvedMethodParameter.hasParameterAnnotation(CNotRequired.class);

        val annotationOpt = resolvedMethodParameter.findAnnotation(CParameter.class);
        annotationOpt.ifPresent(cParameter -> {
            val parameterBuilder = context.parameterBuilder();

            if (hasText(cParameter.value())) {
                parameterBuilder.description(cParameter.value());
            }
            if (hasText(cParameter.name())) {
                parameterBuilder.name(cParameter.name());
            }
            if (hasText(cParameter.example())) {
                parameterBuilder.scalarExample(cParameter.example());
            }
        });

        if (notRequired) {
            context.parameterBuilder().required(false);
        } else if (annotationOpt.isPresent()) {
            // 标注 @CParameter 且未标 @CNotRequired → 默认必填（与 @RequestParam 默认一致）
            context.parameterBuilder().required(true);
        }
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
