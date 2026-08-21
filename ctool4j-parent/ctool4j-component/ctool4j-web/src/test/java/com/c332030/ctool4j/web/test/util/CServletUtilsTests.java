package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.util.CServletUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CServletUtilsTests
 * </p>
 *
 * <p>覆盖 writeJson 两个重载：状态码、content-type、写入内容、字符集</p>
 *
 * @since 2026/8/14
 */

@CustomLog
public class CServletUtilsTests {

    // ---------- writeJson(String) ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void writeJson_string() throws Exception {
        // 正例：写入 JSON 字符串 + 状态码 + content-type
        val response = new MockHttpServletResponse();
        CServletUtils.writeJson(response, HttpStatus.OK, "{\"a\":1}");
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertTrue(response.getContentType().contains("application/json"));
        Assertions.assertEquals(StandardCharsets.UTF_8.name(), response.getCharacterEncoding());
        Assertions.assertEquals("{\"a\":1}", response.getContentAsString());
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void writeJson_string_errorStatus() {
        // 正例：错误状态码
        val response = new MockHttpServletResponse();
        CServletUtils.writeJson(response, HttpStatus.INTERNAL_SERVER_ERROR, "{}");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void writeJson_string_emptyJson() throws Exception {
        // 边界：空 JSON 字符串写入
        val response = new MockHttpServletResponse();
        CServletUtils.writeJson(response, HttpStatus.OK, "");
        Assertions.assertEquals("", response.getContentAsString());
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void writeJson_string_null() {
        // 反例：null jsonBody 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CServletUtils.writeJson(new MockHttpServletResponse(), HttpStatus.OK, null)
        );
    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void writeJson_string_nullResponse() {
        // 异常路径：null response 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CServletUtils.writeJson(null, HttpStatus.OK, "{}")
        );
    }

    // ---------- writeJson(Object) ----------

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void writeJson_object() throws Exception {
        // 正例：Object body 序列化为 JSON 写入
        val response = new MockHttpServletResponse();
        val body = new LinkedHashMap<String, Object>();
        body.put("name", "tom");
        body.put("age", 18);
        CServletUtils.writeJson(response, HttpStatus.OK, body);
        val content = response.getContentAsString();
        Assertions.assertTrue(content.contains("tom"));
        Assertions.assertTrue(content.contains("18"));
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    public void writeJson_object_map() throws Exception {
        // 正例：Map body
        val response = new MockHttpServletResponse();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", 1);
        CServletUtils.writeJson(response, HttpStatus.CREATED, body);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("1"));
    }

    /**
     * 对应测试用例 1.8
     */
    @Test
    public void writeJson_object_null() {
        // 反例：null body 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CServletUtils.writeJson(new MockHttpServletResponse(), HttpStatus.OK, null)
        );
    }

}
