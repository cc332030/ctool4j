package com.c332030.ctool4j.web.test.validation.annotation;

import com.c332030.ctool4j.web.validation.annotation.CSchema;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Description: CSchemaValidator 按类型自动校验逻辑测试（经 hibernate Validator 完整校验，
 * 验证 required/message 等属性生效）
 * </p>
 *
 * <p>
 * 覆盖：required=false 跳过校验、required=true 时 null/字符串（notBlank）/集合（notEmpty）/
 * Map（notEmpty）/数组（notEmpty）/其他对象（notNull），以及 message 自定义提示
 * </p>
 *
 * @author c332030
 */
public class CSchemaValidatorTests {

    private static Validator validator;

    @BeforeAll
    public static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 必填字符串：缺失（null）→ 校验失败，返回默认 message
     */
    @Test
    public void validate_stringRequired() {
        Set<ConstraintViolation<StringBean>> violations = validator.validate(new StringBean());
        Assertions.assertFalse(violations.isEmpty(), "必填字符串缺失应有校验错误");
        Assertions.assertTrue(violations.stream().anyMatch(v ->
                "username".equals(v.getPropertyPath().toString()) && "不能为空".equals(v.getMessage())),
            "username 缺失应报默认 message=不能为空（字段描述前缀由异常处理器拼接）");
    }

    /**
     * 必填字符串：空白 → 校验失败（notBlank）
     */
    @Test
    public void validate_stringBlank() {
        StringBean bean = new StringBean();
        bean.setUsername("   ");
        Assertions.assertFalse(validator.validate(bean).isEmpty(), "必填字符串空白应有校验错误");
    }

    /**
     * 必填字符串：非空 → 通过
     */
    @Test
    public void validate_stringValid() {
        StringBean bean = new StringBean();
        bean.setUsername("c332030");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "必填字符串非空应通过");
    }

    /**
     * 自定义 message：校验失败时使用注解声明的 message
     */
    @Test
    public void validate_customMessage() {
        Set<ConstraintViolation<CustomMessageBean>> violations =
            validator.validate(new CustomMessageBean());
        Assertions.assertFalse(violations.isEmpty(), "必填字段缺失应有校验错误");
        Assertions.assertTrue(violations.stream().anyMatch(v ->
                "age".equals(v.getPropertyPath().toString()) && "年龄不能为空".equals(v.getMessage())),
            "应使用自定义 message=年龄不能为空");
    }

    /**
     * 非必填字段：null/空白均通过
     */
    @Test
    public void validate_notRequired() {
        OptionalBean bean = new OptionalBean();
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "非必填字段缺失应通过");
        bean.setRemark("  ");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "非必填字段空白应通过");
    }

    /**
     * 集合必填：空集合校验失败、非空通过
     */
    @Test
    public void validate_collection() {
        CollectionBean empty = new CollectionBean();
        empty.setTags(Collections.emptyList());
        Assertions.assertFalse(validator.validate(empty).isEmpty(), "空集合应校验失败");

        CollectionBean ok = new CollectionBean();
        ok.setTags(Collections.singletonList("a"));
        Assertions.assertTrue(validator.validate(ok).isEmpty(), "非空集合应通过");
    }

    /**
     * Map 必填：空 Map 校验失败、非空通过
     */
    @Test
    public void validate_map() {
        MapBean empty = new MapBean();
        empty.setExt(Collections.emptyMap());
        Assertions.assertFalse(validator.validate(empty).isEmpty(), "空 Map 应校验失败");

        MapBean ok = new MapBean();
        ok.setExt(Collections.singletonMap("k", "v"));
        Assertions.assertTrue(validator.validate(ok).isEmpty(), "非空 Map 应通过");
    }

    /**
     * 数组必填：空数组校验失败、非空通过
     */
    @Test
    public void validate_array() {
        ArrayBean empty = new ArrayBean();
        empty.setNums(new int[0]);
        Assertions.assertFalse(validator.validate(empty).isEmpty(), "空数组应校验失败");

        ArrayBean ok = new ArrayBean();
        ok.setNums(new int[]{1, 2});
        Assertions.assertTrue(validator.validate(ok).isEmpty(), "非空数组应通过");
    }

    /**
     * 其他对象必填：null 校验失败、非 null 通过
     */
    @Test
    public void validate_object() {
        Assertions.assertFalse(validator.validate(new ObjectBean()).isEmpty(), "payload 为 null 应校验失败");

        ObjectBean ok = new ObjectBean();
        ok.setPayload(new Object());
        Assertions.assertTrue(validator.validate(ok).isEmpty(), "payload 非 null 应通过");
    }

    /**
     * 测试用 bean：必填字符串
     */
    @Getter
    @Setter
    private static class StringBean {

        @CSchema(value = "用户名", required = true)
        private String username;

    }

    /**
     * 测试用 bean：必填集合
     */
    @Getter
    @Setter
    private static class CollectionBean {

        @CSchema(required = true)
        private List<String> tags;

    }

    /**
     * 测试用 bean：必填 Map
     */
    @Getter
    @Setter
    private static class MapBean {

        @CSchema(required = true)
        private Map<String, Object> ext;

    }

    /**
     * 测试用 bean：必填数组
     */
    @Getter
    @Setter
    private static class ArrayBean {

        @CSchema(required = true)
        private int[] nums;

    }

    /**
     * 测试用 bean：必填其他对象
     */
    @Getter
    @Setter
    private static class ObjectBean {

        @CSchema(required = true)
        private Object payload;

    }

    /**
     * 测试用 bean：自定义 message
     */
    @Getter
    @Setter
    private static class CustomMessageBean {

        @CSchema(value = "年龄", required = true, message = "年龄不能为空")
        private Integer age;

    }

    /**
     * 测试用 bean：非必填字段
     */
    @Getter
    @Setter
    private static class OptionalBean {

        @CSchema("备注")
        private String remark;

    }

}
