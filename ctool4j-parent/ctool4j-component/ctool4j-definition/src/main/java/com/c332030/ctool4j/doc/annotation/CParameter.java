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
 * @author c332030
 * @see "doc/design/definition/CParameter.adoc"
 * @see "doc/design/definition/CDocAnnotation.adoc"
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
