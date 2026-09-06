package com.c332030.ctool4j.doc.openapi2.test;

import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * <p>
 * Description: 枚举 text 展示集成测试：实现 ICText 的枚举在接口文档中允许值保持可提交的「枚举名」，
 * 可读的「枚举名(text)」写入 description，覆盖 model 字段枚举与 query 参数枚举
 * </p>
 *
 * <ul>
 *   <li>model 字段枚举：/v2/api-docs definitions 中 CTextEnumTestDTO.properties.header 的 enum
 *       含裸 AUTHORIZATION（可提交），description 含 AUTHORIZATION(鉴权)</li>
 *   <li>query 参数枚举：/c-text-enum/query 的 header 参数 enum 含 AUTHORIZATION，description 含 AUTHORIZATION(鉴权)</li>
 * </ul>
 *
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumIntegrationTests.adoc"
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CTextEnumIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * model 字段枚举：允许值保持可提交的「枚举名」，不含 text 形式
     * <p>对应测试用例 1.1</p>
     */
    @Test
    public void modelFieldEnum_keepCallableValue() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode enumNode = root.path("definitions")
            .path("CTextEnumTestDTO")
            .path("properties")
            .path("header")
            .path("enum");
        List<String> values = toStringList(enumNode);
        assertTrue(values.contains("AUTHORIZATION"), "应含可提交枚举名 AUTHORIZATION，实际 " + values);
        assertTrue(values.stream().noneMatch(v -> v.contains("(")),
            "允许值不应含 text 形式（应保持可提交枚举名），实际 " + values);
    }

    /**
     * model 字段枚举：description 展示可读的「枚举名(text)」
     * <p>对应测试用例 1.2</p>
     */
    @Test
    public void modelFieldEnum_showsTextInDescription() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode description = root.path("definitions")
            .path("CTextEnumTestDTO")
            .path("properties")
            .path("header")
            .path("description");
        assertTrue(description.asText().contains("AUTHORIZATION(鉴权)"),
            "description 应含 AUTHORIZATION(鉴权)，实际 " + description.asText());
    }

    /**
     * model 字段枚举：已标注 @CSchema 自定义描述时不被 text 说明覆盖
     * <p>对应测试用例 1.4</p>
     */
    @Test
    public void modelFieldEnum_keepCSchemaDescription() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode description = root.path("definitions")
            .path("CTextEnumTestDTO")
            .path("properties")
            .path("headerWithSchema")
            .path("description");
        assertEquals("带描述枚举头", description.asText(),
            "@CSchema 自定义描述应保留，不被 text 说明覆盖，实际 " + description.asText());
    }

    /**
     * model 字段枚举：所有允许值均为裸枚举名（数量与常量一致），text 说明完整
     * <p>对应测试用例 1.3</p>
     */
    @Test
    public void modelFieldEnum_allValuesCallableAndTextual() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode properties = root.path("definitions").path("CTextEnumTestDTO").path("properties").path("header");
        List<String> values = toStringList(properties.path("enum"));
        assertEquals(com.c332030.ctool4j.web.enums.CRequestHeaderEnum.values().length, values.size(),
            "允许值数量应等于 CRequestHeaderEnum 常量数");
        values.forEach(v -> assertTrue(v.matches("^[A-Z_]+$"), "允许值应为裸枚举名，实际 " + v));

        String description = properties.path("description").asText();
        for (String value : values) {
            assertTrue(description.contains("(") && description.contains(")"),
                "description 应含「枚举名(text)」形态说明，实际 " + description);
        }
    }

    /**
     * query 参数枚举：允许值保持可提交的「枚举名」，description 展示「枚举名(text)」
     * <p>对应测试用例 2.1</p>
     */
    @Test
    public void queryParamEnum_callableValueAndTextDescription() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode parameters = root.path("paths")
            .path("/c-text-enum/query")
            .path("post")
            .path("parameters");
        boolean found = false;
        for (JsonNode p : parameters) {
            if ("header".equals(p.path("name").asText())) {
                found = true;
                List<String> values = toStringList(p.path("enum"));
                assertTrue(values.contains("AUTHORIZATION"),
                    "应含可提交枚举名 AUTHORIZATION，实际 " + values);
                assertTrue(values.stream().noneMatch(v -> v.contains("(")),
                    "允许值不应含 text 形式，实际 " + values);
                assertTrue(p.path("description").asText().contains("AUTHORIZATION(鉴权)"),
                    "description 应含 AUTHORIZATION(鉴权)，实际 " + p.path("description").asText());
            }
        }
        assertTrue(found, "应找到 header query 参数");
    }

    private JsonNode readApiDocs() throws Exception {
        String body = mockMvc.perform(get("/v2/api-docs"))
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private List<String> toStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> list.add(item.asText()));
        }
        return list;
    }

}
