package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * <p>
 * Description: ICExpandedParameterBuilderPlugin
 * </p>
 *
 * @since 2025/12/17
 */
public interface ICAnnotationExpandedParameterBuilderPlugin<T extends Annotation> extends ICExpandedParameterBuilderPlugin {

    @Override
    default void apply(ParameterExpansionContext context) {

        Optional<T> annotationOpt = context.findAnnotation(getAnnotationClass());
        if (annotationOpt.isPresent()) {
            context.getParameterBuilder().required(true);
        }
    }

    Class<T> getAnnotationClass();

}
