package com.c332030.ctool4j.web.validation.annotation;

import com.c332030.ctool4j.web.validation.validator.CSchemaValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Description: 必填校验 + 文档描述注解，命名对应 OpenAPI3 中 {@code ApiModelProperty} 的替代注解
 * {@code Schema}（前缀 c）。按被标注值（字段/参数）的实际类型自动选择校验逻辑
 *
 * <p>合并 notNull、notBlank（字符串）、notEmpty（集合/Map/数组）三类校验，按类型自动分发，
 * 避免开发者因类型选择错误注解（如对集合标 notBlank、对字符串标 notEmpty）而报错：</p>
 *
 * <ul>
 *   <li>null → 不通过（必填）</li>
 *   <li>CharSequence（字符串）→ 按 notBlank（非空且非空白）</li>
 *   <li>Collection / Map → 按 notEmpty（非空集合/Map）</li>
 *   <li>数组 → 按 notEmpty（长度大于 0）</li>
 *   <li>其他对象 → 非 null 即通过</li>
 * </ul>
 *
 * <p>{@link #required()} 控制是否必填（默认 false，不强制）；{@link #value()} 作为文档描述
 * （配合 ctool4j-doc-openapi2 的 CSchemaAnnotationModelPropertyPlugin 写入属性 description）。
 * 可标注在字段或 getter 方法（如接口 {@code ICUsername.getUsername()}）上。</p>
 *
 * @author c332030
 */
@Target({
    ElementType.FIELD,
    ElementType.PARAMETER,
    ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CSchemaValidator.class)
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

    /**
     * 是否必填（默认 false）
     *
     * <p>true：执行必填校验（按类型自动分发），并在文档中标记为必填；
     * false（默认）：不强制必填，仅提供 {@link #value()} 描述。</p>
     *
     * @return 是否必填
     */
    boolean required() default false;

    /**
     * 校验失败时的提示消息
     *
     * <p>仅描述校验不通过的原因（如 "不能为空"），字段名前缀由异常处理器
     * {@code CMethodArgumentNotValidExceptionHandler} 拼接为 "字段名 + message"。
     * 也可显式指定自定义 message 覆盖。</p>
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
