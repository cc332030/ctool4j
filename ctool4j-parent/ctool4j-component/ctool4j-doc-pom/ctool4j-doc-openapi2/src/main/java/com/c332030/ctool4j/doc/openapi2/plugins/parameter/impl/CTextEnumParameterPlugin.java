package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.annotation.CParameter;
import com.c332030.ctool4j.doc.openapi2.util.CTextEnumUtils;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: CTextEnumParameterPlugin：请求参数枚举 text 展示增强。
 * 对实现 {@code ICText} 的枚举参数，允许值保持可提交的「枚举名」（如 AUTHORIZATION），
 * 并将可读的「枚举名(text)」（如 AUTHORIZATION(鉴权)）写入参数描述，使接口文档枚举值可读；
 * 非 ICText 枚举或非枚举参数不受影响
 * </p>
 *
 * <p>
 * 仅改文档展示，不影响运行时传值（允许值仍为可提交的枚举名）。
 * 若参数已标注 {@code @CParameter} 且提供说明（description），则不覆盖该描述，text 说明跳过。
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

        val resolvedMethodParameter = context.resolvedMethodParameter();
        val parameterType = resolvedMethodParameter
            .getParameterType()
            .getErasedType();

        if (!CTextEnumUtils.isTextEnum(parameterType)) {
            return;
        }

        val parameterBuilder = context.parameterBuilder();

        // 允许值：可提交的「枚举名」列表（运行时按枚举名传值）
        val allowableValues = CTextEnumUtils.enumAllowableValues(parameterType);
        if (null != allowableValues) {
            parameterBuilder.allowableValues(allowableValues);
        }

        // 描述：仅在参数未标注 @CParameter(value) 自定义说明时写入 text 可读说明，避免覆盖既有描述
        val hasCustomDescription = resolvedMethodParameter.findAnnotation(CParameter.class)
            .map(cParameter -> StringUtils.hasText(cParameter.value()))
            .orElse(false);
        if (!hasCustomDescription) {
            val description = CTextEnumUtils.textEnumDescription(parameterType);
            if (null != description) {
                parameterBuilder.description(description);
            }
        }
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

}
