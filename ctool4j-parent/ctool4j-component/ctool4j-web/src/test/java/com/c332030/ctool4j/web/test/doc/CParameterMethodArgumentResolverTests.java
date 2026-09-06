package com.c332030.ctool4j.web.test.doc;

import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Description: CParameterMethodArgumentResolver 集成测试：仅标注 @CParameter（不标 @RequestParam）时，
 * required 同时驱动 SpringMVC 绑定（必填缺参报错、非必填放行），默认值与 @RequestParam 一致
 * </p>
 *
 * <p>
 * 是 {@code CParameterMethodArgumentResolver} 的测试用例（对应测试文档
 * <code>doc/design/web/CParameterMethodArgumentResolverTests.adoc</code>）。
 * </p>
 *
 * @author c332030
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CParameterMethodArgumentResolverTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * required=true 缺参：报错（缺参异常经兜底返回 200 + code=500，ctool4j 异常约定）
 * <p>
 * 对应测试用例 1.1
 */
    @Test
    public void requiredTrue_missing() throws Exception {
        mockMvc.perform(get("/c-parameter/required-true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"));
    }

    /**
     * required=true 携带参数：正常返回
 * <p>
 * 对应测试用例 1.2
 */
    @Test
    public void requiredTrue_present() throws Exception {
        mockMvc.perform(get("/c-parameter/required-true").param("name", "c332030"))
            .andExpect(status().isOk())
            .andExpect(content().string("c332030"));
    }

    /**
     * required=false 缺参：放行返回 null（200 + 空 body）
 * <p>
 * 对应测试用例 2.1
 */
    @Test
    public void requiredFalse_missing() throws Exception {
        mockMvc.perform(get("/c-parameter/required-false"))
            .andExpect(status().isOk());
    }

    /**
     * required=false 携带参数：正常返回
 * <p>
 * 对应测试用例 2.2
 */
    @Test
    public void requiredFalse_present() throws Exception {
        mockMvc.perform(get("/c-parameter/required-false").param("name", "c332030"))
            .andExpect(status().isOk())
            .andExpect(content().string("c332030"));
    }

    /**
     * 不写 required（默认 true）：缺参应报错（与 @RequestParam.required 默认一致）
 * <p>
 * 对应测试用例 3.1
 */
    @Test
    public void requiredDefault_missing() throws Exception {
        mockMvc.perform(get("/c-parameter/required-default"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"));
    }

    /**
     * 不写 required（默认 true）携带参数：正常返回
 * <p>
 * 对应测试用例 3.2
 */
    @Test
    public void requiredDefault_present() throws Exception {
        mockMvc.perform(get("/c-parameter/required-default").param("name", "c332030"))
            .andExpect(status().isOk())
            .andExpect(content().string("c332030"));
    }

    /**
     * 已有 @RequestParam(required=false)：@CParameter 仅作文档，由 @RequestParam 控制绑定（缺参放行）
 * <p>
 * 对应测试用例 4.1
 */
    @Test
    public void withRequestParam_false_missing() throws Exception {
        mockMvc.perform(get("/c-parameter/with-request-param"))
            .andExpect(status().isOk());
    }

}
