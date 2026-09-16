package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.doc.annotation.CParameter;
import com.c332030.ctool4j.doc.openapi2.util.CTextEnumUtils;
import io.swagger.annotations.ApiParam;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
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
 * 若参数已自带描述（{@code @CParameter.value}，或存量的 {@code @ApiParam.value}），则不覆盖该描述，text 说明跳过。
 * </p>
 *
 * <h2>影响范围</h2>
 * <ul>
 *   <li>仅影响参数文档展示（query/header 等枚举参数）；非 ICText 枚举 / 非枚举参数不受影响。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>非 ICText 枚举</td>
 *     <td>不覆写（保持 springfox 默认「枚举名」列表）</td>
 *   </tr>
 *   <tr>
 *     <td>enumAllowableValues 返回 null</td>
 *     <td>不覆写</td>
 *   </tr>
 *   <tr>
 *     <td>已自带描述（{@code @CParameter(value)} 或存量 {@code @ApiParam(value)}）</td>
 *     <td>不写 text 说明（描述由该注解提供）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口请求参数为实现 ICText 的枚举，希望文档允许值可直接使用、text 说明可读。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>允许值保持可提交的枚举名（与运行时一致）；text 可读性通过 description 提供，两者兼顾。</li>
 *   <li>描述防覆盖只识别注解声明的描述（{@code @CParameter}/{@code @ApiParam}）：springfox 由类型或校验注解推导出的
 *   描述不在判定范围内，写法上避免与推导来源混用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>处理时机</b></p>
 * <ul>
 *   <li>经 {@code context.resolvedMethodParameter().getParameterType().getErasedType()} 获取参数类型。</li>
 *   <li>由 {@code CTextEnumUtils.enumAllowableValues} 生成可提交允许值并覆写 parameterBuilder.allowableValues；</li>
 *   <li>由 {@code CTextEnumUtils.textEnumDescription} 生成可读说明写入 description。</li>
 * </ul>
 * <p><b>描述防覆盖</b></p>
 * <ul>
 *   <li>参数已自带描述（{@code @CParameter.value} 或存量 {@code @ApiParam.value} 非空）时不写入 text 说明，
 *   避免覆盖业务声明的描述（存量 Swagger 注解与 {@code @Api} 兼容口径一致）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/6
 * @version 1.1
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTextEnumParameterPlugin implements ParameterBuilderPlugin {

    /**
     * 为 text 枚举类型的参数写入允许值列表与 text 可读描述。
     *
     * <p>非 text 枚举类型直接跳过；允许值恒为可提交的「枚举名」列表；描述仅在参数未自带描述
     * （{@code @CParameter.value} 或存量 {@code @ApiParam.value}）时写入，避免覆盖既有描述。</p>
     *
     * @param context parameter 构建上下文，用于取参数类型并写入允许值/描述
     */
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
        if (CValidUtils.isValid(allowableValues)) {
            parameterBuilder.allowableValues(allowableValues);
        }

        // 描述：仅在参数未自带描述（@CParameter.value / 存量 @ApiParam.value）时写入 text 可读说明，避免覆盖既有描述
        val hasCParameterDescription = resolvedMethodParameter.findAnnotation(CParameter.class)
            .map(CParameter::value)
            .map(value -> CValidUtils.isValid(value))
            .orElse(false);
        val hasApiParamDescription = resolvedMethodParameter.findAnnotation(ApiParam.class)
            .map(ApiParam::value)
            .map(value -> CValidUtils.isValid(value))
            .orElse(false);

        if (!hasCParameterDescription && !hasApiParamDescription) {
            val description = CTextEnumUtils.textEnumDescription(parameterType);
            if (CValidUtils.isValid(description)) {
                parameterBuilder.description(description);
            }
        }
    }

    /**
     * 是否支持该文档类型。
     *
     * @param delimiter 文档类型（本插件不区分类型）
     * @return 恒为 true，对所有文档类型生效
     */
    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

}
