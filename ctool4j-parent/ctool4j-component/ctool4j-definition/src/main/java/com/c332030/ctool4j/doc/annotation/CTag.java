package com.c332030.ctool4j.doc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 接口/Controller 级文档描述注解，命名对应 OpenAPI3 中 Swagger/OpenAPI2 {@code Api} 的替代注解
 * {@code Tag}（前缀 c）
 * </p>
 *
 * <p>标注在 Controller/接口类上，说明该类接口的分组（tag）与描述。
 * 由 ctool4j-doc-openapi2 的文档插件读取 {@link #value()}/{@link #description()}，
 * 作为 springfox 的分组 tag 及描述（替代原生 {@code @Api}），并以 {@link #value()} 作为
 * springfox 收集 Controller 的判定条件（对应 Docket 的 withClassAnnotation）。纯文档注解，不参与运行时校验。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code value()}：分组标签内容（作为该 Controller 的显示分组名），默认 ""，支持简写 {@code @CTag("内容")}</li>
 *   <li>（替代了原先的 {@code name()}，对应 OpenAPI3 {@code Tag.name} / OpenAPI2 {@code Api.tags}）</li>
 *   <li>{@code description()}：分组标签描述（对应 OpenAPI3 {@code Tag.description} / OpenAPI2 {@code Api.value}），默认 ""</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>value 为空</td>
 *     <td>由插件按实际需要处理，不强制分组名</td>
 *   </tr>
 *   <tr>
 *     <td>description 为空</td>
 *     <td>不覆盖已有描述</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Controller/接口类需要分组名与描述时标注 {@code @CTag}，由 openapi2 的 {@code CTagAnnotationPlugin} 落地分组。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>openapi2（springfox）对 tag 描述的渲染受框架限制，{@code description} 以能落地的位置为准。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>替代 OpenAPI2 Api</b></p>
 * <ul>
 *   <li>项目以 C 前缀自定义注解替代 Swagger 注解（同 CSchema 替代 {@code @ApiModelProperty}），{@code @CTag} 替代类级 {@code @Api}。</li>
 * </ul>
 * <p><b>纯文档</b></p>
 * <ul>
 *   <li>仅提供文档元数据，不参与运行时校验（无 @Constraint）。</li>
 * </ul>
 *
 * @author c332030
 * @see "doc/design/definition/openapi-doc-annotations.adoc"
 * @since 1.0
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CTag {

    /**
     * 分组标签内容，作为该 Controller 的显示分组名（springfox 收集并归组使用）；
     * 支持简写 {@code @CTag("内容")}。替代了原先的 {@code name()} 属性
     *
     * @return 分组标签内容
     */
    String value() default "";

    /**
     * 分组标签描述（对应 OpenAPI3 {@code Tag.description} / 原生 {@code Api.value}），
     * 非空时写入文档 tag 描述；为空则不覆盖
     *
     * @return 分组标签描述
     */
    String description() default "";

}
