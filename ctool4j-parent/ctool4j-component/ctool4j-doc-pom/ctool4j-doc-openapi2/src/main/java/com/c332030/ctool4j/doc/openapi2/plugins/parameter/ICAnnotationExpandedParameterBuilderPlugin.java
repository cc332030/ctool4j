package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import lombok.val;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import java.lang.annotation.Annotation;

/**
 * <p>
 * Description: ICAnnotationExpandedParameterBuilderPlugin
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code apply(context)}：命中目标注解且 {@code isRequired} 为 true 时标记参数必填。</li>
 *   <li>{@code isRequired(annotation)}：命中时是否必填（默认 true）。</li>
 *   <li>{@code getAnnotationClass()}：目标注解类型。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未命中注解</td>
 *     <td>不做处理</td>
 *   </tr>
 *   <tr>
 *     <td>isRequired 返回 false</td>
 *     <td>不标记必填</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>统一处理"校验注解 → 参数必填"的 springfox 插件逻辑。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认 isRequired 恒 true，需按 required 属性区分的注解由子类覆写。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>apply 默认实现</b></p>
 * <ul>
 *   <li>从 context 查目标注解，命中且 {@code isRequired} 为 true 时 {@code context.getParameterBuilder().required(true)}。</li>
 * </ul>
 * <p><b>isRequired</b></p>
 * <ul>
 *   <li>默认 true：无 required 开关的注解（如 {@code @NotEmpty}、{@code @CRequired}）命中即必填。</li>
 *   <li>需按注解属性定制判定时可覆写本方法。</li>
 * </ul>
 *
 * @since 2025/12/17
 * @version 1.0
 */
public interface ICAnnotationExpandedParameterBuilderPlugin<T extends Annotation> extends ICExpandedParameterBuilderPlugin {

    /**
     * 处理参数展开上下文（存在目标注解且 {@link #isRequired} 为 true 时将参数标记为必填）
     * @param context 参数展开上下文
     */
    @Override
    default void apply(ParameterExpansionContext context) {

        val annotationOpt = context.findAnnotation(getAnnotationClass());
        if (annotationOpt.isPresent() && isRequired(annotationOpt.get())) {
            context.getParameterBuilder().required(true);
        }
    }

    /**
     * 注解命中时是否标记为必填（默认 true：无 required 开关的注解如 {@code @NotEmpty}、{@code @CRequired} 命中即必填；
     * 需按注解属性定制判定时可覆写本方法）
     *
     * @param annotation 命中的注解
     * @return 是否必填
     */
    default boolean isRequired(Annotation annotation) {
        return true;
    }

    /**
     * 获取目标注解类型
     * @return 目标注解类型
     */
    Class<T> getAnnotationClass();

}
