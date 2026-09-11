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
 * <h2>影响范围</h2>
 * <ul>
 *   <li>仅影响 model 属性文档展示；非 ICText 枚举 / 非枚举属性不受影响。</li>
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
 *     <td>已标 @CSchema(value) 自定义描述</td>
 *     <td>不写 text 说明（description 由 @CSchema 提供）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口文档 model 字段为实现 ICText 的枚举，希望允许值可直接使用、text 说明可读。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>允许值保持可提交的枚举名（与运行时一致）；text 可读性通过 description 提供，两者兼顾。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>处理时机</b></p>
 * <ul>
 *   <li>经 {@code context.getBeanPropertyDefinition().getRawPrimaryType()} 获取字段类型。</li>
 *   <li>由 {@code CTextEnumUtils.enumAllowableValues} 生成可提交允许值并覆写 builder.allowableValues；</li>
 *   <li>由 {@code CTextEnumUtils.textEnumDescription} 生成可读说明写入 description。</li>
 * </ul>
 * <p><b>描述防覆盖</b></p>
 * <ul>
 *   <li>若属性已标注 {@code @CSchema} 且 {@code value} 非空（提供自定义描述），则不写入 text 说明，避免覆盖既有描述。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/6
 * @version 1.0
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTextEnumModelPropertyPlugin implements ModelPropertyBuilderPlugin {

    /**
     * 为 text 枚举类型的属性写入允许值列表与 text 可读描述。
     *
     * <p>非 text 枚举类型（含无属性定义）直接跳过；允许值恒为可提交的「枚举名」列表；描述仅在属性未通过
     * {@code @CSchema(value)} 自定义说明时写入，避免覆盖既有描述。</p>
     *
     * @param context Model 属性构建上下文，用于取属性类型并写入允许值/描述
     */
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
