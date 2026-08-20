package com.c332030.ctool4j.web.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * <p>
 * Description: 必填校验注解，按被标注值（字段/参数）的实际类型自动选择校验逻辑
 * </p>
 *
 * <p>
 * 合并 notNull、notBlank（字符串）、notEmpty（集合/Map/数组）三类校验，按类型自动分发，
 * 避免开发者因类型选择错误注解（如对集合标 notBlank、对字符串标 notEmpty）而报错：
 * <ul>
 *   <li>null → 不通过（必填）</li>
 *   <li>CharSequence（字符串）→ 按 notBlank（非空且非空白）</li>
 *   <li>Collection / Map → 按 notEmpty（非空集合/Map）</li>
 *   <li>数组 → 按 notEmpty（长度大于 0）</li>
 *   <li>其他对象 → 非 null 即通过</li>
 * </ul>
 * </p>
 *
 * @author c332030
 */
@Target({
    ElementType.FIELD,
    ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
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

}
