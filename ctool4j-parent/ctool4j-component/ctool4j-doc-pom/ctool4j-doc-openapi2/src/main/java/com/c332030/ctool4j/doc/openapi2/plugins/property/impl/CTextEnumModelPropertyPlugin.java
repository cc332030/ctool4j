package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.doc.openapi2.util.CTextEnumUtils;
import lombok.val;
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
 * Description: CTextEnumModelPropertyPlugin：model 属性枚举 text 展示增强。
 * 对实现 {@code ICText} 的枚举字段，允许值保持可提交的「枚举名」（如 AUTHORIZATION），
 * 并将可读的「枚举名(text)」（如 AUTHORIZATION(鉴权)）写入属性描述，使接口文档枚举值可读；
 * 非 ICText 枚举或非枚举属性不受影响
 * </p>
 *
 * <p>
 * 仅改文档展示，不影响运行时传值（允许值仍为可提交的枚举名）。
 * 若属性已标注 {@code @CSchema} 且提供描述（value），则不覆盖该描述，text 说明跳过。
 * </p>
 *
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumModelPropertyPlugin.adoc"
 * @since 2026/9/6
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTextEnumModelPropertyPlugin implements ModelPropertyBuilderPlugin {

    @Override
    public void apply(@NonNull ModelPropertyContext context) {

        context.getBeanPropertyDefinition().ifPresent(beanProperty -> {
            val enumType = beanProperty.getRawPrimaryType();
            if (!CTextEnumUtils.isTextEnum(enumType)) {
                return;
            }

            // 允许值：可提交的「枚举名」列表（运行时按枚举名传值）
            val allowableValues = CTextEnumUtils.enumAllowableValues(enumType);
            if (null != allowableValues) {
                context.getBuilder().allowableValues(allowableValues);
            }

            // 描述：仅在该属性未标注 @CSchema(value) 自定义说明时写入 text 可读说明，避免覆盖既有描述
            if (!hasCSchemaDescription(context)) {
                val description = CTextEnumUtils.textEnumDescription(enumType);
                if (null != description) {
                    context.getBuilder().description(description);
                }
            }
        });
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

    /**
     * 判断属性是否标注了带自定义描述的 {@link CSchema} 注解（从 annotatedElement 或 beanPropertyDefinition 查找）
     *
     * @param context Model 属性上下文
     * @return 是否存在自定义描述
     */
    private static boolean hasCSchemaDescription(ModelPropertyContext context) {

        Optional<CSchema> schemaAnnotation = empty();

        if (context.getAnnotatedElement().isPresent()) {
            schemaAnnotation = Optional.ofNullable(
                context.getAnnotatedElement().get().getAnnotation(CSchema.class));
        }
        if (!schemaAnnotation.isPresent() && context.getBeanPropertyDefinition().isPresent()) {
            schemaAnnotation = findPropertyAnnotation(
                context.getBeanPropertyDefinition().get(), CSchema.class);
        }

        return schemaAnnotation.isPresent() && StringUtils.hasText(schemaAnnotation.get().value());
    }
}
