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
 * Description: 枚举 text 展示集成测试：实现 ICText 的枚举在接口文档中显示为「枚举名(text)」，
 * 覆盖 model 字段枚举与 query 参数枚举
 * </p>
 *
 * <ul>
 *   <li>model 字段枚举：/v2/api-docs definitions 中 CTextEnumTestDTO.properties.header.enum
 *       含 AUTHORIZATION(鉴权)，不含裸 AUTHORIZATION</li>
 *   <li>query 参数枚举：/c-text-enum/query 的 header 参数 enum 含 AUTHORIZATION(鉴权)</li>
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
     * model 字段枚举：允许值显示为「枚举名(text)」，不含裸枚举名
     * <p>对应测试用例 1.1</p>
     */
    @Test
    public void modelFieldEnum_showsText() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode enumNode = root.path("definitions")
            .path("CTextEnumTestDTO")
            .path("properties")
            .path("header")
            .path("enum");
        List<String> values = toStringList(enumNode);
        assertTrue(values.contains("AUTHORIZATION(鉴权)"), "应含 AUTHORIZATION(鉴权)，实际 " + values);
        assertTrue(values.stream().noneMatch(v -> v.equals("AUTHORIZATION")),
            "不应含裸枚举名 AUTHORIZATION，实际 " + values);
    }

    /**
     * model 字段枚举：所有枚举值均为「枚举名(text)」形态
     * <p>对应测试用例 1.2</p>
     */
    @Test
    public void modelFieldEnum_allValuesTextual() throws Exception {
        JsonNode root = readApiDocs();
        JsonNode enumNode = root.path("definitions")
            .path("CTextEnumTestDTO")
            .path("properties")
            .path("header")
            .path("enum");
        List<String> values = toStringList(enumNode);
        assertEquals(com.c332030.ctool4j.web.enums.CRequestHeaderEnum.values().length, values.size(),
            "枚举值数量应等于 CRequestHeaderEnum 常量数");
        values.forEach(v -> assertTrue(v.matches("^[A-Z_]+[(].+[)]$"),
            "值应为 枚举名(text) 形态，实际 " + v));
    }

    /**
     * query 参数枚举：允许值显示为「枚举名(text)」
     * <p>对应测试用例 2.1</p>
     */
    @Test
    public void queryParamEnum_showsText() throws Exception {
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
                assertTrue(values.contains("AUTHORIZATION(鉴权)"),
                    "应含 AUTHORIZATION(鉴权)，实际 " + values);
                assertTrue(values.stream().noneMatch(v -> v.equals("AUTHORIZATION")),
                    "不应含裸枚举名 AUTHORIZATION，实际 " + values);
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
