package com.c332030.ctool4j.doc.openapi2.test;

import com.c332030.ctool4j.doc.openapi2.test.model.CSchemaTestDTO;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.Validator;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Description: CSchema 集成测试：验证接口必填/非必填生效、字段文档生效、字段必填生效
 * </p>
 *
 * <ul>
 *   <li>接口必填/非必填生效：MockMvc 调 {@code /c-schema/test}，@CSchema(required=true) 字段缺失/空白触发校验失败
 *       （HTTP 200 + body code=500，ctool4j 异常约定），非必填字段缺失返回 200</li>
 *   <li>字段必填生效：@RequestBody @Valid 校验 DTO 字段，必填字段缺失/空白时 body code=500</li>
 *   <li>字段文档生效：/v2/api-docs 生成的 model 字段含 description 与 required</li>
 * </ul>
 *
 * @author c332030
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CSchemaIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Validator validator;

    /**
     * 字段必填生效：@CSchema 约束被 Spring 的 LocalValidatorFactoryBean 识别（username 缺失产生校验错误）
     */
    @Test
    public void directValidator() {
        CSchemaTestDTO dto = new CSchemaTestDTO();
        Assertions.assertFalse(validator.validate(dto).isEmpty(), "username 缺失应有校验错误");
    }

    /**
     * 必填字段生效：username 缺失 → 校验失败（HTTP 200 + body code=500）
     */
    @Test
    public void requiredField_missing() throws Exception {
        mockMvc.perform(post("/c-schema/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("username 不能为空"));
    }

    /**
     * 必填字段生效：username 空白 → 校验失败（notBlank，HTTP 200 + body code=500）
     */
    @Test
    public void requiredField_blank() throws Exception {
        mockMvc.perform(post("/c-schema/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"   \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("username 不能为空"));
    }

    /**
     * 非必填字段生效：username 必填提供，非必填字段（remark/other）缺失 → 200
     */
    @Test
    public void optionalField_missing() throws Exception {
        mockMvc.perform(post("/c-schema/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"c332030\"}"))
            .andExpect(status().isOk());
    }

    /**
     * 接口正常：必填 + 非必填均提供 → 200
     */
    @Test
    public void allFields_present() throws Exception {
        mockMvc.perform(post("/c-schema/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"c332030\",\"remark\":\"备注\",\"other\":\"x\"}"))
            .andExpect(status().isOk());
    }

    /**
     * 字段文档生效：/v2/api-docs 中 CSchemaTestDTO 的 username 含描述且进入必填列表、remark 含描述且非必填
     *
     * <p>说明：springfox 生成 JSON 中中文字符以 Unicode 转义（形如 uXXXX），故用 jsonPath 断言（解析后对比），
     * 不用字符串 contains（无法匹配转义后的中文）。</p>
     */
    @Test
    public void fieldDocumentation() throws Exception {
        mockMvc.perform(get("/v2/api-docs"))
            .andExpect(status().isOk())
            // 必填字段进入 required 数组
            .andExpect(jsonPath("$.definitions.CSchemaTestDTO.required").isArray())
            .andExpect(jsonPath("$.definitions.CSchemaTestDTO.required", hasItem("username")))
            // 字段描述
            .andExpect(jsonPath("$.definitions.CSchemaTestDTO.properties.username.description").value("用户名"))
            .andExpect(jsonPath("$.definitions.CSchemaTestDTO.properties.remark.description").value("备注"));
    }

}
