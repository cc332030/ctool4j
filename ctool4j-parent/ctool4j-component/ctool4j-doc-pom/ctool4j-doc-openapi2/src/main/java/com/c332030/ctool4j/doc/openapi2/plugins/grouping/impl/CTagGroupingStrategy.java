package com.c332030.ctool4j.doc.openapi2.plugins.grouping.impl;

import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.doc.annotation.CTag;
import io.swagger.annotations.Api;
import lombok.val;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.SpringGroupingStrategy;
import springfox.documentation.spring.wrapper.RequestMappingInfo;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Description: CTagGroupingStrategy：控制器级（分组）标签策略，用类上 {@code @CTag} 的
 * {@code value}/description 作为 springfox 的**分组名与分组描述**，替代 springfox 默认的类名派生
 * （{@code SpringGroupingStrategy} 会把类名转成 kebab 英文名，如 {@code we-com-controller}）
 * </p>
 *
 * <p>作用：{@code @CTag} 此前只经 {@code CTagAnnotationPlugin} 落到 <b>operation</b> 级 tags，
 * 而 swagger 的 {@code tags} 分组列表来自<b>控制器级</b> ResourceGroup——两者不一致时，每个 Controller 会多出一个
 * 没有接口的英文空分组（中文分组与英文空分组并存）。本策略把分组名统一为 {@code @CTag} 值后，两组归一（单分组）。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code getResourceGroups(...)}：分组名取类上 {@code @CTag.value}，缺失时回退存量 {@code @Api.tags}，
 *   两者都无则沿用 springfox 默认（类名 kebab）。</li>
 *   <li>{@code getResourceDescription(...)}：分组描述取 {@code @CTag.description}，缺失时回退 {@code @Api.value}，
 *   都无则沿用 springfox 默认（类名分词）。</li>
 *   <li>未覆写的 {@code getResourcePosition} / {@code supports} 继承默认实现。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为什么用 ResourceGroupingStrategy 而不是 operation 插件</b></p>
 * <ul>
 *   <li>springfox 的分组列表由 {@code ResourceGroupingStrategy} 产出（{@code DocumentationPluginsManager}
 *   {@code getPluginOrDefaultFor(type, new SpringGroupingStrategy())}），与 operation 的 tags 是两条独立链路；
 *   只改 operation tags 无法消除控制器级空分组。</li>
 *   <li>注册本类为 Bean 即被 springfox 的插件注册表按类型收集，成为唯一的分组策略，默认实现自动让位
 *   （无需排除、无需改动 springfox 自身配置）。</li>
 * </ul>
 * <p><b>与 operation 级 tag 的一致性</b></p>
 * <ul>
 *   <li>取值优先级与 {@code CTagAnnotationPlugin} 一致（{@code @CTag} 优先、兼容存量 {@code @Api}），
 *   故分组名与 operation tags 相同，swagger-ui 只呈现一个分组。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无 {@code @CTag}、无 {@code @Api}</td>
 *     <td>回退 springfox 默认策略（类名 splitCamelCase 转 kebab）</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @CTag.value} 为空白</td>
 *     <td>继续尝试 {@code @Api.tags}，仍无则回退默认</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @CTag.description} 为空白</td>
 *     <td>继续尝试 {@code @Api.value}，仍无则回退默认（类名分词）</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @Api.tags} 含多个值</td>
 *     <td>取第一个非空白值作为分组名（其余不参与分组）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>以 {@code @CTag} 声明接口分组的 Controller，期望 swagger-ui 呈现中文分组且不出现英文空分组。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要"按路径前缀分组"等自定义分组语义时，应另行实现 {@code ResourceGroupingStrategy} 并以
 *   {@code @Primary} 或条件装配取代本 Bean（同一类型多 Bean 时由 springfox 插件注册表择一，行为不确定）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>一个 Controller 只产出一个分组（与 {@code @CTag} 单值语义一致）：{@code @Api.tags} 的多值仅取首个，
 *   不支持一个 Controller 拆到多个分组。</li>
 *   <li>分组名参与 swagger 分组键（{@code ResourceGroup}），同一分组名被多个 Controller 使用时会被合并为一组。</li>
 *   <li>依赖 springfox（OpenAPI2）；OpenAPI3/springdoc 场景不适用。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/15
 * @version 1.0
 */
public class CTagGroupingStrategy extends SpringGroupingStrategy {

    /**
     * 分组名：类上 {@code @CTag.value} 优先，其次存量 {@code @Api.tags}，都无则回退 springfox 默认
     *
     * <p>返回单元素集合：分组名与 operation 级 tag 一致，避免"中文分组 + 英文空分组"并存。</p>
     *
     * @param requestMappingInfo springfox 的请求映射包装（本实现不使用，回退默认实现时会用到）
     * @param handlerMethod      处理器方法，用于取控制器类型
     * @return 分组集合（单元素）
     */
    @Override
    public Set<ResourceGroup> getResourceGroups(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {

        val controllerClass = handlerMethod.getBeanType();
        val tag = resolveTag(controllerClass);

        if (CValidUtils.isValid(tag)) {
            return Collections.singleton(new ResourceGroup(tag, controllerClass));
        }

        return super.getResourceGroups(requestMappingInfo, handlerMethod);
    }

    /**
     * 分组描述：类上 {@code @CTag.description} 优先，其次存量 {@code @Api.value}，都无则回退 springfox 默认
     *
     * @param requestMappingInfo springfox 的请求映射包装（本实现不使用，回退默认实现时会用到）
     * @param handlerMethod      处理器方法，用于取控制器类型
     * @return 分组描述
     */
    @Override
    public String getResourceDescription(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {

        val description = resolveDescription(handlerMethod.getBeanType());
        if (CValidUtils.isValid(description)) {
            return description;
        }

        return super.getResourceDescription(requestMappingInfo, handlerMethod);
    }

    /**
     * 解析分组名：{@code @CTag.value} → {@code @Api.tags}（首个非空白）
     *
     * @param controllerClass 控制器类型
     * @return 分组名；未声明返回 null
     */
    private static String resolveTag(Class<?> controllerClass) {

        val cTag = AnnotationUtils.findAnnotation(controllerClass, CTag.class);
        if (CValidUtils.isValid(cTag) && CValidUtils.isValid(cTag.value())) {
            return cTag.value();
        }

        val api = AnnotationUtils.findAnnotation(controllerClass, Api.class);
        if (CValidUtils.isValid(api) && CValidUtils.isValid(api.tags())) {
            for (val tag : api.tags()) {
                if (CValidUtils.isValid(tag)) {
                    return tag;
                }
            }
        }

        return null;
    }

    /**
     * 解析分组描述：{@code @CTag.description} → {@code @Api.value}
     *
     * @param controllerClass 控制器类型
     * @return 分组描述；未声明返回 null
     */
    private static String resolveDescription(Class<?> controllerClass) {

        val cTag = AnnotationUtils.findAnnotation(controllerClass, CTag.class);
        if (CValidUtils.isValid(cTag) && CValidUtils.isValid(cTag.description())) {
            return cTag.description();
        }

        val api = AnnotationUtils.findAnnotation(controllerClass, Api.class);
        if (CValidUtils.isValid(api) && CValidUtils.isValid(api.value())) {
            return api.value();
        }

        return null;
    }

    /**
     * 是否支持该文档类型（继承默认实现：支持全部类型）
     *
     * @param delimiter 文档类型
     * @return 恒为 true
     */
    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return super.supports(delimiter);
    }

}
