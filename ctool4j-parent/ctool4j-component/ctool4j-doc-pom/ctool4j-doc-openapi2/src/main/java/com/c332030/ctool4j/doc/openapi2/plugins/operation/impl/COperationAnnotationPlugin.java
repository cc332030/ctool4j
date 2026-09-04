package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.web.validation.annotation.COperation;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: COperationAnnotationPlugin：识别方法上的 @COperation 注解，
 * 将 summary/description/operationId/deprecated 写入 springfox 的 operation
 * （替代原生 {@code @ApiOperation}；分组 tag 由类级 @CTag 的 CTagAnnotationPlugin 统一处理）
 * </p>
 *
 * @see "doc/design/openapi2/COperationAnnotationPlugin.adoc"
 * @see "doc/design/openapi2/COperationAnnotationPluginTests.adoc"
 * @author c332030
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class COperationAnnotationPlugin implements OperationBuilderPlugin {

    @Override
    public void apply(@NonNull OperationContext context) {

        val annotationOpt = context.findAnnotation(COperation.class);
        annotationOpt.ifPresent(cOperation -> {
            val operationBuilder = context.operationBuilder();

            if (hasText(cOperation.value())) {
                operationBuilder.summary(cOperation.value());
            }
            if (hasText(cOperation.description())) {
                operationBuilder.notes(cOperation.description());
            }
            if (cOperation.deprecated()) {
                operationBuilder.deprecated(Boolean.TRUE.toString());
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
