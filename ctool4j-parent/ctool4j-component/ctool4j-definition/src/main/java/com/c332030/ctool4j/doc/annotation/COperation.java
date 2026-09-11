package com.c332030.ctool4j.doc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 接口方法级文档描述注解，命名对应 OpenAPI3 中 Swagger/OpenAPI2 {@code ApiOperation} 的替代注解
 * {@code Operation}（前缀 c）
 * </p>
 *
 * <p>标注在接口方法上，描述单个接口操作的摘要、说明等，由 ctool4j-doc-openapi2 的文档插件
 * 读取 {@link #value()}/{@link #description()}/{@link #operationId()} 等写入 springfox 的 operation
 * （替代原生 {@code @ApiOperation}）。纯文档注解，不参与运行时校验。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code value()}：操作摘要，作为接口标题，默认 ""，支持简写 {@code @COperation("内容")}</li>
 *   <li>（替代了原先的 {@code summary()}，对应 {@code Operation.summary} / {@code ApiOperation.value}）</li>
 *   <li>{@code description()}：操作详细说明（对应 {@code Operation.description} / {@code ApiOperation.notes}），默认 ""</li>
 *   <li>{@code operationId()}：操作唯一标识（对应 {@code Operation.operationId}），默认 ""（openapi2 暂不覆盖，见已知限制）</li>
 *   <li>{@code tags()}：操作归属分组标签（对应 {@code Operation.tags}），默认空</li>
 *   <li>{@code deprecated()}：是否废弃（对应 {@code Operation.deprecated}），默认 false</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>value/description 为空</td>
 *     <td>不覆盖已有内容</td>
 *   </tr>
 *   <tr>
 *     <td>deprecated=false</td>
 *     <td>不标记废弃</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口方法需要摘要/说明/分组时标注 {@code @COperation}，由 openapi2 的 {@code COperationAnnotationPlugin} 落地。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>springfox 会为 operation 自动生成 id，openapi2 插件暂不覆盖 {@code operationId}；该属性保留供 OpenAPI3 生成使用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>替代 OpenAPI2 ApiOperation</b></p>
 * <ul>
 *   <li>{@code @COperation} 替代方法级 {@code @ApiOperation}。</li>
 * </ul>
 * <p><b>纯文档</b></p>
 * <ul>
 *   <li>仅提供文档元数据，不参与运行时校验。</li>
 * </ul>
 *
 * @author c332030
 * @see "doc/design/definition/openapi-doc-annotations.adoc"
 * @since 1.0
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface COperation {

    /**
     * 操作摘要，作为接口标题（对应原生 {@code ApiOperation.value} / OpenAPI3 {@code Operation.summary}），
     * 支持简写 {@code @COperation("内容")}。替代了原先的 {@code summary()} 属性
     *
     * @return 操作摘要
     */
    String value() default "";

    /**
     * 操作详细说明（对应 OpenAPI3 {@code Operation.description} / 原生 {@code ApiOperation.notes}）
     *
     * @return 操作详细说明
     */
    String description() default "";

    /**
     * 操作唯一标识（对应 OpenAPI3 {@code Operation.operationId}）。
     * 注意：springfox 会为 operation 自动生成 id，openapi2 插件暂不覆盖该字段，此属性保留供 OpenAPI3 生成使用
     *
     * @return 操作唯一标识
     */
    String operationId() default "";

    /**
     * 操作归属的分组标签（对应 OpenAPI3 {@code Operation.tags}），为空时沿用所属类 {@code CTag} 分组
     *
     * @return 分组标签数组
     */
    String[] tags() default {};

    /**
     * 是否标记为废弃（对应 OpenAPI3 {@code Operation.deprecated} / 原生 {@code ApiOperation.hidden=false} 反向）
     *
     * @return 是否废弃
     */
    boolean deprecated() default false;

}
