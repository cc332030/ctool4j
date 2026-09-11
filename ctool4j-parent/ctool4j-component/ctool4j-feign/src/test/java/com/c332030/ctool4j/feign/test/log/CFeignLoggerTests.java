package com.c332030.ctool4j.feign.test.log;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import com.c332030.ctool4j.web.model.CRequestLog;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
import feign.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * `com.c332030.ctool4j.feign.log.CFeignLogger`（feign 请求日志）的测试用例
 *
 * 覆盖：logRequest 的白名单/黑名单/全量开关判定、请求日志模型构建（method/path/headers/req）、
 * logAndRebufferResponse 重缓冲与取值、logIOException 异常信息设置、未记录时的降级返回；
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CFeignLogger 的「请求生命周期」时序组织分类：logRequest（是否记录 + 请求模型构建）→ 响应模型构建 → 异常日志 → 降级返回，使分类与 feign 拦截请求到输出日志的流程顺序对应</li>
 *   <li>请求阶段拆为两个维度：一是「记录与否」的开关与白名单/黑名单判定（决定是否产生日志），二是「请求模型构建」的字段填充（决定日志内容），避免把「是否记录」与「记录什么」混在一类</li>
 *   <li>每个分类下给出正例/分支/边界，覆盖易错点（如空 body 占位、开关只作用打印层、响应重缓冲可再读）</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 CFeignLogger 设计约定：请求头/响应头采集层总是采集、是否输出由打印层 enableHeader 开关控制（故需验证采集与打印解耦）；响应重缓冲后需可再次读取</li>
 *   <li>依据需求对可读性与安全的要求：空请求/响应体输出 EMPTY_REQ/EMPTY_RSP 占位避免 {@code [null]}；白名单优先于黑名单</li>
 *   <li>依据测试方法（分支/边界覆盖）：白名单/黑名单/全量各分支、开关关闭的采集分支、响应状态码/响应头的采集</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：白名单/黑名单/全量判定、path/query、请求体文本/非文本/空、请求头采集（开启/关闭）、响应重缓冲与再读、响应状态码/响应头采集、异常 errorMessage、降级返回</li>
 *   <li>未覆盖：{@code toHandleSpecial}（当前无调用方，不作问题提示，暂无用例）</li>
 * </ul>
 * <h2>已知取舍</h2>
 * <ul>
 *   <li>请求头/响应头采集层总是采集、是否输出由打印层 enableHeader 开关控制（采集与打印解耦）</li>
 *   <li>feign 的 {@code Response.headers()} 统一为小写 header 名（HTTP 头不区分大小写）、值为 Collection 实现，采集直接引用不做转换</li>
 * </ul>
 * <h2>名单命中判定（采集层总是采集，白名单/黑名单/logAll 仅控制完整日志打印）</h2>
 * <ul>
 *   <li>1.1.1 host 白名单命中时打印标志为 true（testLogRequestHostWhiteListHit）</li>
 *   <li>1.1.2 path 白名单命中时打印标志为 true（testLogRequestPathWhiteListHit）</li>
 *   <li>1.1.3 api 白名单命中时打印标志为 true（testLogRequestApiWhiteListHit）</li>
 *   <li>1.1.4 白名单优先于黑名单（同时命中时打印标志为 true）（testLogRequestWhiteListWinsOverBlackList）</li>
 *   <li>1.1.5 无任何名单且 logAll=true 时打印标志为 true（testLogRequestNoListLogAllTrue）</li>
 *   <li>1.1.6 黑名单命中且未命中白名单时不打印完整日志（打印标志 false），但采集仍进行（testLogRequestHostBlackListHit）</li>
 *   <li>1.1.7 无任何名单且 logAll=false 时不打印完整日志（打印标志 false），但采集仍进行（testLogRequestNoListLogAllFalse，慢日志不受 logAll 控制）</li>
 * </ul>
 * <h2>path 与 query</h2>
 * <ul>
 *   <li>2.1.1 path 含 query 时 path 与 query 分别记录</li>
 *   <li>2.1.2 path 无 query 时仅记录 path</li>
 * </ul>
 * <h2>请求体</h2>
 * <ul>
 *   <li>2.2.1 文本 body 按 charset 解码存入 req</li>
 *   <li>2.2.2 非文本 body 输出占位符</li>
 *   <li>2.2.3 空 body 输出 EMPTY_REQ 占位（避免 [null]）</li>
 * </ul>
 * <h2>请求头</h2>
 * <ul>
 *   <li>2.3.1 enableHeader 开启时深拷贝请求头存储（不可变）</li>
 *   <li>2.3.2 enableHeader 关闭时请求头仍总是被采集存储，开关仅作用于打印层（testSetRequestLogHeadersAlwaysCollectedEvenDisabled）</li>
 * </ul>
 * <h2>响应体与重缓冲</h2>
 * <ul>
 *   <li>3.1.1 重缓冲响应体后可再次读取，rsp 存响应体文本</li>
 * </ul>
 * <h2>空响应体占位</h2>
 * <ul>
 *   <li>3.2.1 空响应体输出 EMPTY_RSP 占位</li>
 * </ul>
 * <h2>响应状态码与响应头</h2>
 * <ul>
 *   <li>3.3.1 采集响应状态码与响应头（responseStatus/responseHeaders）供响应报文头输出</li>
 * </ul>
 * <h2>异常日志（logIOException）</h2>
 * <ul>
 *   <li>4.1.1 异常时记录 errorMessage</li>
 * </ul>
 * <h2>未记录时的降级返回</h2>
 * <ul>
 *   <li>5.1.1 enable 只控制打印完整日志，不控制采集：enable=false 且 logAll=true 时请求日志仍被采集（testLogRequestDisabledStillCollects）</li>
 *   <li>5.1.2 无进行中请求日志时响应/异常走降级返回，不抛异常（testLogAndRebufferResponseNoRequestLogReturnsSame/testLogIOExceptionNoRequestLogReturnsSame）</li>
 * </ul>
 * <h2>慢请求日志</h2>
 * <ul>
 *   <li>6.1 慢日志统一出口 {@code CCommUtils.logSlowRequest} 的用例（耗时超过阈值输出/开关关闭不输出/耗时不足不输出）见 ctool4j-web 的 {@code CCommUtilsTests} 1.9.x</li>
 *   <li>6.2 feign 侧验证：采集层无条件采集（黑名单/logAll 仅控制完整日志打印标志），保证慢日志所需的 source/path/起止时间始终有值（对应 1.1.6/1.1.7/5.1.1）</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
class CFeignLoggerTests {

    /**
     * 被测 API 类型
     */
    static class ApiClass {
    }

    private final CFeignClientLogConfig config = new CFeignClientLogConfig();
    private final TestableLogger logger = new TestableLogger(config);

    @AfterEach
    void clearThreadLocal() throws Exception {
        requestLogThreadLocal().remove();
        printLogThreadLocal().remove();
    }

    @SuppressWarnings("unchecked")
    private ThreadLocal<CRequestLog> requestLogThreadLocal() throws Exception {
        Field field = CFeignLogger.class.getDeclaredField("REQUEST_THREAD_LOCAL");
        field.setAccessible(true);
        return (ThreadLocal<CRequestLog>) field.get(logger);
    }

    @SuppressWarnings("unchecked")
    private ThreadLocal<Boolean> printLogThreadLocal() throws Exception {
        Field field = CFeignLogger.class.getDeclaredField("PRINT_LOG_THREAD_LOCAL");
        field.setAccessible(true);
        return (ThreadLocal<Boolean>) field.get(logger);
    }

    private CRequestLog currentRequestLog() throws Exception {
        return requestLogThreadLocal().get();
    }

    private Boolean currentPrintLog() throws Exception {
        return printLogThreadLocal().get();
    }

    // ==================== enableLog 判定（logRequest 写入 REQUEST_THREAD_LOCAL） ====================

    /**
     * 对应测试用例 5.1.1：enable 只控制打印完整日志，不控制采集（采集层总是采集，不受 enable/logAll 影响）
     */
    @Test
    void testLogRequestDisabledStillCollects() throws Exception {

        config.setEnable(false);
        config.setLogAll(false);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // enable=false 且 logAll=false 时采集仍进行（始终采集），打印完整日志由 dealResponse 中 enable 与打印标志控制
        Assertions.assertNotNull(currentRequestLog());
        Assertions.assertNotEquals(Boolean.TRUE, currentPrintLog());
    }

    /**
     * 对应测试用例 1.1.1：host 白名单命中时打印标志为 true
     */
    @Test
    void testLogRequestHostWhiteListHit() throws Exception {

        config.setEnable(true);
        config.setHostWhiteList(CSet.of("api.example.com"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        CRequestLog requestLog = currentRequestLog();
        Assertions.assertNotNull(requestLog);
        Assertions.assertEquals("GET", requestLog.getMethod());
    }

    /**
     * 对应测试用例 1.1.2：path 白名单命中时打印标志为 true
     */
    @Test
    void testLogRequestPathWhiteListHit() throws Exception {

        config.setEnable(true);
        config.setPathWhiteList(CSet.of("/foo"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://other.example.com/foo"));

        Assertions.assertNotNull(currentRequestLog());
    }

    /**
     * 对应测试用例 1.1.3：api 白名单命中时打印标志为 true
     */
    @Test
    void testLogRequestApiWhiteListHit() throws Exception {

        config.setEnable(true);
        config.setApiWhiteList(CSet.of("ApiClass"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        Assertions.assertNotNull(currentRequestLog());
    }

    /**
     * 对应测试用例 1.1.6：命中黑名单且未命中白名单 -> 不打印完整日志（打印标志 false），但采集仍进行
     */
    @Test
    void testLogRequestHostBlackListHit() throws Exception {

        config.setEnable(true);
        config.setHostBlackList(CSet.of("api.example.com"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // 黑名单只控制完整日志打印，采集层总是采集（慢日志不受黑名单影响）
        Assertions.assertNotNull(currentRequestLog());
        Assertions.assertNotEquals(Boolean.TRUE, currentPrintLog());
    }

    /**
     * 对应测试用例 1.1.4：白名单优先于黑名单（同时命中时打印标志为 true）
     */
    @Test
    void testLogRequestWhiteListWinsOverBlackList() throws Exception {

        config.setEnable(true);
        config.setHostWhiteList(CSet.of("api.example.com"));
        config.setHostBlackList(CSet.of("api.example.com"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // 白名单优先于黑名单
        Assertions.assertNotNull(currentRequestLog());
    }

    /**
     * 对应测试用例 1.1.7：无名单且 logAll=false -> 不打印完整日志（打印标志 false），但采集仍进行（慢日志不受 logAll 控制）
     */
    @Test
    void testLogRequestNoListLogAllFalse() throws Exception {

        config.setEnable(true);
        config.setLogAll(false);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // 慢日志不受 logAll 控制：采集层总是采集，logAll 仅控制完整日志打印
        Assertions.assertNotNull(currentRequestLog());
        Assertions.assertNotEquals(Boolean.TRUE, currentPrintLog());
    }

    /**
     * 对应测试用例 1.1.5：无任何名单且 logAll=true 时打印标志为 true
     */
    @Test
    void testLogRequestNoListLogAllTrue() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        Assertions.assertNotNull(currentRequestLog());
    }

    // ==================== 请求日志模型构建 ====================

    /**
     * 对应测试用例 2.1.1：path 含 query 时 path 与 query 分别记录
     */
    @Test
    void testSetRequestLogPathWithQuery() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL,
            buildRequest("http://api.example.com/order/list?id=1&type=2"));

        CRequestLog requestLog = currentRequestLog();
        Assertions.assertNotNull(requestLog);
        Assertions.assertEquals("/order/list?id=1&type=2", requestLog.getPath());
    }

    /**
     * 对应测试用例 2.1.2：path 无 query 时仅记录 path
     */
    @Test
    void testSetRequestLogPathWithoutQuery() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/order/list"));

        Assertions.assertEquals("/order/list", currentRequestLog().getPath());
    }

    /**
     * 对应测试用例 2.2.1：文本 body 按 charset 解码存入 req
     */
    @Test
    void testSetRequestLogBodyText() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        Request request = buildRequest("http://api.example.com/post", "{\"name\":\"张三\"}", "application/json");
        logger.logRequest("cfg", Logger.Level.FULL, request);

        CRequestLog requestLog = currentRequestLog();
        Assertions.assertEquals("{\"name\":\"张三\"}", requestLog.getReq());
    }

    /**
     * 对应测试用例 2.2.2：非文本 body 输出占位符
     */
    @Test
    void testSetRequestLogBodyNonText() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        Request request = buildRequest("http://api.example.com/post",
            new byte[]{1, 2, 3}, "application/octet-stream");
        logger.logRequest("cfg", Logger.Level.FULL, request);

        Assertions.assertEquals("[not text body]", currentRequestLog().getReq());
    }

    /**
     * 对应测试用例 2.2.3：空 body 输出 EMPTY_REQ 占位（避免 [null]）
     */
    @Test
    void testSetRequestLogBodyEmpty() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        Request request = buildRequest("http://api.example.com/post",
            new byte[0], "application/json");
        logger.logRequest("cfg", Logger.Level.FULL, request);

        // 空 body -> 统一占位 EMPTY_REQ，与 MVC 无请求体时一致
        Assertions.assertEquals(CRequestLogUtils.EMPTY_REQ, currentRequestLog().getReq());
    }

    /**
     * 对应测试用例 2.3.1：enableHeader 开启时深拷贝请求头存储（不可变）
     */
    @Test
    void testSetRequestLogHeadersImmutableWhenEnabled() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);
        config.setEnableHeader(true);

        Request request = buildRequest("http://api.example.com/foo", "X-Trace", Collections.singletonList("abc"));

        logger.logRequest("cfg", Logger.Level.FULL, request);

        Map<String, Collection<String>> headers = currentRequestLog().getRequestHeaders();
        Assertions.assertNotNull(headers);
        Assertions.assertEquals(Collections.singletonList("abc"), headers.get("X-Trace"));
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, headers::clear);
    }

    /**
     * 对应测试用例 2.3.2：enableHeader 为打印层开关，采集层总是采集请求头存储，是否输出由打印层控制
     */
    @Test
    void testSetRequestLogHeadersAlwaysCollectedEvenDisabled() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);
        config.setEnableHeader(false);

        Request request = buildRequest("http://api.example.com/foo", "X-Trace", Collections.singletonList("abc"));

        logger.logRequest("cfg", Logger.Level.FULL, request);

        Assertions.assertNotNull(currentRequestLog().getRequestHeaders());
        Assertions.assertEquals(
            Collections.singletonList("abc"),
            currentRequestLog().getRequestHeaders().get("X-Trace")
        );
    }

    // ==================== logAndRebufferResponse ====================

    /**
     * 对应测试用例 3.3.1：采集响应状态码与响应头，供 appendHttpLog 输出响应报文头
     */
    @Test
    void testLogAndRebufferResponseBuffersBody() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));
        CRequestLog requestLog = currentRequestLog();

        Response original = buildResponse(200, "OK", "{\"result\":1}", "application/json");
        Response rebuilt = logger.logAndRebufferResponse("cfg", Logger.Level.FULL, original, 10L);

        // 重新缓冲后返回新实例，body 与原始一致
        Assertions.assertNotSame(original, rebuilt);
        Assertions.assertArrayEquals("{\"result\":1}".getBytes(StandardCharsets.UTF_8),
            Util.toByteArray(rebuilt.body().asInputStream()));
        Assertions.assertEquals("{\"result\":1}", requestLog.getRsp());
        Assertions.assertEquals(200, requestLog.getResponseStatus());
        // feign 的 Response.headers() 统一为小写 header 名（HTTP 头不区分大小写），值为 Collection 实现
        Assertions.assertNotNull(requestLog.getResponseHeaders());
        Assertions.assertTrue(requestLog.getResponseHeaders()
            .get("content-type").contains("application/json"));
        // dealResponse 已清除线程标记，避免线程复用泄漏
        Assertions.assertNull(currentRequestLog());
    }

    /**
     * 对应测试用例 5.1.1：enable 只控制打印完整日志，不控制采集：enable=false 且 logAll=true 时请求日志仍被采集
     */
    @Test
    void testLogAndRebufferResponseNoRequestLogReturnsSame() throws Exception {

        config.setEnable(true);
        requestLogThreadLocal().remove();

        Response original = buildResponse(200, "OK", "body", "text/plain");
        Response result = logger.logAndRebufferResponse("cfg", Logger.Level.FULL, original, 5L);

        // 无进行中请求日志 -> 原样返回同一实例
        Assertions.assertSame(original, result);
    }

    // ==================== logIOException ====================

    /**
     * 对应测试用例 4.1.1：异常时记录 errorMessage
     */
    @Test
    void testLogIOExceptionSetsError() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));
        CRequestLog requestLog = currentRequestLog();

        IOException original = new IOException("connect timeout");
        IOException result = logger.logIOException("cfg", Logger.Level.FULL, original, 5L);

        Assertions.assertSame(original, result);
        Assertions.assertEquals("connect timeout", requestLog.getErrorMessage());
        Assertions.assertNull(currentRequestLog());
    }

    /**
     * 对应测试用例 5.1.1：enable 只控制打印完整日志，不控制采集：enable=false 且 logAll=true 时请求日志仍被采集
     */
    @Test
    void testLogIOExceptionNoRequestLogReturnsSame() throws Exception {

        config.setEnable(true);
        requestLogThreadLocal().remove();

        IOException original = new IOException("boom");
        Assertions.assertSame(original, logger.logIOException("cfg", Logger.Level.FULL, original, 5L));
    }

    // ==================== helper ====================

    private Request buildRequest(String url) {
        return buildRequest(url, new byte[0], "application/json");
    }

    private Request buildRequest(String url, String headerName, Collection<String> headerValues) {
        Map<String, Collection<String>> extra = new java.util.LinkedHashMap<>();
        extra.put(headerName, headerValues);
        return buildRequest(url, new byte[0], "application/json", extra);
    }

    private Request buildRequest(String url, String body, String contentType) {
        return buildRequest(url, body.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private Request buildRequest(String url, byte[] body, String contentType) {
        return buildRequest(url, body, contentType, new java.util.LinkedHashMap<>());
    }

    private Request buildRequest(String url, byte[] body, String contentType,
                                 Map<String, Collection<String>> extraHeaders) {
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new TestTarget<>(ApiClass.class, "api"));
        Map<String, Collection<String>> headers = new java.util.LinkedHashMap<>(extraHeaders);
        headers.put("Content-Type", Collections.singletonList(contentType));
        return Request.create(Request.HttpMethod.GET, url, headers, body, StandardCharsets.UTF_8, template);
    }

    private Response buildResponse(int status, String reason, String body, String contentType) {
        return Response.builder()
            .status(status)
            .reason(reason)
            .headers(Collections.singletonMap("Content-Type", Collections.singletonList(contentType)))
            .request(buildRequest("http://api.example.com/foo"))
            .body(body.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    /**
     * feign.Target 最小测试实现
     */
    private static final class TestTarget<T> implements Target<T> {

        private final Class<T> type;
        private final String name;

        private TestTarget(Class<T> type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String url() {
            return "http://api.example.com";
        }

        @Override
        public Request apply(RequestTemplate input) {
            return null;
        }
    }

    /**
     * 将 protected 方法提升为 public 便于测试
     */
    private static final class TestableLogger extends CFeignLogger {

        private TestableLogger(CFeignClientLogConfig config) {
            super(config);
        }

        @Override
        public void logRequest(String configKey, Level logLevel, Request request) {
            super.logRequest(configKey, logLevel, request);
        }

        @Override
        public Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {
            return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        }

        @Override
        public IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
            return super.logIOException(configKey, logLevel, ioe, elapsedTime);
        }
    }
}
