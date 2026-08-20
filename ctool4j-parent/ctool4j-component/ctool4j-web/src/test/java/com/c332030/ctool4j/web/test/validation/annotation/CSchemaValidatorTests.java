package com.c332030.ctool4j.web.test.validation.annotation;

import com.c332030.ctool4j.web.validation.annotation.CSchema;
import com.c332030.ctool4j.web.validation.validator.CSchemaValidator;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CSchemaValidator 按类型自动校验逻辑测试
 * </p>
 *
 * <p>
 * 覆盖：required=false 跳过校验、required=true 时 null/字符串（notBlank）/集合（notEmpty）/
 * Map（notEmpty）/数组（notEmpty）/其他对象（notNull）
 * </p>
 *
 * @author c332030
 */
public class CSchemaValidatorTests {

    /**
     * 构造指定 required 的 CSchema 注解实例
     *
     * @param required 是否必填
     * @return CSchema 实例
     */
    private CSchema cSchema(boolean required) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("required", required);
        return AnnotationUtils.synthesizeAnnotation(attributes, CSchema.class, null);
    }

    @Test
    public void validate_notRequired() {
        // required=false：不校验，任意值（含 null/空白/空集合）均通过
        val validator = newValidator(false);
        Assertions.assertTrue(validator.isValid(null, null));
        Assertions.assertTrue(validator.isValid("   ", null));
        Assertions.assertTrue(validator.isValid(Collections.emptyList(), null));
        Assertions.assertTrue(validator.isValid(Collections.emptyMap(), null));
        Assertions.assertTrue(validator.isValid(new int[0], null));
    }

    @Test
    public void validate_null() {
        // 边界：required=true，null 不通过（必填）
        Assertions.assertFalse(newValidator(true).isValid(null, null));
    }

    @Test
    public void validate_string() {
        // 正例：字符串非空白通过
        Assertions.assertTrue(newValidator(true).isValid("abc", null));
        // 边界：空白字符串不通过（notBlank）
        Assertions.assertFalse(newValidator(true).isValid("   ", null));
        Assertions.assertFalse(newValidator(true).isValid("", null));
    }

    @Test
    public void validate_collection() {
        // 正例：非空集合通过
        Assertions.assertTrue(newValidator(true).isValid(Collections.singletonList(1), null));
        // 边界：空集合不通过（notEmpty）
        Assertions.assertFalse(newValidator(true).isValid(Collections.emptyList(), null));
    }

    @Test
    public void validate_map() {
        // 正例：非空 Map 通过
        Assertions.assertTrue(newValidator(true).isValid(Collections.singletonMap("k", "v"), null));
        // 边界：空 Map 不通过（notEmpty）
        Assertions.assertFalse(newValidator(true).isValid(Collections.emptyMap(), null));
    }

    @Test
    public void validate_array() {
        // 正例：非空数组通过
        Assertions.assertTrue(newValidator(true).isValid(new int[]{1, 2}, null));
        Assertions.assertTrue(newValidator(true).isValid(new Object[]{"a"}, null));
        // 边界：空数组不通过（notEmpty）
        Assertions.assertFalse(newValidator(true).isValid(new int[0], null));
        Assertions.assertFalse(newValidator(true).isValid(new Object[0], null));
    }

    @Test
    public void validate_otherObject() {
        // 正例：其他对象非 null 即通过
        val value = new Object();
        Assertions.assertTrue(newValidator(true).isValid(value, null));
        Assertions.assertTrue(newValidator(true).isValid(42, null));
        Assertions.assertTrue(newValidator(true).isValid(true, null));
    }

    private CSchemaValidator newValidator(boolean required) {
        val validator = new CSchemaValidator();
        validator.initialize(cSchema(required));
        return validator;
    }

}
