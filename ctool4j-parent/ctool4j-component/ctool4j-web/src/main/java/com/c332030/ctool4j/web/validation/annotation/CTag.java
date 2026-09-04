package com.c332030.ctool4j.web.validation.annotation;

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
 * @author c332030
 * @see "doc/design/web/CTag.adoc"
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
