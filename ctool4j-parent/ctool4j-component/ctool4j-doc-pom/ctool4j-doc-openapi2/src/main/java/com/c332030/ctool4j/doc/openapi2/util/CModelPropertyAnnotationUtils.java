package com.c332030.ctool4j.doc.openapi2.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import java.lang.annotation.Annotation;
import java.util.Optional;

import static java.util.Optional.empty;
import static springfox.documentation.schema.Annotations.findPropertyAnnotation;

/**
 * <p>
 * Description: CModelPropertyAnnotationUtils：model 属性注解查找工具
 * </p>
 *
 * <p>
 * 统一 model 属性上的注解查找顺序：先从 {@code annotatedElement}（字段/getter）取，未命中再退到
 * {@code beanPropertyDefinition}（属性定义）。供 {@code CSchemaAnnotationModelPropertyPlugin}、
 * {@code CTextEnumModelPropertyPlugin} 等 model 属性插件复用，避免各插件重复实现。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code findAnnotation(context, annotationType)}：在 model 属性上查找指定注解。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>annotatedElement 与 beanPropertyDefinition 均不可用</td>
 *     <td>返回 {@code Optional.empty()}</td>
 *   </tr>
 *   <tr>
 *     <td>annotatedElement 已命中</td>
 *     <td>不再查 beanPropertyDefinition</td>
 *   </tr>
 *   <tr>
 *     <td>annotatedElement 未命中（含不可用）</td>
 *     <td>继续查 beanPropertyDefinition</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>springfox {@code ModelPropertyContext} 上的注解查找（字段与 getter）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>方法参数注解查找不适用：参数链路用 {@code ResolvedMethodParameter#findAnnotation}（见
 *   {@code CTextEnumParameterPlugin}、{@code CParameterAnnotationPlugin}）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>只覆盖 annotatedElement 与 beanPropertyDefinition 两处来源；springfox 由类型推导出的描述不在范围内
 *   （调用方据此判断"是否自带描述"时应已知该边界）。</li>
 *   <li>注解不继承自父类属性定义（与 springfox {@code findPropertyAnnotation} 行为一致）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>查找顺序与 springfox 自身一致：annotatedElement 优先，未命中才用 {@code findPropertyAnnotation} 查属性定义。</li>
 *   <li>本工具由原先两份私有实现合并而来（写法统一为"未命中才继续查"）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/16
 * @version 1.0
 */
@UtilityClass
public class CModelPropertyAnnotationUtils {

    /**
     * 在 model 属性上查找指定注解（annotatedElement 优先，未命中再查 beanPropertyDefinition）
     *
     * @param context        Model 属性上下文
     * @param annotationType 注解类型
     * @param <T>            注解类型泛型
     * @return 命中的注解，未命中返回 {@code Optional.empty()}
     */
    public <T extends Annotation> Optional<T> findAnnotation(ModelPropertyContext context, Class<T> annotationType) {

        if (context.getAnnotatedElement().isPresent()) {
            val annotation = Optional.ofNullable(
                context.getAnnotatedElement().get().getAnnotation(annotationType));
            if (annotation.isPresent()) {
                return annotation;
            }
        }

        if (context.getBeanPropertyDefinition().isPresent()) {
            return findPropertyAnnotation(context.getBeanPropertyDefinition().get(), annotationType);
        }

        return empty();
    }

}
