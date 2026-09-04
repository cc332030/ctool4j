package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.web.doc.annotation.CSchema;
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
 * 在 required=true 时将属性标记为必填，并在 value 非空时写入字段描述
 * （替代 {@code @ApiModelProperty + @NotNull} 两个注解）
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

        Optional<CSchema> annotation = empty();

        if (context.getAnnotatedElement().isPresent()) {
            annotation = Optional.ofNullable(
                context.getAnnotatedElement().get().getAnnotation(CSchema.class));
        }
        if (context.getBeanPropertyDefinition().isPresent()) {
            annotation = annotation.isPresent() ? annotation : findPropertyAnnotation(
                context.getBeanPropertyDefinition().get(), CSchema.class);
        }

        annotation.ifPresent(cSchema -> {
            // required=true 时才标记必填
            if (cSchema.required()) {
                context.getBuilder().required(true);
            }
            // value 非空时写入描述，为空则保留已有描述（避免空串覆盖）
            if (StringUtils.hasText(cSchema.value())) {
                context.getBuilder().description(cSchema.value());
            }
        });
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        // 与项目其余插件约定一致：默认支持（null 也视为支持）
        return true;
    }
}
