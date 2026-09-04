package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.web.validation.annotation.COperation;
import com.c332030.ctool4j.web.validation.annotation.CTag;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CTagAnnotationPlugin：识别类上的 @CTag 注解，将该接口的分组（tag）应用到其所有 operation，
 * 并合并方法上 @COperation.tags 指定的额外分组；由类级 @CTag 替代原生 {@code @Api} 的分组作用
 * </p>
 *
 * @see "doc/design/openapi2/CTagAnnotationPlugin.adoc"
 * @author c332030
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTagAnnotationPlugin implements OperationBuilderPlugin {

    @Override
    public void apply(@NonNull OperationContext context) {

        val classTagOpt = context.findControllerAnnotation(CTag.class);
        val methodOperationOpt = context.findAnnotation(COperation.class);

        Set<String> tags = new LinkedHashSet<>();

        classTagOpt.ifPresent(cTag -> {
            if (hasText(cTag.value())) {
                tags.add(cTag.value());
            }
        });

        methodOperationOpt.ifPresent(cOperation -> {
            if (cOperation.tags().length > 0) {
                Arrays.stream(cOperation.tags())
                    .filter(CTagAnnotationPlugin::hasText)
                    .forEach(tags::add);
            }
        });

        if (!tags.isEmpty()) {
            context.operationBuilder().tags(tags);
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
