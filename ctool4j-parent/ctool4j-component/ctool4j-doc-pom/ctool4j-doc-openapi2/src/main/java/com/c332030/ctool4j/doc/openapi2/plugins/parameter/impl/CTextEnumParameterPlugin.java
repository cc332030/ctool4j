package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.util.CTextEnumUtils;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: CTextEnumParameterPlugin：请求参数枚举展示 text 增强。
 * 对实现 {@code ICText} 的枚举参数，将允许值列表由「枚举名」改为「枚举名(text)」（如 AUTHORIZATION(鉴权)），
 * 使接口文档枚举值可读；非 ICText 枚举或非枚举参数不受影响
 * </p>
 *
 * <p>
 * 仅改文档展示（allowableValues），不影响运行时传值（仍用枚举名）。
 * </p>
 *
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumParameterPlugin.adoc"
 * @since 2026/9/6
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTextEnumParameterPlugin implements ParameterBuilderPlugin {

    @Override
    public void apply(@NonNull ParameterContext context) {

        val parameterType = context.resolvedMethodParameter()
            .getParameterType()
            .getErasedType();

        if (CTextEnumUtils.isTextEnum(parameterType)) {
            val allowableValues = CTextEnumUtils.textEnumAllowableValues(parameterType);
            if (null != allowableValues) {
                context.parameterBuilder().allowableValues(allowableValues);
            }
        }
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

}
