package com.c332030.ctool4j.web.validation.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 接口参数级文档描述注解，命名对应 OpenAPI3 中 Swagger/OpenAPI2 {@code ApiParam} 的替代注解
 * {@code Parameter}（前缀 c）
 * </p>
 *
 * <p>标注在接口方法参数上，描述参数的名称、说明、是否必填、示例等，由 ctool4j-doc-openapi2 的文档插件
 * 读取 {@link #value()}/{@link #name()}/{@link #required()}/{@link #example()} 写入 springfox 的
 * operation 参数（替代原生 {@code @ApiParam}）。纯文档注解，不参与运行时校验。</p>
 *
 * @author c332030
 * @see "doc/design/web/CParameter.adoc"
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
     * 是否必填（对应 OpenAPI3 {@code Parameter.required} / 原生 {@code ApiParam.required}）
     *
     * @return 是否必填
     */
    boolean required() default false;

    /**
     * 参数示例值（对应 OpenAPI3 {@code Parameter.example}）
     *
     * @return 参数示例
     */
    String example() default "";

}
