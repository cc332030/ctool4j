package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.annotation.CParameter;
import com.c332030.ctool4j.web.validation.annotation.CNotRequired;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: CParameterAnnotationPlugin：识别方法参数上的 @CParameter 注解，
 * 将 name/description/required/example 写入 springfox 的 operation 参数
 * （替代原生 {@code @ApiParam}）
 * </p>
 *
 * <p>
 * 必填语义：标注 {@code @CNotRequired} 即文档非必填（独立生效，无需同时标注 @CParameter）；
 * 标注 {@code @CParameter} 且未标 {@code @CNotRequired} 时默认必填（与 {@code @RequestParam} 默认一致）。
 * </p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>name/description/example 为空</td>
 *     <td>不覆盖</td>
 *   </tr>
 *   <tr>
 *     <td>未标 @CNotRequired 且标注 @CParameter</td>
 *     <td>标记必填（默认，与 SpringMVC 绑定一致）</td>
 *   </tr>
 *   <tr>
 *     <td>标注 @CNotRequired（含无 @CParameter）</td>
 *     <td>标记非必填（独立生效）</td>
 *   </tr>
 *   <tr>
 *     <td>无 @CParameter 也无 @CNotRequired</td>
 *     <td>不处理</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>用 {@code @CParameter} 标注方法参数后，springfox 文档展示参数名称/说明/必填/示例。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code @CParameter}（ctool4j-definition 模块）与 {@code @CNotRequired}（web 模块）注解定义。</li>
 *   <li>{@code @CNotRequired} 与 {@code @RequestParam} 同时标注时，文档非必填语义由 {@code @RequestParam} 覆盖（绑定以 @RequestParam 为准）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注解读取</b></p>
 * <ul>
 *   <li>用 {@code context.resolvedMethodParameter().findAnnotation(CParameter.class)} 取参数级注解。</li>
 * </ul>
 * <p><b>处理</b></p>
 * <ul>
 *   <li>{@code @CParameter.value} 非空 → {@code parameterBuilder.description(...)}。</li>
 *   <li>name 非空 → {@code parameterBuilder.name(...)}。</li>
 *   <li>必填判定：标注 {@code @CNotRequired} → {@code required(false)}（非必填，独立生效，无需同时标注 @CParameter）；</li>
 *   <li>否则标注 {@code @CParameter} → {@code required(true)}（默认必填，与 {@code @RequestParam} 一致）。</li>
 *   <li>example 非空 → {@code parameterBuilder.scalarExample(...)}。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CParameterAnnotationPlugin implements ParameterBuilderPlugin {

    /**
     * 将 {@code @CParameter} 的 value/required 写入参数描述与必填标记。
     *
     * <p>注解缺失时不做任何修改；value 为空白时描述不写入，必填只在 required 为 true 时置为必填
     * （不覆盖对方已有的 true）。</p>
     *
     * @param context parameter 构建上下文，用于查找注解并写入字段
     */
    @Override
    public void apply(@NonNull ParameterContext context) {

        val resolvedMethodParameter = context.resolvedMethodParameter();

        // 非必填标记独立生效：标注 @CNotRequired 即文档非必填（无需同时标注 @CParameter）
        val notRequired = resolvedMethodParameter.hasParameterAnnotation(CNotRequired.class);

        val annotationOpt = resolvedMethodParameter.findAnnotation(CParameter.class);
        annotationOpt.ifPresent(cParameter -> {
            val parameterBuilder = context.parameterBuilder();

            if (hasText(cParameter.value())) {
                parameterBuilder.description(cParameter.value());
            }
            if (hasText(cParameter.name())) {
                parameterBuilder.name(cParameter.name());
            }
            if (hasText(cParameter.example())) {
                parameterBuilder.scalarExample(cParameter.example());
            }
        });

        if (notRequired) {
            context.parameterBuilder().required(false);
        } else if (annotationOpt.isPresent()) {
            // 标注 @CParameter 且未标 @CNotRequired → 默认必填（与 @RequestParam 默认一致）
            context.parameterBuilder().required(true);
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

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
