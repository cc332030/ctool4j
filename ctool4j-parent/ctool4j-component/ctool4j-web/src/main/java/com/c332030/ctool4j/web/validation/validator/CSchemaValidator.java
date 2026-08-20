package com.c332030.ctool4j.web.validation.validator;

import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.web.validation.annotation.CSchema;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CSchema 注解校验器：按 {@link CSchema#required()} 决定是否校验，
 * 校验规则按值类型自动分发，复用 {@link CValidUtils}
 * （字符串→notBlank、集合/Map/数组→notEmpty、其他→notNull）
 * </p>
 *
 * @author c332030
 */
public class CSchemaValidator implements ConstraintValidator<CSchema, Object> {

    /**
     * 是否必填（false 时跳过校验，直接通过）
     */
    private boolean required;

    @Override
    public void initialize(CSchema constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        // 非必填：不校验，直接通过
        if (!required) {
            return true;
        }
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
