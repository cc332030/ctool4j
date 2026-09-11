package com.c332030.ctool4j.web.validation.validator;

import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.web.validation.annotation.CRequired;
import lombok.CustomLog;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CRequired 注解校验器：标注 {@link CRequired} 即必填（无 required 开关），
 * 校验规则按值类型自动分发，复用 {@link CValidUtils}
 * （字符串→notBlank、集合/Map/数组→notEmpty、其他→notNull）
 * </p>
 *
 * <p>
 * 属性支持：校验失败时应用 {@link CRequired#message()}（默认"不能为空"）作为约束消息；
 * {@link CRequired#groups()} / {@link CRequired#payload()} 由 Bean Validation 框架按标准约定自动处理
 * （分组过滤/载荷元数据），validator 无需读取。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequiredValidator} 为 {@code @CRequired} 注解的校验器，实现 {@code ConstraintValidator&lt;CRequired, Object&gt;}， 校验规则按值类型自动分发，复用 {@code CValidUtils}。标注 {@code @CRequired} 即必填（无 required 开关）。</p>
 * <p>核心方法：</p>
 * <ul>
 *   <li>{@code isValidValue(Object)}：按类型分发（null 不通过、CharSequence→notBlank、Collection→notEmpty、</li>
 *   <li>Map→notEmpty、数组→notEmpty、其他对象→notNull）</li>
 *   <li>{@code isValidArray(Object)}：按数组类型分发到 CValidUtils 数组重载；未覆盖类型（float[]/double[]/boolean[]）</li>
 *   <li>按 {@code Array.getLength &gt; 0} 判断</li>
 * </ul>
 * <p>校验失败时应用注解声明的 {@code message}（{@code disableDefaultConstraintViolation} + 自定义模板）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>值类型不在明确分支</td>
 *     <td>数组未覆盖类型按长度判断；其他对象非 null 即通过</td>
 *   </tr>
 *   <tr>
 *     <td>{@code CValidUtils} 对 null 的判定</td>
 *     <td>null 返回 false，必填场景 null 不通过</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@code @CRequired} 标注字段/参数时执行必填校验。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code float[]}/{@code double[]}/{@code boolean[]} 未走 CValidUtils 重载，按长度判断（CValidUtils 未覆盖）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>标注即必填</b></p>
 * <ul>
 *   <li>{@code @CRequired} 无 required 开关，标注即执行必填校验（原 CSchemaValidator 的 required 开关迁移后去除）。</li>
 * </ul>
 * <p><b>类型自动分发</b></p>
 * <ul>
 *   <li>合并 notNull/notBlank/notEmpty 校验按值类型自动分发，复用 {@code CValidUtils} 各类重载。</li>
 * </ul>
 * <p><b>自定义消息</b></p>
 * <ul>
 *   <li>校验失败时禁用默认约束消息，改用注解声明的 {@code message}（供异常处理器拼接字段名前缀）。</li>
 * </ul>
 * <p><b>分组/载荷</b></p>
 * <ul>
 *   <li>{@code groups()}/{@code payload()} 由 Bean Validation 框架按标准约定自动处理，validator 无需读取。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@CustomLog
public class CRequiredValidator implements ConstraintValidator<CRequired, Object> {

    /**
     * 校验失败时的约束消息
     */
    private String message;

    /**
     * 读取约束注解声明的消息模板，供校验失败时使用。
     * <ul>
     *   <li>{@code initialize(CRequired)}：读取 {@code message}</li>
     * </ul>
     *
     * @param constraintAnnotation 当前生效的 {@link CRequired} 注解实例
     */
    @Override
    public void initialize(CRequired constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    /**
     * 按值类型自动分发的必填校验。
     *
     * <p>校验不通过时禁用默认约束消息、改用注解声明的 {@code message} 模板构建违规信息，保证与项目统一返回体的
     * 消息口径一致。</p>
     * <ul>
     *   <li>{@code isValid(Object, ConstraintValidatorContext)}：按类型分发校验</li>
     * </ul>
     *
     * @param value   被校验值（可为 null）
     * @param context 约束校验上下文，用于构建自定义违规信息
     * @return 是否满足必填约束
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        boolean valid = isValidValue(value);
        log.debug("CRequired 校验: value={}, valid={}", value, valid);
        if (!valid) {
            // 应用自定义 message（禁用默认约束消息，改用注解声明的 message）
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }
        return valid;
    }

    /**
     * 按值类型自动分发的必填校验逻辑
     *
     * @param value 被校验值
     * @return 是否有效（必填场景下非空）
     */
    private boolean isValidValue(Object value) {

        // 必填：null 不通过（CValidUtils.isValid(Object) 对 null 返回 false）
        if (null == value) {
            return false;
        }
        // 字符串：按 notBlank（CValidUtils 字符串使用 blank 判断）
        if (value instanceof CharSequence) {
            return CValidUtils.isValid((CharSequence) value);
        }
        // 集合 / Map：按 notEmpty
        if (value instanceof Collection) {
            return CValidUtils.isValid((Collection<?>) value);
        }
        if (value instanceof Map) {
            return CValidUtils.isValid((Map<?, ?>) value);
        }
        // 数组：按 notEmpty（分发到 CValidUtils 数组重载）
        if (value.getClass().isArray()) {
            return isValidArray(value);
        }
        // 其他对象（含 Number）：非 null 即通过
        return CValidUtils.isValid(value);
    }

    /**
     * 按数组类型分发到 {@link CValidUtils} 对应数组重载
     *
     * @param array 数组
     * @return 是否有效（非空）
     */
    private boolean isValidArray(Object array) {
        if (array instanceof byte[]) {
            return CValidUtils.isValid((byte[]) array);
        }
        if (array instanceof short[]) {
            return CValidUtils.isValid((short[]) array);
        }
        if (array instanceof char[]) {
            return CValidUtils.isValid((char[]) array);
        }
        if (array instanceof int[]) {
            return CValidUtils.isValid((int[]) array);
        }
        if (array instanceof long[]) {
            return CValidUtils.isValid((long[]) array);
        }
        if (array instanceof Object[]) {
            return CValidUtils.isValid((Object[]) array);
        }
        // CValidUtils 未覆盖的数组类型（如 float[]/double[]/boolean[]）按长度判断
        return java.lang.reflect.Array.getLength(array) > 0;
    }

}
