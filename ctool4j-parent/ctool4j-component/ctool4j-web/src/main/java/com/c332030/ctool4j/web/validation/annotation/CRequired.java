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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code message()}：校验失败提示消息（默认 "不能为空"），字段名前缀由 {@code CMethodArgumentNotValidExceptionHandler} 拼接</li>
 *   <li>{@code groups()}/{@code payload()}：Bean Validation 标准分组/载荷</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>标注即校验</td>
 *     <td>required 恒为必填</td>
 *   </tr>
 *   <tr>
 *     <td>message 缺省</td>
 *     <td>默认 "不能为空"</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>request body DTO 字段/getter 需要必填校验与文档必填标记时标注 {@code @CRequired}。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>request param 方法参数级非必填由 {@code @CNotRequired} 表达（见 {@code CNotRequired.adoc}）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>校验按类型自动分发，依赖 {@code CValidUtils} 各类重载。</li>
 *   <li>{@code @Target} 与 {@code @NotNull} 一致，允许标注在参数上；request body 场景实际作用于字段。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>标注即必填</b></p>
 * <ul>
 *   <li>无 required 属性，标注即执行必填校验；不标注默认非必填（request body 字段默认非必填）。</li>
 * </ul>
 * <p><b>文档联动</b></p>
 * <ul>
 *   <li>openapi2 的 {@code CRequiredAnnotationPlugin}（参数展开）与 {@code CSchemaAnnotationModelPropertyPlugin}（model 属性）</li>
 *   <li>读取 {@code @CRequired} 将字段/属性标记为必填。</li>
 * </ul>
 * <p><b>与 CSchema 解耦</b></p>
 * <ul>
 *   <li>描述由 {@code @CSchema}（纯文档）提供，必填由 {@code @CRequired} 承担，字段可组合标注。</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
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
