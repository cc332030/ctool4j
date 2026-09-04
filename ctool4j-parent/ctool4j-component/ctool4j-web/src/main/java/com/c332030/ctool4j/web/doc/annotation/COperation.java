package com.c332030.ctool4j.web.doc.annotation;

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
 * @author c332030
 * @see "doc/design/web/COperation.adoc"
 * @see "doc/design/web/CDocAnnotation.adoc"
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
