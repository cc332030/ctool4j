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
 * @author c332030
 * @see "doc/design/doc-base/CSchema.adoc"
 * @see "doc/design/doc-base/CDocAnnotation.adoc"
 */
@Target({
    ElementType.FIELD,
    ElementType.PARAMETER,
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
