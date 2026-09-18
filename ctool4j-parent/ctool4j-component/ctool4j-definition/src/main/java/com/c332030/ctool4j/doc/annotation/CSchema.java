package com.c332030.ctool4j.doc.annotation;

import java.lang.annotation.*;

/**
 * Description: 接口属性文档描述注解，命名对应 OpenAPI3 {@code Schema} 的替代注解
 * {@code Schema}（前缀 c），语义与原生 {@code ApiModelProperty} / OpenAPI3 {@code Schema} 一致，
 * 仅承载文档描述（description），不参与运行时校验
 *
 * <p>可标注在字段或 getter 方法（如接口 {@code ICUsername.getUsername()}）上。
 * 需要必填校验时配合 {@code com.c332030.ctool4j.web.validation.annotation.CRequired}
 * （校验实现见 {@code com.c332030.ctool4j.web.validation.validator.CRequiredValidator}）。</p>
 *
 * <p>参数级描述请用 {@code @CParameter}（参数必填由 {@code @CRequired} / {@code @CNotRequired} 表达）。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code value()}：属性描述（对应 {@code ApiModelProperty.value} / {@code Schema.description}），默认 ""</li>
 * </ul>
 * <p>需要必填校验时配合 {@code CRequired} 注解（定义在 ctool4j-web），描述与必填解耦。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>value 为空</td>
 *     <td>不覆盖已有描述</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口属性/字段/getter 需要文档描述时标注 {@code @CSchema}。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不做运行时校验；需必填校验时配合 {@code @CRequired}（web.validation.annotation）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>纯文档描述</b></p>
 * <ul>
 *   <li>与 {@code @ApiModelProperty} / {@code @Schema} 对齐，只承载文档描述；不再含校验相关属性。</li>
 *   <li>必填语义迁移至 {@code CRequired}（web.validation）。</li>
 * </ul>
 * <p><b>文档联动</b></p>
 * <ul>
 *   <li>{@code value()} 作为文档描述，配合 openapi2 的 {@code CSchemaAnnotationModelPropertyPlugin} 写入属性 description。</li>
 * </ul>
 *
 * @author c332030
 * @see "doc/design/definition/openapi-doc-annotations.adoc"
 * @since 1.0
 * @version 1.0
 */
@Target({
    ElementType.FIELD,
    ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CSchema {

    /**
     * 字段描述（对应文档中的属性描述，功能同 {@code ApiModelProperty.value} / OpenAPI3 {@code Schema.description}）
     *
     * <p>非空时由文档插件写入属性 description；为空则不覆盖描述。不参与运行时校验。</p>
     *
     * @return 字段描述
     */
    String value() default "";

}
