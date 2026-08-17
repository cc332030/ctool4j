package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import lombok.val;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import java.lang.annotation.Annotation;

/**
 * <p>
 * Description: ICAnnotationExpandedParameterBuilderPlugin
 * </p>
 *
 * @since 2025/12/17
 */
public interface ICAnnotationExpandedParameterBuilderPlugin<T extends Annotation> extends ICExpandedParameterBuilderPlugin {

    /**
     * 处理参数展开上下文（存在目标注解时将参数标记为必填）
     * @param context 参数展开上下文
     */
    @Override
    default void apply(ParameterExpansionContext context) {

        val annotationOpt = context.findAnnotation(getAnnotationClass());
        if (annotationOpt.isPresent()) {
            context.getParameterBuilder().required(true);
        }
    }

    /**
     * 获取目标注解类型
     * @return 目标注解类型
     */
    Class<T> getAnnotationClass();

}
