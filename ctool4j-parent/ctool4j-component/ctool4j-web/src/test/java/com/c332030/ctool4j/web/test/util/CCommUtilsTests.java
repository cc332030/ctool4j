package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.core.enums.CLogSource;
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
 * <p>`com.c332030.ctool4j.web.util.CCommUtils`（web 工具类）请求日志拼接的测试用例，
 * 覆盖容易出错或出错后难发现的方法：headers 拼接、URL/Query 拼接、charset 解析、响应报文头、完整 HTTP 日志拼接等；
 * 测试用例分类与编号见 doc/design/web/CCommUtilsTests.adoc，各测试方法在 javadoc 中标注对应编号</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CCommUtilsTests {

    /**
     * 构造一个可复用的 CRequestLog 实例（@Data 提供 public setter）
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

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void contextTypeForm() {
        // 正例：设置表单 content-type
        val headers = new HttpHeaders();
        CCommUtils.contextTypeForm(headers);
        Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void contextTypeForm_nullHeaders_throws() {
        // 异常路径：null headers 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.contextTypeForm(null)
        );
    }

    // ---------- contextTypeJson ----------

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void contextTypeJson() {
        // 正例：设置 json content-type
        val headers = new HttpHeaders();
        CCommUtils.contextTypeJson(headers);
        Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    /**
     * 对应测试用例 3.4
     */
    @Test
    public void contextTypeJson_nullHeaders_throws() {
        // 异常路径：null headers 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.contextTypeJson(null)
        );
    }

    // ---------- acceptJson ----------

    /**
     * 对应测试用例 3.5
     */
    @Test
    public void acceptJson() {
        // 正例：设置 Accept: application/json
        val headers = new HttpHeaders();
        CCommUtils.acceptJson(headers);
        Assertions.assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
    }

    /**
     * 对应测试用例 3.6
     */
    @Test
    public void acceptJson_nullHeaders_throws() {
        // 异常路径：null headers 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.acceptJson(null)
        );
    }

    // ---------- getFullHeaderStr ----------

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void getFullHeaderStr() {
        // 正例：多个 header 以换行连接
        val map = new LinkedHashMap<String, Collection<String>>();
        map.put("A", Collections.singletonList("1"));
        map.put("B", Collections.singletonList("2"));
        Assertions.assertEquals("A: 1\nB: 2", CCommUtils.getFullHeaderStr(map));
    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void getFullHeaderStr_null_returnsNull() {
        // 边界：null/空 map 返回 null
        Assertions.assertNull(CCommUtils.getFullHeaderStr(null));
        Assertions.assertNull(CCommUtils.getFullHeaderStr(Collections.emptyMap()));
    }

    /**
     * 对应测试用例 4.3
     */
    @Test
    public void getFullHeaderStr_multiValue() {
        // 边界：同一 header 多个值用逗号连接
        val map = new LinkedHashMap<String, Collection<String>>();
        map.put("A", java.util.Arrays.asList("1", "2"));
        Assertions.assertEquals("A: 1,2", CCommUtils.getFullHeaderStr(map));
    }

    /**
     * 对应测试用例 4.4
     */
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

    /**
     * 对应测试用例 4.5
     */
    @Test
    public void getFullHeaderStr_predicateAllReject_returnsEmptyString() {
        // 边界：predicate 全部拒绝时返回空串（非 null）
        val map = headers("A", "1");
        Assertions.assertEquals("", CCommUtils.getFullHeaderStr(map, e -> false));
    }

    // ---------- isTextBody ----------

    /**
     * 对应测试用例 5.1
     */
    @Test
    public void isTextBody_json() {
        // 正例：application/json 视为文本 body
        val map = headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Assertions.assertTrue(CCommUtils.isTextBody(map));
    }

    /**
     * 对应测试用例 5.2
     */
    @Test
    public void isTextBody_binary() {
        // 反例：application/octet-stream 非文本
        val map = headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        Assertions.assertFalse(CCommUtils.isTextBody(map));
    }

    /**
     * 对应测试用例 5.3
     */
    @Test
    public void isTextBody_emptyHeaders() {
        // 边界：无 Content-Type 时按非文本处理
        Assertions.assertFalse(CCommUtils.isTextBody(Collections.emptyMap()));
    }

    /**
     * 对应测试用例 5.4
     */
    @Test
    public void isTextBody_nullHeaders_returnsFalse() {
        // 边界：null headers 按无 Content-Type 处理，返回 false
        Assertions.assertFalse(CCommUtils.isTextBody(null));
    }

    // ---------- getCharsetOrDefault ----------

    /**
     * 对应测试用例 6.1
     */
    @Test
    public void getCharsetOrDefault_explicit() {
        // 正例：显式声明 charset 时按声明解析
        val map = headers(HttpHeaders.CONTENT_TYPE, "text/plain;charset=gbk");
        Assertions.assertEquals("GBK", CCommUtils.getCharsetOrDefault(map).name());
    }

    /**
     * 对应测试用例 6.2
     */
    @Test
    public void getCharsetOrDefault_defaultUtf8() {
        // 边界：未声明 charset 时默认 UTF-8
        val map = headers(HttpHeaders.CONTENT_TYPE, "application/json");
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(map));
    }

    /**
     * 对应测试用例 6.3
     */
    @Test
    public void getCharsetOrDefault_invalidContentType() {
        // 边界：无法解析的 Content-Type 忽略并回退 UTF-8
        val map = headers(HttpHeaders.CONTENT_TYPE, "!!!not-a-media-type!!!");
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(map));
    }

    /**
     * 对应测试用例 6.4
     */
    @Test
    public void getCharsetOrDefault_emptyHeaders() {
        // 边界：无 Content-Type 时默认 UTF-8
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(Collections.emptyMap()));
    }

    /**
     * 对应测试用例 6.5
     */
    @Test
    public void getCharsetOrDefault_nullHeaders_returnsUtf8() {
        // 边界：null headers 按无 Content-Type 处理，返回默认 UTF-8
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(null));
    }

    // ---------- appendRequestUrl / appendUrl ----------

    /**
     * 对应测试用例 7.1
     */
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

    /**
     * 对应测试用例 7.2
     */
    @Test
    public void appendRequestUrl_getNoParams() {
        // 边界：GET 无 params 时不拼 ?
        val log = requestLog();
        val sb = new StringBuilder();
        CCommUtils.appendRequestUrl(sb, log);
        Assertions.assertEquals("GET /api/test", sb.toString());
    }

    /**
     * 对应测试用例 7.3
     */
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

    /**
     * 对应测试用例 7.4
     */
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

    /**
     * 对应测试用例 7.5
     */
    @Test
    public void appendUrl_multiValueParams() {
        // 边界：同一 key 多个 value 用 & 连接
        val log = requestLog();
        log.setParams(java.util.Collections.singletonMap("a", java.util.Arrays.asList("1", "2")));
        val sb = new StringBuilder();
        CCommUtils.appendUrl(sb, log);
        Assertions.assertEquals("/api/test?a=1&a=2", sb.toString());
    }

    // ---------- appendHeaderLine ----------

    /**
     * 对应测试用例 8.1
     */
    @Test
    public void appendHeaderLine_string() {
        // 正例：拼接 key: value 且前置换行
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", "Value");
        Assertions.assertEquals("start\nKey: Value", sb.toString());
    }

    /**
     * 对应测试用例 8.2
     */
    @Test
    public void appendHeaderLine_emptyValue() {
        // 边界：value 为空时不做任何拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", "");
        Assertions.assertEquals("start", sb.toString());
    }

    /**
     * 对应测试用例 8.3
     */
    @Test
    public void appendHeaderLine_nullValue() {
        // 边界：value 为 null 时不做任何拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", null);
        Assertions.assertEquals("start", sb.toString());
    }

    // ---------- getBodyText（feign 等 byte[] body 场景的真实转换节点） ----------

    /**
     * 对应测试用例 2.1：正例，文本 body 按 charset 解码
     */
    @Test
    public void getBodyText_text() {
        val body = "你好".getBytes(StandardCharsets.UTF_8);
        val map = headers(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
        Assertions.assertEquals("你好", CCommUtils.getBodyText(body, map, CCommUtils.NOT_TEXT_BODY));
    }

    /**
     * 对应测试用例 2.2：反例，非文本 body 输出占位符
     */
    @Test
    public void getBodyText_binary() {
        val body = new byte[]{(byte) 0xFF, (byte) 0x00};
        val map = headers(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        Assertions.assertEquals(CCommUtils.NOT_TEXT_BODY, CCommUtils.getBodyText(body, map, null));
    }

    /**
     * 对应测试用例 2.3：边界，空 body 返回传入占位符（feign 无请求体时输出 EMPTY_REQ/EMPTY_RSP，避免 [null]）
     */
    @Test
    public void getBodyText_emptyBytes_returnsPlaceholder() {
        Assertions.assertEquals("EMPTY", CCommUtils.getBodyText(new byte[0], null, "EMPTY"));
        Assertions.assertEquals("EMPTY", CCommUtils.getBodyText(null, null, "EMPTY"));
    }

    /**
     * 对应测试用例 2.4：边界，空 body 且未传占位符时返回 null（不输出）
     */
    @Test
    public void getBodyText_emptyBytes_noPlaceholder_returnsNull() {
        Assertions.assertNull(CCommUtils.getBodyText(new byte[0], null, null));
        Assertions.assertNull(CCommUtils.getBodyText(null, null, null));
    }

    // ---------- appendError ----------

    /**
     * 对应测试用例 9.1
     */
    @Test
    public void appendError() {
        // 正例：拼接 error 信息
        val sb = new StringBuilder("start");
        CCommUtils.appendError(sb, "boom");
        Assertions.assertEquals("start\n\nerror: boom", sb.toString());
    }

    /**
     * 对应测试用例 9.2
     */
    @Test
    public void appendError_empty() {
        // 边界：空/null errorMessage 不拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendError(sb, null);
        CCommUtils.appendError(sb, "");
        Assertions.assertEquals("start", sb.toString());
    }

    // ---------- appendHttpLog（完整 HTTP 报文拼接） ----------

    /**
     * 对应测试用例 1.1.1
     */
    @Test
    public void appendHttpLog_get() {
        // 正例：GET 完整拼接（请求行 + query + header + 业务数据）
        val log = requestLog();
        log.setTraceId("trace-1");
        log.setParams(Collections.singletonMap("a", Collections.singletonList("1")));
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.startsWith("GET /api/test?a=1"));
        Assertions.assertTrue(result.contains("X-Trace-Id: trace-1"));
    }

    /**
     * 对应测试用例 1.1.1
     */
    @Test
    public void appendHttpLog_source() {
        // 正例：设置 source 时，日志最前面输出 [来源] 独立行，请求行紧随其后
        val log = requestLog();
        log.setSource(CLogSource.FEIGN);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.startsWith("[feign]\nGET /api/test"));
    }

    /**
     * 对应测试用例 1.3.1
     */
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
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("name=张三"));
        Assertions.assertTrue(result.contains("{\"x\":1}"));
        Assertions.assertTrue(result.contains("{\"y\":2}"));
    }

    /**
     * 对应测试用例 1.3.5
     */
    @Test
    public void appendHttpLog_paramsEmptyReqOnly() {
        // 分支：params 为空但请求体有值，只输出 json 请求体（无 form body）
        val log = requestLog();
        log.setMethod(HttpMethod.POST.name());
        log.setReq("{\"x\":1}");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.startsWith("POST /api/test\n\n{\"x\":1}"));
        Assertions.assertFalse(result.contains("name="));
    }

    /**
     * 对应测试用例 1.3.6
     */
    @Test
    public void appendHttpLog_reqNullRspPresent() {
        // 分支：请求体为 null（不输出）但响应体有值，只输出响应体
        val log = requestLog();
        log.setMethod(HttpMethod.POST.name());
        log.setRsp("{\"y\":2}");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("{\"y\":2}"));
        Assertions.assertFalse(result.contains("[null]"));
    }

    /**
     * 对应测试用例 1.7.4
     */
    @Test
    public void appendHttpLog_allFields() {
        // 正例：所有选项都有值，验证最终完整打印格式
        val log = requestLog();
        log.setSource(CLogSource.MVC);
        log.setMethod(HttpMethod.POST.name());
        log.setPath("/api/submit");
        log.setRequestHeaders(headers("Content-Type", "application/json"));
        log.setParams(Collections.singletonMap("name", Collections.singletonList("张三")));
        log.setReq("{\"x\":1}");
        log.setRsp("{\"y\":2}");
        log.setResponseStatus(200);
        log.setResponseHeaders(headers("Content-Type", "application/json"));
        log.setErrorMessage("boom");
        log.setTraceId("trace-1");
        log.setTenantId("tenant-1");
        log.setUserId("user-1");
        log.setBeginTimeMillis(1000L);
        log.setEndTimeMillis(1100L);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        Assertions.assertEquals(
            "[mvc]\n"
                + "POST /api/submit\n"
                + "Content-Type: application/json\n\n"
                + "name=张三\n\n"
                + "{\"x\":1}\n\n"
                + "200 OK\n"
                + "Content-Type: application/json\n\n"
                + "{\"y\":2}\n\n"
                + "error: boom\n\n"
                + "X-Trace-Id: trace-1\n"
                + "X-Tenant-Id: tenant-1\n"
                + "X-User-Id: user-1\n"
                + "rt: 100ms\n",
            sb.toString()
        );
    }

    /**
     * 对应测试用例 1.4.1：正例，仅有响应状态码无响应头时输出状态行
     */
    @Test
    public void appendHttpLog_responseStatusOnly() {
        val log = requestLog();
        log.setResponseStatus(404);
        log.setRsp("not found");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("\n404 Not Found\n\nnot found"));
    }

    /**
     * 对应测试用例 1.4.2：边界，未知状态码不输出描述，仅输出数字
     */
    @Test
    public void appendHttpLog_responseStatusUnknownCode() {
        val log = requestLog();
        log.setResponseStatus(599);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("\n599"));
        Assertions.assertFalse(result.contains("599 "));
    }

    /**
     * 对应测试用例 1.4.3：边界，未采集响应状态码/响应头时不输出响应报文头（向后兼容）
     */
    @Test
    public void appendHttpLog_noResponseStatus() {
        val log = requestLog();
        log.setRsp("{\"y\":2}");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("{\"y\":2}"));
        Assertions.assertFalse(result.contains(" OK"));
    }

    /**
     * 对应测试用例 1.4.4：分支，无响应状态码但有响应头时仅输出响应头（无状态行）
     */
    @Test
    public void appendHttpLog_responseHeadersOnlyNoStatus() {
        val log = requestLog();
        log.setResponseHeaders(headers("X-Trace-Id", "abc"));
        log.setRsp("{\"y\":2}");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("X-Trace-Id: abc"));
        Assertions.assertFalse(result.contains(" OK"));
    }

    /**
     * 对应测试用例 1.5.2：边界，响应头同一 header 多个值逐行输出（响应头复用 appendHeaderMap，与请求头一致）
     */
    @Test
    public void appendHttpLog_responseHeadersMultipleValues() {
        val log = requestLog();
        log.setResponseHeaders(
            java.util.Collections.singletonMap("Set-Cookie", java.util.Arrays.asList("a=1", "b=2"))
        );
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("Set-Cookie: a=1"));
        Assertions.assertTrue(result.contains("Set-Cookie: b=2"));
    }

    /**
     * 对应测试用例 1.6.3
     */
    @Test
    public void appendHttpLog_noResponseBody() {
        // 边界：请求体/响应体为 null 时不输出占位（避免出现 [null]）
        val log = requestLog();
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        Assertions.assertFalse(sb.toString().contains("[null]"));
    }

    /**
     * 对应测试用例 1.7.5
     */
    @Test
    public void appendHttpLog_errorAndRt() {
        // 正例：异常信息 + 耗时输出
        val log = requestLog();
        log.setErrorMessage("bad request");
        log.setBeginTimeMillis(1000L);
        log.setEndTimeMillis(2000L);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("error: bad request"));
        Assertions.assertTrue(result.contains("rt: 1000ms"));
    }

    /**
     * 对应测试用例 1.7.6
     */
    @Test
    public void appendHttpLog_rtInvalidNotOutput() {
        // 边界：end 早于 begin 时不输出耗时
        val log = requestLog();
        log.setBeginTimeMillis(2000L);
        log.setEndTimeMillis(1000L);
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        Assertions.assertFalse(sb.toString().contains("rt:"));
    }

    /**
     * 对应测试用例 1.5.2
     */
    @Test
    public void appendHttpLog_headersMultipleValues() {
        // 边界：同一 header 多个值逐行输出
        val log = requestLog();
        log.setRequestHeaders(java.util.Collections.singletonMap("Accept", java.util.Arrays.asList("a", "b")));
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        Assertions.assertTrue(result.contains("Accept: a"));
        Assertions.assertTrue(result.contains("Accept: b"));
    }

    /**
     * 对应测试用例 1.8.2：开关关闭，请求头/响应头不输出，但状态行与请求/响应体仍输出
     */
    @Test
    public void appendHttpLog_enableHeaderFalse_skipHeaders() {
        val log = requestLog();
        log.setRequestHeaders(headers("Accept", "application/json"));
        log.setResponseStatus(200);
        log.setResponseHeaders(headers("Content-Type", "application/json"));
        log.setReq("{\"x\":1}");
        log.setRsp("{\"y\":2}");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, false);
        val result = sb.toString();
        Assertions.assertFalse(result.contains("Accept"));
        Assertions.assertFalse(result.contains("Content-Type"));
        Assertions.assertTrue(result.contains("200 OK"));
        Assertions.assertTrue(result.contains("{\"x\":1}"));
        Assertions.assertTrue(result.contains("{\"y\":2}"));
    }

    /**
     * 对应测试用例 1.8.4：开关关闭时请求头不输出，业务数据区仍输出 token/ip，保证鉴权与来源信息可见
     */
    @Test
    public void appendHttpLog_enableHeaderFalse_tokenIpInBusinessData() {
        val log = requestLog();
        log.setRequestHeaders(headers(HttpHeaders.AUTHORIZATION, "Bearer abc"));
        log.setToken("Bearer abc");
        log.setIp("1.2.3.4");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, false);
        val result = sb.toString();
        // 请求头不输出，token/ip 仅在业务数据区各出现一次（避免完全看不到鉴权与来源信息）
        Assertions.assertEquals(1, countOccurrences(result, "Bearer abc"));
        Assertions.assertTrue(result.contains(HttpHeaders.AUTHORIZATION + ": Bearer abc"));
        Assertions.assertTrue(result.contains("ip: 1.2.3.4"));
    }

    /**
     * 对应测试用例 1.8.5：开关开启时请求头已输出 Authorization/ip，业务数据区不重复打印
     */
    @Test
    public void appendHttpLog_enableHeaderTrue_tokenIpNotDuplicated() {
        val log = requestLog();
        log.setRequestHeaders(headers(HttpHeaders.AUTHORIZATION, "Bearer abc"));
        log.setToken("Bearer abc");
        log.setIp("1.2.3.4");
        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, log, true);
        val result = sb.toString();
        // Authorization 仅在请求头区出现一次，业务数据区不重复打印
        Assertions.assertEquals(1, countOccurrences(result, "Bearer abc"));
        Assertions.assertFalse(result.contains(HttpHeaders.AUTHORIZATION + ": Bearer abc\n"));
        Assertions.assertFalse(result.contains("ip: 1.2.3.4"));
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }
        return count;
    }

    /**
     * 对应测试用例 1.8.3
     */
    @Test
    public void appendHttpLog_nullInfo() {
        // 异常路径：null info 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.appendHttpLog(new StringBuilder(), null, true)
        );
    }

}
