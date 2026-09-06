package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.web.doc.annotation.CSchema;
import com.c332030.ctool4j.web.validation.annotation.CRequired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Optional;

import static java.util.Optional.empty;
import static springfox.documentation.schema.Annotations.findPropertyAnnotation;

/**
 * <p>
 * Description: CSchemaAnnotationModelPropertyPlugin：识别 model 字段/getter 上的 @CSchema 注解，
 * 在 value 非空时写入字段描述（description）；必填由同一元素上的 @CRequired 注解驱动（标注即必填）
 * </p>
 *
 * <p>
 * 描述与必填解耦：@CSchema 负责文档描述（对齐 @ApiModelProperty/@Schema），@CRequired 负责必填。
 * </p>
 *
 * @see "doc/design/openapi2/CSchemaAnnotationModelPropertyPlugin.adoc"
 * @see "doc/design/openapi2/CSchemaAnnotationModelPropertyPluginTests.adoc"
 * @author c332030
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CSchemaAnnotationModelPropertyPlugin implements ModelPropertyBuilderPlugin {

    @Override
    public void apply(@NonNull ModelPropertyContext context) {

        Optional<CSchema> schemaAnnotation = findAnnotation(context, CSchema.class);
        schemaAnnotation.ifPresent(cSchema -> {
            // value 非空时写入描述，为空则保留已有描述（避免空串覆盖）
            if (StringUtils.hasText(cSchema.value())) {
                context.getBuilder().description(cSchema.value());
            }
        });

        // 必填：标注 @CRequired 即必填
        if (findAnnotation(context, CRequired.class).isPresent()) {
            context.getBuilder().required(true);
        }
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        // 与项目其余插件约定一致：默认支持（null 也视为支持）
        return true;
    }

    /**
     * 从 annotatedElement 或 beanPropertyDefinition 上查找指定注解
     *
     * @param context       Model 属性上下文
     * @param annotationType 注解类型
     * @param <T>           注解类型泛型
     * @return 注解
     */
    private static <T extends java.lang.annotation.Annotation> Optional<T> findAnnotation(
        ModelPropertyContext context, Class<T> annotationType
    ) {

        Optional<T> annotation = empty();

        if (context.getAnnotatedElement().isPresent()) {
            annotation = Optional.ofNullable(
                context.getAnnotatedElement().get().getAnnotation(annotationType));
        }
        if (context.getBeanPropertyDefinition().isPresent()) {
            annotation = annotation.isPresent() ? annotation : findPropertyAnnotation(
                context.getBeanPropertyDefinition().get(), annotationType);
        }

        return annotation;
    }
}
