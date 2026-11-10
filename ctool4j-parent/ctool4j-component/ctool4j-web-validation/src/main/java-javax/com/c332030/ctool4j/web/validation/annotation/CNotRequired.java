package com.c332030.ctool4j.web.validation.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 请求参数非必填标记注解（仅 request param / 方法参数级生效），标注即文档标记非必填
 * </p>
 *
 * <p>
 * 独立生效（无需同时标注 @CParameter）：标注本注解即文档非必填，由 openapi2 文档插件据此标记非必填；
 * request param 默认必填（与 {@code @RequestParam.required} 默认一致），需要非必填时标注本注解。
 * 实际缺参放行由 {@code @RequestParam(required = false)} 控制（本注解不参与 SpringMVC 绑定）。
 * </p>
 *
 * <p>
 * 纯标记注解，无属性。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code CParameterAnnotationPlugin}（openapi2）：标注 {@code @CNotRequired} 的参数文档不标记必填。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无属性</td>
 *     <td>纯标记，仅存在性生效</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>request param 参数需要文档标记非必填时叠加 {@code @CNotRequired}。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>request body 字段必填由 {@code @CRequired} 表达（非必填默认），本注解不适用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 {@code @RequestParam} 同时标注时，绑定以 {@code @RequestParam} 为准，{@code @CNotRequired} 仅作文档提示。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>与 CParameter 配合</b></p>
 * <ul>
 *   <li>{@code @CParameter}（ctool4j-definition）只管描述，{@code @CNotRequired} 只声明"文档非必填"，二者可组合：{@code @CNotRequired @CParameter("名称")}。</li>
 * </ul>
 * <p><b>独立生效</b></p>
 * <ul>
 *   <li>{@code CParameterAnnotationPlugin}（openapi2）对标注 {@code @CNotRequired} 的参数直接标记非必填；</li>
 *   <li>即使未标注 {@code @CParameter} 也独立生效（无需依赖 @CParameter 同时存在）。</li>
 * </ul>
 * <p><b>纯标记</b></p>
 * <ul>
 *   <li>不参与 SpringMVC 绑定；缺参放行由 {@code @RequestParam(required = false)} 控制，本注解仅作文档标记。</li>
 * </ul>
 *
 * @since 2026/9/6
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CNotRequired {
}
