package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.doc.openapi2.util.CTextEnumUtils;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: CTextEnumModelPropertyPlugin：model 属性枚举展示 text 增强。
 * 对实现 {@code ICText} 的枚举字段，将允许值列表由「枚举名」改为「枚举名(text)」（如 AUTHORIZATION(鉴权)），
 * 使接口文档枚举值可读；非 ICText 枚举或非枚举属性不受影响
 * </p>
 *
 * <p>
 * 仅改文档展示（allowableValues），不影响运行时传值（仍用枚举名）。
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
            if (CTextEnumUtils.isTextEnum(enumType)) {
                val allowableValues = CTextEnumUtils.textEnumAllowableValues(enumType);
                if (null != allowableValues) {
                    context.getBuilder().allowableValues(allowableValues);
                }
            }
        });
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

}
