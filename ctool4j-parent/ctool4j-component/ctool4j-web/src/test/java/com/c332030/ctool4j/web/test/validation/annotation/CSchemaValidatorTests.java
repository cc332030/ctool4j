package com.c332030.ctool4j.web.test.validation.annotation;

import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import com.c332030.ctool4j.web.validation.annotation.CSchema;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Description: CSchemaValidator 按类型自动校验逻辑测试：启动 Spring 容器（@CTool4jSpringBootTest），
 * 经容器注入的真实 {@link Validator} 完整校验（验证 required/message 等属性生效），并通过 MockMvc
 * 走真实接口（@Valid @RequestBody）验证必填/非必填生效，贴近真实使用场景
 * </p>
 *
 * <p>
 * 覆盖：required=false 跳过校验、required=true 时 null/字符串（notBlank）/集合（notEmpty）/
 * Map（notEmpty）/数组（notEmpty）/其他对象（notNull），以及 message 自定义提示
 * </p>
 *
 * @author c332030
 * @see doc/design/web/CSchemaValidatorTests.adoc
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CSchemaValidatorTests {

    @Autowired
    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    /**
     * 必填字符串：缺失（null）→ 校验失败，返回默认 message（对应测试用例 1.1）
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
     * 必填字符串：空白 → 校验失败（notBlank）（对应测试用例 1.2）
     */
    @Test
    public void validate_stringBlank() {
        StringBean bean = new StringBean();
        bean.setUsername("   ");
        Assertions.assertFalse(validator.validate(bean).isEmpty(), "必填字符串空白应有校验错误");
    }

    /**
     * 必填字符串：非空 → 通过（对应测试用例 1.3）
     */
    @Test
    public void validate_stringValid() {
        StringBean bean = new StringBean();
        bean.setUsername("c332030");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "必填字符串非空应通过");
    }

    /**
     * 自定义 message：校验失败时使用注解声明的 message（对应测试用例 1.4）
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
     * 非必填字段：null/空白均通过（对应测试用例 1.5）
     */
    @Test
    public void validate_notRequired() {
        OptionalBean bean = new OptionalBean();
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "非必填字段缺失应通过");
        bean.setRemark("  ");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "非必填字段空白应通过");
    }

    /**
     * 集合必填：空集合校验失败、非空通过（对应测试用例 1.6）
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
     * Map 必填：空 Map 校验失败、非空通过（对应测试用例 1.7）
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
     * 数组必填：空数组校验失败、非空通过（对应测试用例 1.8）
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
     * 其他对象必填：null 校验失败、非 null 通过（对应测试用例 1.9）
     */
    @Test
    public void validate_object() {
        Assertions.assertFalse(validator.validate(new ObjectBean()).isEmpty(), "payload 为 null 应校验失败");

        ObjectBean ok = new ObjectBean();
        ok.setPayload(new Object());
        Assertions.assertTrue(validator.validate(ok).isEmpty(), "payload 非 null 应通过");
    }

    /**
     * groups 兼容：指定分组时才校验，默认组不校验（对应测试用例 1.10）
     */
    @Test
    public void validate_groups() {
        GroupsBean bean = new GroupsBean();

        // 默认组：username 不在默认组，不校验
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "默认组不应校验 UpdateGroup 分组的字段");

        // UpdateGroup 分组：username 缺失，校验失败
        Assertions.assertFalse(
            validator.validate(bean, UpdateGroup.class).isEmpty(),
            "UpdateGroup 分组应校验该必填字段");
    }

    /**
     * groups 兼容：提供值后，指定分组校验通过（正例）（对应测试用例 1.11）
     */
    @Test
    public void validate_groups_valid() {
        GroupsBean bean = new GroupsBean();
        bean.setUsername("c332030");
        Assertions.assertTrue(
            validator.validate(bean, UpdateGroup.class).isEmpty(),
            "UpdateGroup 分组提供值应通过");
    }

    /**
     * groups 兼容：其他分组（CreateGroup）校验时不触发 UpdateGroup 字段（反例，分组隔离）（对应测试用例 1.12）
     */
    @Test
    public void validate_groups_otherGroup() {
        GroupsBean bean = new GroupsBean();
        // username 缺失，但用 CreateGroup 分组校验，不属于该分组的字段不校验
        Assertions.assertTrue(
            validator.validate(bean, CreateGroup.class).isEmpty(),
            "CreateGroup 分组不应校验 UpdateGroup 分组的字段");
    }

    /**
     * payload 兼容：校验失败时 violation 携带自定义 payload（反例）（对应测试用例 1.13）
     */
    @Test
    public void validate_payload() {
        Set<ConstraintViolation<PayloadBean>> violations = validator.validate(new PayloadBean());
        Assertions.assertFalse(violations.isEmpty(), "必填字段缺失应有校验错误");
        Assertions.assertTrue(violations.stream().anyMatch(v ->
                v.getConstraintDescriptor().getPayload().contains(MyPayload.class)),
            "校验错误应携带自定义 payload");
    }

    /**
     * payload 兼容：提供值后校验通过，无 violation（正例）（对应测试用例 1.14）
     */
    @Test
    public void validate_payload_valid() {
        PayloadBean bean = new PayloadBean();
        bean.setCode("E100");
        Assertions.assertTrue(validator.validate(bean).isEmpty(), "payload 字段提供值应通过");
    }

    /**
     * 接口必填生效：username 缺失 → 校验失败（HTTP 200 + body code=500，ctool4j 异常约定）（对应测试用例 2.1）
     */
    @Test
    public void requiredField_missing() throws Exception {
        mockMvc.perform(post("/c-schema-validator/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("username 不能为空"));
    }

    /**
     * 接口必填生效：username 空白 → 校验失败（notBlank，HTTP 200 + body code=500）（对应测试用例 2.2）
     */
    @Test
    public void requiredField_blank() throws Exception {
        mockMvc.perform(post("/c-schema-validator/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"   \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("username 不能为空"));
    }

    /**
     * 接口非必填生效：username 必填提供，非必填字段（remark/other）缺失 → 200（对应测试用例 2.3）
     */
    @Test
    public void optionalField_missing() throws Exception {
        mockMvc.perform(post("/c-schema-validator/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"c332030\"}"))
            .andExpect(status().isOk());
    }

    /**
     * 接口正常：必填 + 非必填均提供 → 200（对应测试用例 2.4）
     */
    @Test
    public void allFields_present() throws Exception {
        mockMvc.perform(post("/c-schema-validator/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"c332030\",\"remark\":\"备注\",\"other\":\"x\"}"))
            .andExpect(status().isOk());
    }

    /**
     * 分组接口：创建组
     */
    private interface CreateGroup {}

    /**
     * 分组接口：更新组
     */
    private interface UpdateGroup {}

    /**
     * 自定义载荷
     */
    private static class MyPayload implements Payload {}

    /**
     * 测试用 bean：分组必填字段
     */
    @Getter
    @Setter
    private static class GroupsBean {

        @CSchema(value = "用户名", required = true, groups = UpdateGroup.class)
        private String username;

    }

    /**
     * 测试用 bean：自定义载荷字段
     */
    @Getter
    @Setter
    private static class PayloadBean {

        @CSchema(value = "编码", required = true, payload = MyPayload.class)
        private String code;

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
