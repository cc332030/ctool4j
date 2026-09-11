package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.doc.annotation.CSchema;
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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code @CRequired} 标注（即必填）时标记属性必填。</li>
 *   <li>{@code @CSchema.value} 非空时写入字段描述（为空则保留已有描述）。</li>
 *   <li>描述与必填解耦：{@code @CSchema} 只管描述、{@code @CRequired} 只管必填（替代 {@code @ApiModelProperty + @NotNull}）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未标 @CRequired</td>
 *     <td>不标记必填</td>
 *   </tr>
 *   <tr>
 *     <td>@CRequired 重复标注（@CRequired.List 容器）</td>
 *     <td>仍标记必填</td>
 *   </tr>
 *   <tr>
 *     <td>@CSchema.value 为空</td>
 *     <td>不写描述（保留已有）</td>
 *   </tr>
 *   <tr>
 *     <td>均未标注</td>
 *     <td>不处理</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>springfox 文档生成时用 {@code @CSchema}（描述）+ {@code @CRequired}（必填）标注 model 属性。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code @CSchema}（ctool4j-definition 模块）与 {@code @CRequired}（web 模块）注解定义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注解查找</b></p>
 * <ul>
 *   <li>优先从 {@code context.getAnnotatedElement()} 取注解，其次从 {@code getBeanPropertyDefinition()} 查找。</li>
 * </ul>
 * <p><b>处理</b></p>
 * <ul>
 *   <li>命中 {@code @CRequired}：{@code required(true)}；兼容 {@code @Repeatable} 重复标注（多 groups 时注解存于容器</li>
 *   <li>{@code @CRequired.List}，同样命中必填）。</li>
 *   <li>{@code @CSchema.value} 有内容时 {@code description(value)}；为空时保留已有描述，避免空串覆盖。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CSchemaAnnotationModelPropertyPlugin implements ModelPropertyBuilderPlugin {

    /**
     * 将 {@code @CSchema} 的 value 写入属性描述，并按 {@code @CRequired}（含 {@code @Repeatable} 容器
     * {@code CRequired.List}）标记属性必填。
     *
     * <p>value 为空白时保留既有描述（避免空串覆盖）；未标注 {@code @CRequired} 时不改变必填状态。</p>
     *
     * @param context Model 属性构建上下文，用于查找注解并写入描述/必填
     */
    @Override
    public void apply(@NonNull ModelPropertyContext context) {

        Optional<CSchema> schemaAnnotation = findAnnotation(context, CSchema.class);
        schemaAnnotation.ifPresent(cSchema -> {
            // value 非空时写入描述，为空则保留已有描述（避免空串覆盖）
            if (StringUtils.hasText(cSchema.value())) {
                context.getBuilder().description(cSchema.value());
            }
        });

        // 必填：标注 @CRequired 即必填（兼容 @Repeatable：重复标注时注解存于容器 @CRequired.List）
        if (findAnnotation(context, CRequired.class).isPresent()
            || findAnnotation(context, CRequired.List.class).isPresent()) {
            context.getBuilder().required(true);
        }
    }

    /**
     * 是否支持该文档类型。
     *
     * <h2>supports</h2>
     * <ul>
     *   <li>默认支持全部文档类型（含 null）。</li>
     * </ul>
     *
     * @param delimiter 文档类型（本插件不区分类型，null 也视为支持）
     * @return 恒为 true，对所有文档类型生效*/
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
