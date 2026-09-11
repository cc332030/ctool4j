package com.c332030.ctool4j.doc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 接口参数级文档描述注解，命名对应 OpenAPI3 中 Swagger/OpenAPI2 {@code ApiParam} 的替代注解
 * {@code Parameter}（前缀 c）
 * </p>
 *
 * <p>标注在接口方法参数上，描述参数的名称、说明、示例等，由各文档实现
 * （如 ctool4j-doc-openapi2）的插件读取 {@link #value()}/{@link #name()}/{@link #example()}
 * 写入文档 operation 参数（替代原生 {@code @ApiParam}）。</p>
 *
 * <p>纯文档描述注解，不参与运行时绑定；参数绑定与必填由 SpringMVC 原生
 * {@code @RequestParam(required = ...)} 控制（可叠加标注
 * {@code com.c332030.ctool4j.web.validation.annotation.CNotRequired} 表达文档非必填）。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code value()}：参数说明，默认 ""，支持简写 {@code @CParameter("内容")}</li>
 *   <li>（替代了原先的 {@code description()}，对应 {@code Parameter.description} / {@code ApiParam.value}）</li>
 *   <li>{@code name()}：参数名称（对应 {@code Parameter.name} / {@code ApiParam.name}），默认 ""</li>
 *   <li>{@code example()}：参数示例（对应 {@code Parameter.example}），默认 ""</li>
 * </ul>
 * <p>不含 required 属性（必填由 SpringMVC {@code @RequestParam} 或 {@code @CNotRequired} 表达），也不参与 SpringMVC 参数绑定。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>name 为空</td>
 *     <td>沿用参数既有名称</td>
 *   </tr>
 *   <tr>
 *     <td>value/example 为空</td>
 *     <td>不覆盖已有内容</td>
 *   </tr>
 *   <tr>
 *     <td>必填表达（文档）</td>
 *     <td>未标 @CNotRequired 即标记必填；标 @CNotRequired 独立标记非必填</td>
 *   </tr>
 *   <tr>
 *     <td>必填表达（绑定）</td>
 *     <td>由 @RequestParam.required 控制</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口方法参数需要名称/说明/示例时标注 {@code @CParameter}，由 openapi2 的 {@code CParameterAnnotationPlugin} 落地文档。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不参与运行时绑定；绑定请用 {@code @RequestParam}。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>替代 OpenAPI2 ApiParam</b></p>
 * <ul>
 *   <li>{@code @CParameter} 替代参数级 {@code @ApiParam}。</li>
 * </ul>
 * <p><b>纯文档描述</b></p>
 * <ul>
 *   <li>文档侧由 openapi2 的 {@code CParameterAnnotationPlugin} 读取 name/description/example 写入 springfox；</li>
 *   <li>必填判定：标注 {@code @CNotRequired} 即非必填（独立生效，无需同时标注 @CParameter）；</li>
 *   <li>标注 {@code @CParameter} 且未标 {@code @CNotRequired} 时标记必填（默认必填）。</li>
 * </ul>
 * <p><b>绑定分离</b></p>
 * <ul>
 *   <li>参数绑定与必填由 SpringMVC 原生 {@code @RequestParam(required = ...)} 控制，{@code @CParameter} 不驱动绑定。</li>
 * </ul>
 *
 * @author c332030
 * @see "doc/design/definition/openapi-doc-annotations.adoc"
 * @since 1.0
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CParameter {

    /**
     * 参数说明（对应原生 {@code ApiParam.value} / OpenAPI3 {@code Parameter.description}），
     * 支持简写 {@code @CParameter("内容")}。替代了原先的 {@code description()} 属性
     *
     * @return 参数说明
     */
    String value() default "";

    /**
     * 参数名称（对应 OpenAPI3 {@code Parameter.name} / 原生 {@code ApiParam.name}），
     * 为空时沿用被标注参数的既有名称
     *
     * @return 参数名称
     */
    String name() default "";

    /**
     * 参数示例值（对应 OpenAPI3 {@code Parameter.example}）
     *
     * @return 参数示例
     */
    String example() default "";

}
