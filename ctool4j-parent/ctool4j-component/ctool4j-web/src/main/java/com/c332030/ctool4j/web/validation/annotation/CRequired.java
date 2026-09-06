package com.c332030.ctool4j.web.validation.annotation;

import com.c332030.ctool4j.web.validation.validator.CRequiredValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Description: 必填校验注解（标注即必填，无需 required 属性），合并 notNull、notBlank（字符串）、
 * notEmpty（集合/Map/数组）三类校验，按被标注值的实际类型自动选择校验逻辑，替代原生 {@code @NotNull} 系列
 * </p>
 *
 * <p>
 * 适用于 request body / 字段级必填校验（Bean Validation）；标注 {@code required} 相关的文档必填
 * 由文档插件读取本注解（标注即必填）。与原生 {@code @NotNull} 的 {@code @Target} 保持一致。
 * </p>
 *
 * <ul>
 *   <li>null → 不通过（必填）</li>
 *   <li>CharSequence（字符串）→ 按 notBlank（非空且非空白）</li>
 *   <li>Collection / Map → 按 notEmpty（非空集合/Map）</li>
 *   <li>数组 → 按 notEmpty（长度大于 0）</li>
 *   <li>其他对象 → 非 null 即通过</li>
 * </ul>
 *
 * @see "doc/design/web/CRequired.adoc"
 * @see "doc/design/web/CRequiredValidator.adoc"
 */
@Target({
    METHOD,
    FIELD,
    ANNOTATION_TYPE,
    CONSTRUCTOR,
    PARAMETER,
    TYPE_USE
})
@Retention(RUNTIME)
@Repeatable(CRequired.List.class)
@Constraint(validatedBy = CRequiredValidator.class)
@Documented
public @interface CRequired {

    /**
     * 校验失败时的提示消息
     *
     * @return 提示消息
     */
    String message() default "不能为空";

    /**
     * 分组
     *
     * @return 分组
     */
    Class<?>[] groups() default {};

    /**
     * 载荷
     *
     * @return 载荷
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 可重复标注支持
     */
    @Target({
        METHOD,
        FIELD,
        ANNOTATION_TYPE,
        CONSTRUCTOR,
        PARAMETER,
        TYPE_USE
    })
    @Retention(RUNTIME)
    @Documented
    @interface List {

        /**
         * 可重复的必填校验注解
         *
         * @return 注解数组
         */
        CRequired[] value();
    }

}
