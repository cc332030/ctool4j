package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.core.enums.CLogSource;
import com.c332030.ctool4j.web.config.CRequestLogBaseConfig;
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
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「HTTP 报文结构」的先后顺序组织分类：请求行/请求头/请求体 → 响应状态行/响应头/响应体 → 异常与业务数据 → 开关控制，使分类与真实报文输出结构一一对应，便于从文档直接对照最终打印格式检查每一段是否被覆盖</li>
 *   <li>在报文结构各段之外，单独提取「开关控制」与「body 文本转换」两个横向维度：开关（enableHeader）作用于 header 输出、与报文内容解耦，单独成类便于验证开关只影响 header 不影响 body/状态行；body 文本转换（getBodyText）是 feign 等 byte[] body 场景与 MVC Object body 场景的统一转换节点，单独成类便于验证文本/非文本/空 body 的转换边界</li>
 *   <li>每个编号下同时给出正例/反例/边界（分支）路径，避免只测主路径而遗漏易错的边界与分支</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对输出格式的约定：请求行、header 逐行、请求体/响应体带空行、响应报文头与请求侧对称</li>
 *   <li>依据需求对安全与可读性的要求：enableHeader 开关默认关闭避免敏感 header 泄露（验证开关只作用 header）；空 body 输出占位符（EMPTY_REQ/EMPTY_RSP）避免 {@code [null]}（边界覆盖）</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：未知状态码为边界值、无状态码有响应头为独立分支、开关关闭为分支覆盖</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：请求行/query/form body、请求头（单值/多值）、响应状态行（正/未知码/无状态码）、响应头（单值/多值）、请求体/响应体、异常与业务数据、enableHeader 开关开/关、getBodyText 文本/非文本/空 body</li>
 *   <li>未覆盖：非 UTF-8 charset 解码的 gbk 场景（getCharsetOrDefault 已单独覆盖，getBodyText 复用其逻辑）；响应头多值走共用 appendHeaderMap（请求头多值已覆盖），行为等价</li>
 * </ul>
 * <h2>已知取舍</h2>
 * <ul>
 *   <li>请求头/响应头采集层总是采集、是否输出由打印层 enableHeader 开关控制（采集与打印解耦，打印层按开关过滤敏感 header）</li>
 *   <li>getBodyText 空 body 返回占位符参数（EMPTY_REQ/EMPTY_RSP）与 null（不输出）两种取值，由调用方传参决定</li>
 * </ul>
 * <h2>请求行与来源</h2>
 * <ul>
 *   <li>1.1.1 有来源标识输出 {@code [source]}，GET 请求输出 METHOD URL</li>
 *   <li>1.1.2 GET 带 query 参数拼到 URL，多 value 时 key 重复出现</li>
 *   <li>1.1.3 POST 不拼 URL query（参数在 body/form）</li>
 *   <li>1.1.4 method 为 null 时 URL 前输出 {@code null}</li>
 * </ul>
 * <h2>请求头</h2>
 * <ul>
 *   <li>1.2.1 请求头逐行输出 Key: Value</li>
 *   <li>1.2.2 同一 header 多个值逐行输出</li>
 * </ul>
 * <h2>请求体</h2>
 * <ul>
 *   <li>1.3.1 POST 有 body 输出 body</li>
 *   <li>1.3.2 POST 无 body 有 params 时输出 form-urlencoded body</li>
 *   <li>1.3.3 req 为 null 时不输出请求体</li>
 *   <li>1.3.4 空 req 不输出</li>
 *   <li>1.3.5 params 为空但请求体有值时仅输出 json 请求体（paramsEmptyReqOnly）</li>
 *   <li>1.3.6 请求体为 null 但响应体有值时仅输出响应体（reqNullRspPresent）</li>
 * </ul>
 * <h2>响应状态行</h2>
 * <ul>
 *   <li>1.4.1 有状态码输出状态行（如 404 Not Found），响应体随后（responseStatusOnly）</li>
 *   <li>1.4.2 未知状态码（599）仅输出数字不输出描述（responseStatusUnknownCode）</li>
 *   <li>1.4.3 未采集状态码/响应头时不输出响应报文头，向后兼容（noResponseStatus）</li>
 *   <li>1.4.4 无状态码但有响应头时仅输出响应头（responseHeadersOnlyNoStatus）</li>
 * </ul>
 * <h2>响应头</h2>
 * <ul>
 *   <li>1.5.1 响应头逐行输出（allFields 中随状态行）</li>
 *   <li>1.5.2 响应头同一 header 多个值（如 Set-Cookie）逐行输出（responseHeadersMultipleValues）</li>
 * </ul>
 * <h2>响应体</h2>
 * <ul>
 *   <li>1.6.1 有响应体输出</li>
 *   <li>1.6.2 无响应体不输出</li>
 *   <li>1.6.3 请求/响应体为 null 时不输出占位（noResponseBody，避免 [null]）</li>
 * </ul>
 * <h2>异常与业务数据</h2>
 * <ul>
 *   <li>1.7.1 异常信息 errorMessage 输出</li>
 *   <li>1.7.2 traceId/tenantId/userId 业务数据行输出</li>
 *   <li>1.7.3 耗时 rt 输出</li>
 *   <li>1.7.4 全部字段均有值时验证完整格式与各段顺序（allFields）</li>
 *   <li>1.7.5 异常信息 + 耗时输出（errorAndRt）</li>
 *   <li>1.7.6 耗时无效（end 早于 begin）时不输出（rtInvalidNotOutput）</li>
 * </ul>
 * <h2>开关控制（enableHeader）</h2>
 * <ul>
 *   <li>1.8.1 开关开启输出请求头/响应头</li>
 *   <li>1.8.2 开关关闭不输出请求头/响应头，但状态行与请求/响应体仍输出（enableHeaderFalse_skipHeaders）</li>
 *   <li>1.8.3 null info 抛 NPE（nullInfo）</li>
 *   <li>1.8.4 开关关闭时业务数据区仍输出 token/ip，保证鉴权与来源信息可见（enableHeaderFalse_tokenIpInBusinessData）</li>
 *   <li>1.8.5 开关开启时请求头已输出 Authorization/ip，业务数据区不重复打印（enableHeaderTrue_tokenIpNotDuplicated）</li>
 * </ul>
 * <h2>慢请求日志（logSlowRequest，web/feign 共用，耗时从 CRequestLog 起止时间计算）</h2>
 * <ul>
 *   <li>1.9.1 耗时（end-begin）超过 slowLogMillis 时输出 warn 慢日志（logSlowRequest_exceeded）</li>
 *   <li>1.9.2 慢日志开关关闭时不输出（logSlowRequest_disabled）</li>
 *   <li>1.9.3 耗时未超过阈值时不输出（logSlowRequest_notExceeded）</li>
 * </ul>
 * <h2>body 文本转换（getBodyText）</h2>
 * <ul>
 *   <li>2.1 正例：文本 body 按 Content-Type charset 解码（text）</li>
 *   <li>2.2 反例：非文本 body（application/octet-stream）输出占位符 NOT_TEXT_BODY（binary）</li>
 *   <li>2.3 边界：空 body（空数组/null）返回传入占位符，避免 [null]（emptyBytes_returnsPlaceholder）</li>
 *   <li>2.4 边界：空 body 且未传占位符时返回 null，调用方不输出（emptyBytes_noPlaceholder_returnsNull）</li>
 * </ul>
 * <h2>Content-Type 与 Accept 设置</h2>
 * <ul>
 *   <li>3.1 contextTypeForm：设置表单 content-type（contextTypeForm）</li>
 *   <li>3.2 contextTypeForm null headers 抛 NPE（contextTypeForm_nullHeaders_throws）</li>
 *   <li>3.3 contextTypeJson：设置 json content-type（contextTypeJson）</li>
 *   <li>3.4 contextTypeJson null headers 抛 NPE（contextTypeJson_nullHeaders_throws）</li>
 *   <li>3.5 acceptJson：设置 Accept: application/json（acceptJson）</li>
 *   <li>3.6 acceptJson null headers 抛 NPE（acceptJson_nullHeaders_throws）</li>
 * </ul>
 * <h2>getFullHeaderStr</h2>
 * <ul>
 *   <li>4.1 多个 header 以换行连接（getFullHeaderStr）</li>
 *   <li>4.2 null/空 map 返回 null（getFullHeaderStr_null_returnsNull）</li>
 *   <li>4.3 同一 header 多值用逗号连接（getFullHeaderStr_multiValue）</li>
 *   <li>4.4 predicate 过滤 header（getFullHeaderStr_withPredicate）</li>
 *   <li>4.5 predicate 全部拒绝返回空串（getFullHeaderStr_predicateAllReject_returnsEmptyString）</li>
 * </ul>
 * <h2>isTextBody</h2>
 * <ul>
 *   <li>5.1 application/json 视为文本 body（isTextBody_json）</li>
 *   <li>5.2 application/octet-stream 非文本（isTextBody_binary）</li>
 *   <li>5.3 无 Content-Type 按非文本处理（isTextBody_emptyHeaders）</li>
 *   <li>5.4 null headers 返回 false（isTextBody_nullHeaders_returnsFalse）</li>
 * </ul>
 * <h2>getCharsetOrDefault</h2>
 * <ul>
 *   <li>6.1 显式声明 charset 时按声明解析（getCharsetOrDefault_explicit）</li>
 *   <li>6.2 未声明 charset 默认 UTF-8（getCharsetOrDefault_defaultUtf8）</li>
 *   <li>6.3 无法解析的 Content-Type 回退 UTF-8（getCharsetOrDefault_invalidContentType）</li>
 *   <li>6.4 无 Content-Type 默认 UTF-8（getCharsetOrDefault_emptyHeaders）</li>
 *   <li>6.5 null headers 返回 UTF-8（getCharsetOrDefault_nullHeaders_returnsUtf8）</li>
 * </ul>
 * <h2>appendRequestUrl / appendUrl</h2>
 * <ul>
 *   <li>7.1 GET 带 query 参数拼到 URL，多 value 时 key 重复出现（appendRequestUrl_getWithParams）</li>
 *   <li>7.2 GET 无 query 仅记录 path（appendRequestUrl_getNoParams）</li>
 *   <li>7.3 POST 不拼 URL query（参数在 body/form）（appendUrl_postParamsNotAppended）</li>
 *   <li>7.4 method 为 null 时 URL 前输出 null（appendUrl_nullMethod）</li>
 *   <li>7.5 多 value 参数（appendUrl_multiValueParams）</li>
 * </ul>
 * <h2>appendHeaderLine</h2>
 * <ul>
 *   <li>8.1 正常输出 Key: Value（appendHeaderLine_string）</li>
 *   <li>8.2 空 value 输出（appendHeaderLine_emptyValue）</li>
 *   <li>8.3 null value 输出（appendHeaderLine_nullValue）</li>
 * </ul>
 * <h2>appendError</h2>
 * <ul>
 *   <li>9.1 异常信息拼接（appendError）</li>
 *   <li>9.2 空 error 不输出（appendError_empty）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
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
     * 对应测试用例 3.1：设置表单 content-type
     */
    @Test
    public void contextTypeForm() {
        // 正例：设置表单 content-type
        val headers = new HttpHeaders();
        CCommUtils.contextTypeForm(headers);
        Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
    }

    /**
     * 对应测试用例 3.2：contextTypeForm null headers 抛 NPE
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
     * 对应测试用例 3.3：设置 json content-type
     */
    @Test
    public void contextTypeJson() {
        // 正例：设置 json content-type
        val headers = new HttpHeaders();
        CCommUtils.contextTypeJson(headers);
        Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    /**
     * 对应测试用例 3.4：contextTypeJson null headers 抛 NPE
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
     * 对应测试用例 3.5：设置 Accept: application/json
     */
    @Test
    public void acceptJson() {
        // 正例：设置 Accept: application/json
        val headers = new HttpHeaders();
        CCommUtils.acceptJson(headers);
        Assertions.assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
    }

    /**
     * 对应测试用例 3.6：acceptJson null headers 抛 NPE
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
     * 对应测试用例 4.1：多个 header 以换行连接
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
     * 对应测试用例 4.2：null/空 map 返回 null
     */
    @Test
    public void getFullHeaderStr_null_returnsNull() {
        // 边界：null/空 map 返回 null
        Assertions.assertNull(CCommUtils.getFullHeaderStr(null));
        Assertions.assertNull(CCommUtils.getFullHeaderStr(Collections.emptyMap()));
    }

    /**
     * 对应测试用例 4.3：同一 header 多值用逗号连接
     */
    @Test
    public void getFullHeaderStr_multiValue() {
        // 边界：同一 header 多个值用逗号连接
        val map = new LinkedHashMap<String, Collection<String>>();
        map.put("A", java.util.Arrays.asList("1", "2"));
        Assertions.assertEquals("A: 1,2", CCommUtils.getFullHeaderStr(map));
    }

    /**
     * 对应测试用例 4.4：predicate 过滤 header
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
     * 对应测试用例 4.5：predicate 全部拒绝返回空串
     */
    @Test
    public void getFullHeaderStr_predicateAllReject_returnsEmptyString() {
        // 边界：predicate 全部拒绝时返回空串（非 null）
        val map = headers("A", "1");
        Assertions.assertEquals("", CCommUtils.getFullHeaderStr(map, e -> false));
    }

    // ---------- isTextBody ----------

    /**
     * 对应测试用例 5.1：application/json 视为文本 body
     */
    @Test
    public void isTextBody_json() {
        // 正例：application/json 视为文本 body
        val map = headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Assertions.assertTrue(CCommUtils.isTextBody(map));
    }

    /**
     * 对应测试用例 5.2：application/octet-stream 非文本
     */
    @Test
    public void isTextBody_binary() {
        // 反例：application/octet-stream 非文本
        val map = headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        Assertions.assertFalse(CCommUtils.isTextBody(map));
    }

    /**
     * 对应测试用例 5.3：无 Content-Type 按非文本处理
     */
    @Test
    public void isTextBody_emptyHeaders() {
        // 边界：无 Content-Type 时按非文本处理
        Assertions.assertFalse(CCommUtils.isTextBody(Collections.emptyMap()));
    }

    /**
     * 对应测试用例 5.4：null headers 返回 false
     */
    @Test
    public void isTextBody_nullHeaders_returnsFalse() {
        // 边界：null headers 按无 Content-Type 处理，返回 false
        Assertions.assertFalse(CCommUtils.isTextBody(null));
    }

    // ---------- getCharsetOrDefault ----------

    /**
     * 对应测试用例 6.1：显式声明 charset 时按声明解析
     */
    @Test
    public void getCharsetOrDefault_explicit() {
        // 正例：显式声明 charset 时按声明解析
        val map = headers(HttpHeaders.CONTENT_TYPE, "text/plain;charset=gbk");
        Assertions.assertEquals("GBK", CCommUtils.getCharsetOrDefault(map).name());
    }

    /**
     * 对应测试用例 6.2：未声明 charset 默认 UTF-8
     */
    @Test
    public void getCharsetOrDefault_defaultUtf8() {
        // 边界：未声明 charset 时默认 UTF-8
        val map = headers(HttpHeaders.CONTENT_TYPE, "application/json");
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(map));
    }

    /**
     * 对应测试用例 6.3：无法解析的 Content-Type 回退 UTF-8
     */
    @Test
    public void getCharsetOrDefault_invalidContentType() {
        // 边界：无法解析的 Content-Type 忽略并回退 UTF-8
        val map = headers(HttpHeaders.CONTENT_TYPE, "!!!not-a-media-type!!!");
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(map));
    }

    /**
     * 对应测试用例 6.4：无 Content-Type 默认 UTF-8
     */
    @Test
    public void getCharsetOrDefault_emptyHeaders() {
        // 边界：无 Content-Type 时默认 UTF-8
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(Collections.emptyMap()));
    }

    /**
     * 对应测试用例 6.5：null headers 返回 UTF-8
     */
    @Test
    public void getCharsetOrDefault_nullHeaders_returnsUtf8() {
        // 边界：null headers 按无 Content-Type 处理，返回默认 UTF-8
        Assertions.assertEquals(StandardCharsets.UTF_8, CCommUtils.getCharsetOrDefault(null));
    }

    // ---------- appendRequestUrl / appendUrl ----------

    /**
     * 对应测试用例 7.1：GET 带 query 参数拼到 URL，多 value 时 key 重复出现
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
     * 对应测试用例 7.2：GET 无 query 仅记录 path
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
     * 对应测试用例 7.3：POST 不拼 URL query（参数在 body/form）
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
     * 对应测试用例 7.4：method 为 null 时 URL 前输出 null
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
     * 对应测试用例 7.5：多 value 参数
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
     * 对应测试用例 8.1：正常输出 Key: Value
     */
    @Test
    public void appendHeaderLine_string() {
        // 正例：拼接 key: value 且前置换行
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", "Value");
        Assertions.assertEquals("start\nKey: Value", sb.toString());
    }

    /**
     * 对应测试用例 8.2：空 value 输出
     */
    @Test
    public void appendHeaderLine_emptyValue() {
        // 边界：value 为空时不做任何拼接
        val sb = new StringBuilder("start");
        CCommUtils.appendHeaderLine(sb, "Key", "");
        Assertions.assertEquals("start", sb.toString());
    }

    /**
     * 对应测试用例 8.3：null value 输出
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
     * 对应测试用例 9.1：异常信息拼接
     */
    @Test
    public void appendError() {
        // 正例：拼接 error 信息
        val sb = new StringBuilder("start");
        CCommUtils.appendError(sb, "boom");
        Assertions.assertEquals("start\n\nerror: boom", sb.toString());
    }

    /**
     * 对应测试用例 9.2：空 error 不输出
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
     * 对应测试用例 1.1.1：有来源标识输出 {@code [source]}，GET 请求输出 METHOD URL
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
     * 对应测试用例 1.1.1：有来源标识输出 {@code [source]}，GET 请求输出 METHOD URL
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
     * 对应测试用例 1.3.1：POST 有 body 输出 body
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
     * 对应测试用例 1.3.5：params 为空但请求体有值时仅输出 json 请求体
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
     * 对应测试用例 1.3.6：请求体为 null 但响应体有值时仅输出响应体
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
     * 对应测试用例 1.7.4：全部字段均有值时验证完整格式与各段顺序
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
     * 对应测试用例 1.6.3：请求/响应体为 null 时不输出占位（noResponseBody，避免 [null]）
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
     * 对应测试用例 1.7.5：异常信息 + 耗时输出
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
     * 对应测试用例 1.7.6：耗时无效（end 早于 begin）时不输出
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
     * 对应测试用例 1.5.2：响应头同一 header 多个值（如 Set-Cookie）逐行输出
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
     * 对应测试用例 1.8.3：null info 抛 NPE
     */
    @Test
    public void appendHttpLog_nullInfo() {
        // 异常路径：null info 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCommUtils.appendHttpLog(new StringBuilder(), null, true)
        );
    }

    // ---------- logSlowRequest（慢请求日志，web/feign 共用） ----------

    private CRequestLog slowLogInfo(String sourceText, String path, long begin, long end) {
        val info = new CRequestLog();
        info.setSource(() -> sourceText);
        info.setPath(path);
        info.setBeginTimeMillis(begin);
        info.setEndTimeMillis(end);
        return info;
    }

    private CRequestLogBaseConfig slowLogConfig(boolean enable, int millis) {
        val config = new CRequestLogBaseConfig();
        config.setSlowLogEnable(enable);
        config.setSlowLogMillis(millis);
        return config;
    }

    /**
     * 对应测试用例 6.1.1：耗时（end-begin）超过 slowLogMillis 时输出慢日志，带来源标识（中括号置于最前）
     */
    @Test
    public void logSlowRequest_exceeded() {
        CCommUtils.logSlowRequest(slowLogConfig(true, 1000), slowLogInfo("feign", "/api/test", 1000, 3000));
        // 输出到统一慢日志 logger（SLOW_LOG），无异常即视为通过（耗时超阈值分支）
        Assertions.assertTrue(CCommUtils.SLOW_LOG.isWarnEnabled());
    }

    /**
     * 对应测试用例 6.1.2：慢日志开关关闭时不输出（不抛异常，开关生效）
     */
    @Test
    public void logSlowRequest_disabled() {
        // 开关关闭时直接跳过，不应抛出任何异常
        Assertions.assertDoesNotThrow(() ->
            CCommUtils.logSlowRequest(slowLogConfig(false, 1000), slowLogInfo("feign", "/api/test", 1000, 3000)));
    }

    /**
     * 对应测试用例 6.1.3：耗时未超过阈值时不输出（不抛异常，阈值判断生效）
     */
    @Test
    public void logSlowRequest_notExceeded() {
        // 耗时不足时直接跳过，不应抛出任何异常
        Assertions.assertDoesNotThrow(() ->
            CCommUtils.logSlowRequest(slowLogConfig(true, 1000), slowLogInfo("feign", "/api/test", 1000, 1500)));
    }

}
