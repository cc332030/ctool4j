package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.doc.openapi2.util.CTextEnumUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
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
 * 若属性已自带描述（{@code @CSchema.value}，或存量的 {@code @ApiModelProperty.value}），则不覆盖该描述，text 说明跳过。
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
 *     <td>已自带描述（{@code @CSchema(value)} 或存量 {@code @ApiModelProperty(value)}）</td>
 *     <td>不写 text 说明（描述由该注解提供）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口文档 model 字段为实现 ICText 的枚举，希望允许值可直接使用、text 说明可读。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>允许值保持可提交的枚举名（与运行时一致）；text 可读性通过 description 提供，两者兼顾。</li>
 *   <li>描述防覆盖只识别注解声明的描述（{@code @CSchema}/{@code @ApiModelProperty}）：springfox 由类型推导出的描述
 *   不在判定范围内，写法上避免与推导来源混用。</li>
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
 *   <li>属性已自带描述（{@code @CSchema.value} 或存量 {@code @ApiModelProperty.value} 非空）时不写入 text 说明，
 *   避免覆盖业务声明的描述（存量 Swagger 注解与 {@code @Api} 兼容口径一致）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/6
 * @version 1.1
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTextEnumModelPropertyPlugin implements ModelPropertyBuilderPlugin {

    /**
     * 为 text 枚举类型的属性写入允许值列表与 text 可读描述。
     *
     * <p>非 text 枚举类型（含无属性定义）直接跳过；允许值恒为可提交的「枚举名」列表；描述仅在属性未自带描述
     * （{@code @CSchema.value} 或存量 {@code @ApiModelProperty.value}）时写入，避免覆盖既有描述。</p>
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
            if (CValidUtils.isValid(allowableValues)) {
                context.getBuilder().allowableValues(allowableValues);
            }

            // 描述：仅在该属性未自带描述时写入 text 可读说明，避免覆盖既有描述
            if (!hasCustomDescription(context)) {
                val description = CTextEnumUtils.textEnumDescription(enumType);
                if (CValidUtils.isValid(description)) {
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
     * 属性是否自带描述：{@code @CSchema.value} 或存量 {@code @ApiModelProperty.value} 非空
     *
     * <p>注解优先从 {@code annotatedElement} 取，其次从 {@code beanPropertyDefinition} 查找。</p>
     *
     * @param context Model 属性上下文
     * @return true 表示自带描述（不应被 text 说明覆盖）
     */
    private static boolean hasCustomDescription(ModelPropertyContext context) {

        val schemaDescription = findAnnotation(context, CSchema.class)
            .map(CSchema::value)
            .orElse(null);
        if (CValidUtils.isValid(schemaDescription)) {
            return true;
        }

        val apiModelPropertyDescription = findAnnotation(context, ApiModelProperty.class)
            .map(ApiModelProperty::value)
            .orElse(null);
        return CValidUtils.isValid(apiModelPropertyDescription);
    }

    /**
     * 从 annotatedElement 或 beanPropertyDefinition 上查找指定注解
     *
     * @param context        Model 属性上下文
     * @param annotationType 注解类型
     * @param <T>            注解类型泛型
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
        if (!annotation.isPresent() && context.getBeanPropertyDefinition().isPresent()) {
            annotation = findPropertyAnnotation(context.getBeanPropertyDefinition().get(), annotationType);
        }

        return annotation;
    }
}
