package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import lombok.val;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import java.lang.annotation.Annotation;

/**
 * <p>
 * Description: ICAnnotationExpandedParameterBuilderPlugin
 * </p>
 *
 * @see "doc/design/openapi2/ICAnnotationExpandedParameterBuilderPlugin.adoc"
 * @since 2025/12/17
 */
public interface ICAnnotationExpandedParameterBuilderPlugin<T extends Annotation> extends ICExpandedParameterBuilderPlugin {

    /**
     * 处理参数展开上下文（存在目标注解且 {@link #isRequired} 为 true 时将参数标记为必填）
     * @param context 参数展开上下文
     */
    @Override
    default void apply(ParameterExpansionContext context) {

        val annotationOpt = context.findAnnotation(getAnnotationClass());
        if (annotationOpt.isPresent() && isRequired(annotationOpt.get())) {
            context.getParameterBuilder().required(true);
        }
    }

    /**
     * 注解命中时是否标记为必填（默认 true：无 required 开关的注解如 {@code @NotEmpty}、{@code @CRequired} 命中即必填；
     * 需按注解属性定制判定时可覆写本方法）
     *
     * @param annotation 命中的注解
     * @return 是否必填
     */
    default boolean isRequired(Annotation annotation) {
        return true;
    }

    /**
     * 获取目标注解类型
     * @return 目标注解类型
     */
    Class<T> getAnnotationClass();

}
