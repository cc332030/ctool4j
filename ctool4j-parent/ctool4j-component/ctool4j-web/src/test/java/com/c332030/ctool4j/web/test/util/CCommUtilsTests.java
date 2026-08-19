package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import com.c332030.ctool4j.web.model.CRequestLog;
import com.c332030.ctool4j.web.util.CCommUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CCommUtilsTests
 * </p>
 *
 * <p>覆盖容易出错或出错后难发现的方法：headers 拼接、URL/Query 拼接、charset 解析、完整 HTTP 日志拼接等</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CCommUtilsTests {

    /**
     * 构造一个可复用的 IHttpLogInfo 实现，复用 CRequestLog（@Data 提供 public setter）
     */
    private CRequestLog requestLog() {
        val log = new CRequestLog();
        log.setMethod(HttpMethod.GET.name());
        log.setPath("/api/test");
        return log;
    }

    private Map<String, Collection<String>> headers(String key, String value) {
        val headers = new LinkedHashMap<String, Collection<String>>();
        headers.put(key, Collections.singletonList(value));
        return headers;
    }

    // ---------- contextTypeForm ----------

    @Test
    public void contextTypeForm() {
        // 正例：设置表单 content-type
        val headers = new HttpHeaders();
        CCommUtils.contextTypeForm(headers);
        Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
    }

    @Test
    public void contextTypeForm_nullHeaders_throws() {
        // 异常路径：null headers 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.contextTypeForm(null)
        );
    }

    // ---------- contextTypeJson ----------

    @Test
    public void contextTypeJson() {
        // 正例：设置 json content-type
        val headers = new HttpHeaders();
        CCommUtils.contextTypeJson(headers);
        Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    @Test
    public void contextTypeJson_nullHeaders_throws() {
        // 异常路径：null headers 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.contextTypeJson(null)
        );
    }

    // ---------- acceptJson ----------

    @Test
    public void acceptJson() {
        // 正例：设置 Accept: application/json
        val headers = new HttpHeaders();
        CCommUtils.acceptJson(headers);
        Assertions.assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
    }

    @Test
    public void acceptJson_nullHeaders_throws() {
        // 异常路径：null headers 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.acceptJson(null)
        );
    }

    // ---------- getFullHttp ----------

    @Test
    public void getFullHttp() {
        // 正例：method + url + headers + requestBody + responseBody 完整拼接
        val headers = headers("Content-Type", "application/json");
        val result = CCommUtils.getFullHttp(
            HttpMethod.POST, "/api", headers, "{\"a\":1}", "{\"b\":2}"
        );
        Assertions.assertTrue(result.startsWith("POST /api"));
        Assertions.assertTrue(result.contains("Content-Type: application/json"));
        Assertions.assertTrue(result.contains("{\"a\":1}"));
        Assertions.assertTrue(result.contains("{\"b\":2}"));
    }

    @Test
    public void getFullHttp_noHeader_noBody() {
        // 正例：无 headers、无 body 时仅输出请求行
        val result = CCommUtils.getFullHttp(HttpMethod.GET, "/api", null, null, null);
        Assertions.assertEquals("GET /api", result);
    }

    @Test
    public void getFullHttp_nullMethod() {
        // 边界：method 为 null 时 toString 输出 "null"
        val result = CCommUtils.getFullHttp(null, "/api", null, null, null);
        Assertions.assertEquals("null /api", result);
    }

    // ---------- getFullHeaderStr ----------

    @Test
    public void getFullHeaderStr() {
        // 正例：多个 header 以换行连接
        val map = new LinkedHashMap<String, Collection<String>>();
        map.put("A", Collections.singletonList("1"));
        map.put("B", Collections.singletonList("2"));
        Assertions.assertEquals("A: 1\nB: 2", CCommUtils.getFullHeaderStr(map));
    }

    @Test
    public void getFullHeaderStr_null_returnsNull() {
        // 边界：null/空 map 返回 null
        Assertions.assertNull(CCommUtils.getFullHeaderStr(null));
        Assertions.assertNull(CCommUtils.getFullHeaderStr(Collections.emptyMap()));
    }

    @Test
    public void getFullHeaderStr_multiValue() {
        // 边界：同一 header 多个值用逗号连接
        val map = new LinkedHashMap<String, Collection<String>>();
        map.put("A", java.util.Arrays.asList("1", "2"));
        Assertions.assertEquals("A: 1,2", CCommUtils.getFullHeaderStr(map));
    }

    @Test
    public void getFullHeaderStr_withPredicate() {
        // 正例：predicate 过滤 header
        val map = new LinkedHashMap<String, Collection<String>>();
        map.put("A", Collections.singletonList("1"));
        map.put("Secret", Collections.singletonList("x"));
        val result = CCommUtils.getFullHeaderStr(
            map, e -> !"Secret".equals(e.getKey())
        );
        Assertions.assertEquals("A: 1", result);
    }

    @Test
    public void getFullHeaderStr_predicateAllReject_returnsEmptyString() {
        // 边界：predicate 全部拒绝时返回空串（非 null）
        val map = headers("A", "1");
        Assertions.assertEquals("", CCommUtils.getFullHeaderStr(map, e -> false));
    }

    // ---------- isTextBody ----------

    @Test
    public void isTextBody_json() {
        // 正例：application/json 视为文本 body
        val map = headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Assertions.assertTrue(CCommUtils.isTextBody(map));
    }

    @Test
    public void isTextBody_binary() {
        // 反例：application/octet-stream 非文本
        val map = headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        Assertions.assertFalse(CCommUtils.isTextBody(map));
    }

    @Test
    public void isTextBody_emptyHeaders() {
        // 边界：无 Content-Type 时按非文本处理
        Assertions.assertFalse(CCommUtils.isTextBody(Collections.emptyMap()));
    }

    @Test
    public void isTextBody_nullHeaders_returnsFalse() {
        // 边界：null headers 按无 Content-Type 处理，返回 false
        Assertions.assertFalse(CCommUtils.isTextBody(null));
    }

    // ---------- getCharsetOrDefault ----------

    @Test
    public void getCharsetOrDefault_explicit() {
        // 正例：显式声明 charset 时按声明解析
        val map = headers(HttpHeaders.CONTENT_TYPE, "text/plain;charset=gbk");
        Assertions.assertEquals("GBK", CCommUtils.getCharsetOrDefault(map).name());
    }

    @Test
    public void getCharsetOrDefault_defaultUtf8() {
        // 边界：未声明 charset 时默认 UTF-8
        val map = headers(HttpHeaders.CONTENT_TYPE, "application/json");
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(map));
    }

    @Test
    public void getCharsetOrDefault_invalidContentType() {
        // 边界：无法解析的 Content-Type 忽略并回退 UTF-8
        val map = headers(HttpHeaders.CONTENT_TYPE, "!!!not-a-media-type!!!");
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(map));
    }

    @Test
    public void getCharsetOrDefault_emptyHeaders() {
        // 边界：无 Content-Type 时默认 UTF-8
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(Collections.emptyMap()));
    }

    @Test
    public void getCharsetOrDefault_nullHeaders_returnsUtf8() {
        // 边界：null headers 按无 Content-Type 处理，返回默认 UTF-8
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(null));
    }

    // ---------- appendRequestUrl / appendUrl ----------

    @Test
    public void appendRequestUrl_getWithParams() {
        // 正例：GET 请求拼接 query string
        val log = requestLog();
        Map<String, Collection<String>> params = new java.util.HashMap<>();
        params.put("a", Collections.singletonList("1"));
        params.put("b", Collections.singletonList("2"));
        log.setParams(params);
        val sb = new StringBuilder();
        CCommUtils.appendRequestUrl(sb, log);
        Assertions.assertEquals("GET /api/test?a=1&b=2", sb.toString());
    }

    @Test
    public void appendRequestUrl_getNoParams() {
        // 边界：GET 无 params 时不拼 ?
        val log = requestLog();
        val sb = new StringBuilder();
        CCommUtils.appendRequestUrl(sb, log);
        Assertions.assertEquals("GET /api/test", sb.toString());
    }

    @Test
    public void appendUrl_postParamsNotAppended() {
        // 反例：POST 的 params 不拼到 URL
        val log = requestLog();
        log.setMethod(HttpMethod.POST.name());
        log.setParams(Collections.singletonMap("a", Collections.singletonList("1")));
        val sb = new StringBuilder();
        CCommUtils.appendUrl(sb, log);
        Assertions.assertEquals("/api/test", sb.toString());
    }

    @Test
    public void appendUrl_nullMethod() {
        // 边界：method 为 null/空时不拼接 query
        val log = requestLog();
        log.setMethod(null);
        log.setParams(Collections.singletonMap("a", Collections.singletonList("1")));
        val sb = new StringBuilder();
        CCommUtils.appendUrl(sb, log);
        Assertions.assertEquals("/api/test", sb.toString());
    }

    @Test
    public void appendUrl_multiValueParams() {
        // 边界：同一 key 多个 value 用 & 连接
        val log = requestLog();
        log.setParams(java.util.Collections.singletonMap("a", java.util.Arrays.asList("1", "2")));
        val sb = new StringBuilder();
        CCommUtils.appendUrl(sb, log);
        Assertions.assertEquals("/api/test?a=1&a=2", sb.toString());
    }

    // ---------- appendRequestLine ----------

    @Test
    public void appendRequestLine() {
        // 正例：拼接 METHOD URL
        val sb = new StringBuilder();
        CCommUtils.appendRequestLine(sb, "POST", "/api");
        Assertions.assertEquals("POST /api", sb.toString());
    }

    @Test
    public void appendRequestLine_nullMethod() {
        // 边界：method 为 null 时输出 "null"
        val sb = new StringBuilder();
        CCommUtils.appendRequestLine(sb, null, "/api");
        Assertions.assertEquals("null /api", sb.toString());
    }

    // ---------- appendHeaderLine ----------

    @Test
    public void appendHeaderLine_string() {
        // 正例：拼接 key: value 且前置换行
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", "Value");
        Assertions.assertEquals("start\nKey: Value", sb.toString());
    }

    @Test
    public void appendHeaderLine_emptyValue() {
        // 边界：value 为空时不做任何拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", "");
        Assertions.assertEquals("start", sb.toString());
    }

    @Test
    public void appendHeaderLine_nullValue() {
        // 边界：value 为 null 时不做任何拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", null);
        Assertions.assertEquals("start", sb.toString());
    }

    @Test
    public void appendHeaderLine_enum() {
        // 正例：通过 ICRequestHeader 拼接 header 名
        val sb = new StringBuilder();
        CCommUtils.appendHeaderLine(sb, CRequestHeaderEnum.AUTHORIZATION, "Bearer abc");
        Assertions.assertTrue(sb.toString().contains("Authorization: Bearer abc"));
    }

    // ---------- appendHeaderBlock ----------

    @Test
    public void appendHeaderBlock() {
        // 正例：拼接 header 块
        val map = headers("A", "1");
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderBlock(sb, map);
        Assertions.assertEquals("start\nA: 1", sb.toString());
    }

    @Test
    public void appendHeaderBlock_empty() {
        // 边界：null/空 headers 不拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderBlock(sb, null);
        Assertions.assertEquals("start", sb.toString());
    }

    // ---------- appendBody ----------

    @Test
    public void appendBody_text() {
        // 正例：文本 body 按 charset 解码
        val body = "你好".getBytes(StandardCharsets.UTF_8);
        val map = headers(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
        val sb = new StringBuilder("start");
        CCommUtils.appendBody(sb, body, map);
        Assertions.assertEquals("start\n\n你好", sb.toString());
    }

    @Test
    public void appendBody_binary() {
        // 反例：非文本 body 输出占位符
        val body = new byte[]{(byte) 0xFF, (byte) 0x00};
        val map = headers(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        val sb = new StringBuilder("start");
        CCommUtils.appendBody(sb, body, map);
        Assertions.assertEquals("start\n\n[not text body]", sb.toString());
    }

    @Test
    public void appendBody_emptyBytes() {
        // 边界：空 byte[] 不拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendBody(sb, new byte[0], null);
        CCommUtils.appendBody(sb, null, null);
        Assertions.assertEquals("start", sb.toString());
    }

    // ---------- appendError ----------

    @Test
    public void appendError() {
        // 正例：拼接 error 信息
        val sb = new StringBuilder("start");
        CCommUtils.appendError(sb, "boom");
        Assertions.assertEquals("start\n\nerror: boom", sb.toString());
    }

    @Test
    public void appendError_empty() {
        // 边界：空/null errorMessage 不拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendError(sb, null);
        CCommUtils.appendError(sb, "");
        Assertions.assertEquals("start", sb.toString());
    }

    // ---------- appendHttpLog（完整 HTTP 报文拼接） ----------

    @Test
    public void appendHttpLog_get() {
        // 正例：GET 完整拼接（请求行 + query + header + 业务数据）
        val log = requestLog();
        log.setToken("token-1");
        log.setTraceId("trace-1");
        log.setParams(Collections.singletonMap("a", Collections.singletonList("1")));
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log);
        val result = sb.toString();
        Assertions.assertTrue(result.startsWith("GET /api/test?a=1"));
        Assertions.assertTrue(result.contains("Authorization: token-1"));
        Assertions.assertTrue(result.contains("X-Trace-Id: trace-1"));
    }

    @Test
    public void appendHttpLog_postFormAndBody() {
        // 正例：POST 有 params（form body）+ 请求体，两者均输出
        val log = requestLog();
        log.setMethod(HttpMethod.POST.name());
        log.setParams(Collections.singletonMap("name", Collections.singletonList("张三")));
        // req 统一为 Object，请求体直接存字符串
        log.setReq("{\"x\":1}");
        log.setRsp("{\"y\":2}");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("name=张三"));
        Assertions.assertTrue(result.contains("{\"x\":1}"));
        Assertions.assertTrue(result.contains("{\"y\":2}"));
    }

    @Test
    public void appendHttpLog_noResponseBody() {
        // 边界：无响应体时输出占位符
        val log = requestLog();
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log);
        Assertions.assertTrue(sb.toString().contains("[no response body]"));
    }

    @Test
    public void appendHttpLog_errorAndRt() {
        // 正例：异常信息 + 耗时输出
        val log = requestLog();
        log.setErrorMessage("bad request");
        log.setBeginTimeMillis(1000L);
        log.setEndTimeMillis(2000L);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("error: bad request"));
        Assertions.assertTrue(result.contains("rt: 1000ms"));
    }

    @Test
    public void appendHttpLog_rtInvalidNotOutput() {
        // 边界：end 早于 begin 时不输出耗时
        val log = requestLog();
        log.setBeginTimeMillis(2000L);
        log.setEndTimeMillis(1000L);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log);
        Assertions.assertFalse(sb.toString().contains("rt:"));
    }

    @Test
    public void appendHttpLog_headersMultipleValues() {
        // 边界：同一 header 多个值逐行输出
        val log = requestLog();
        log.setHeaders(java.util.Collections.singletonMap("Accept", java.util.Arrays.asList("a", "b")));
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("Accept: a"));
        Assertions.assertTrue(result.contains("Accept: b"));
    }

    @Test
    public void appendHttpLog_nullInfo() {
        // 异常路径：null info 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.appendHttpLog(new StringBuilder(), null)
        );
    }

}
