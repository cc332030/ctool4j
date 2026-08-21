package com.c332030.ctool4j.web.test.validation;

import com.c332030.ctool4j.web.validation.annotation.CSchema;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Description: 标准 Bean Validation 注解（@NotNull/@NotBlank/@NotEmpty）生效验证
 * </p>
 *
 * <p>
 * 目的：即使不使用 @CSchema，标准校验注解也应正常生效——真实校验触发 violation，
 * 且 message 经 getMessage()（即异常处理器使用的 getDefaultMessage()）正确返回，
 * 与 @CSchema 的处理逻辑一致（都由约束注解的 message 提供完整提示，处理器不拼字段名）。
 * </p>
 *
 * @author c332030
 */
public class StandardConstraintValidatorTests {

    private static Validator validator;

    @BeforeAll
    public static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * @NotNull 生效：null → violation，message 非空
     */
    @Test
    public void notNull() {
        Set<ConstraintViolation<NotNullBean>> violations = validator.validate(new NotNullBean());
        Assertions.assertFalse(violations.isEmpty(), "@NotNull 字段为 null 应有校验错误");
        Assertions.assertTrue(violations.stream().anyMatch(v ->
                "name".equals(v.getPropertyPath().toString()) && v.getMessage() != null && !v.getMessage().isEmpty()),
            "@NotNull 应产生带 message 的校验错误");
    }

    /**
     * @NotNull 生效：非 null → 通过
     */
    @Test
    public void notNull_valid() {
        NotNullBean bean = new NotNullBean();
        bean.setName("x");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "@NotNull 字段非 null 应通过");
    }

    /**
     * @NotBlank 生效：空白字符串 → violation
     */
    @Test
    public void notBlank() {
        NotBlankBean blank = new NotBlankBean();
        blank.setName("   ");
        Assertions.assertFalse(validator.validate(blank).isEmpty(), "@NotBlank 空白字符串应有校验错误");

        NotBlankBean empty = new NotBlankBean();
        empty.setName("");
        Assertions.assertFalse(validator.validate(empty).isEmpty(), "@NotBlank 空字符串应有校验错误");
    }

    /**
     * @NotBlank 生效：非空白 → 通过
     */
    @Test
    public void notBlank_valid() {
        NotBlankBean bean = new NotBlankBean();
        bean.setName("x");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "@NotBlank 非空白应通过");
    }

    /**
     * @NotEmpty 生效：空集合 → violation
     */
    @Test
    public void notEmpty() {
        NotEmptyBean empty = new NotEmptyBean();
        empty.setTags(Collections.emptyList());
        Assertions.assertFalse(validator.validate(empty).isEmpty(), "@NotEmpty 空集合应有校验错误");
    }

    /**
     * @NotEmpty 生效：非空集合 → 通过
     */
    @Test
    public void notEmpty_valid() {
        NotEmptyBean bean = new NotEmptyBean();
        bean.setTags(Collections.singletonList("a"));
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "@NotEmpty 非空集合应通过");
    }

    /**
     * 标准注解与 @CSchema 同 bean 共存：message 处理逻辑一致（都由约束注解 message 提供，经 getMessage 返回）
     */
    @Test
    public void standardAndCSchema_coexist() {
        MixedBean bean = new MixedBean();
        // 两个必填字段都缺失
        Set<ConstraintViolation<MixedBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.stream().anyMatch(
            v -> "code".equals(v.getPropertyPath().toString())),
            "@NotNull 字段应校验");
        Assertions.assertTrue(violations.stream().anyMatch(
            v -> "username".equals(v.getPropertyPath().toString()) && "不能为空".equals(v.getMessage())),
            "@CSchema 字段应校验且返回默认 message（字段描述前缀由异常处理器拼接）");
    }

    /**
     * 测试用 bean：@NotNull 字段
     */
    @Getter
    @Setter
    private static class NotNullBean {

        @NotNull
        private String name;

    }

    /**
     * 测试用 bean：@NotBlank 字段
     */
    @Getter
    @Setter
    private static class NotBlankBean {

        @NotBlank
        private String name;

    }

    /**
     * 测试用 bean：@NotEmpty 字段
     */
    @Getter
    @Setter
    private static class NotEmptyBean {

        @NotEmpty
        private List<String> tags;

    }

    /**
     * 测试用 bean：标准注解 + @CSchema 共存
     */
    @Getter
    @Setter
    private static class MixedBean {

        @NotNull
        private String code;

        @CSchema(value = "用户名", required = true)
        private String username;

    }

}
