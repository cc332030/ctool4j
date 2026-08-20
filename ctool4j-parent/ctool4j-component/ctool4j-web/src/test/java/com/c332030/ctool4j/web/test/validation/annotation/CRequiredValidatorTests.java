package com.c332030.ctool4j.web.test.validation.annotation;

import com.c332030.ctool4j.web.validation.annotation.CRequiredValidator;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * <p>
 * Description: CRequiredValidator 按类型自动校验逻辑测试
 * </p>
 *
 * <p>
 * 覆盖：null、字符串（notBlank）、集合（notEmpty）、Map（notEmpty）、数组（notEmpty）、其他对象（notNull）
 * </p>
 *
 * @author c332030
 */
public class CRequiredValidatorTests {

    private final CRequiredValidator validator = new CRequiredValidator();

    @Test
    public void validate_null() {
        // 边界：null 不通过（必填）
        Assertions.assertFalse(validator.isValid(null, null));
    }

    @Test
    public void validate_string() {
        // 正例：字符串非空白通过
        Assertions.assertTrue(validator.isValid("abc", null));
        // 边界：空白字符串不通过（notBlank）
        Assertions.assertFalse(validator.isValid("   ", null));
        Assertions.assertFalse(validator.isValid("", null));
    }

    @Test
    public void validate_collection() {
        // 正例：非空集合通过
        Assertions.assertTrue(validator.isValid(Collections.singletonList(1), null));
        // 边界：空集合不通过（notEmpty）
        Assertions.assertFalse(validator.isValid(Collections.emptyList(), null));
    }

    @Test
    public void validate_map() {
        // 正例：非空 Map 通过
        Assertions.assertTrue(validator.isValid(Collections.singletonMap("k", "v"), null));
        // 边界：空 Map 不通过（notEmpty）
        Assertions.assertFalse(validator.isValid(Collections.emptyMap(), null));
    }

    @Test
    public void validate_array() {
        // 正例：非空数组通过
        Assertions.assertTrue(validator.isValid(new int[]{1, 2}, null));
        Assertions.assertTrue(validator.isValid(new Object[]{"a"}, null));
        // 边界：空数组不通过（notEmpty）
        Assertions.assertFalse(validator.isValid(new int[0], null));
        Assertions.assertFalse(validator.isValid(new Object[0], null));
    }

    @Test
    public void validate_otherObject() {
        // 正例：其他对象非 null 即通过
        val value = new Object();
        Assertions.assertTrue(validator.isValid(value, null));
        Assertions.assertTrue(validator.isValid(42, null));
        Assertions.assertTrue(validator.isValid(true, null));
    }

}
