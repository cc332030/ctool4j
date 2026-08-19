package com.c332030.ctool4j.feign.test.log;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import com.c332030.ctool4j.web.model.CRequestLog;
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
 * CFeignLogger 测试
 *
 * 覆盖：logRequest 的白名单/黑名单/全量开关判定、请求日志模型构建（method/path/headers/req）、
 * logAndRebufferResponse 重缓冲与取值、logIOException 异常信息设置、未记录时的降级返回
 *
 * @author c332030
 */
class CFeignLoggerTests {

    /** 被测 API 类型 */
    static class ApiClass {
    }

    private final CFeignClientLogConfig config = new CFeignClientLogConfig();
    private final TestableLogger logger = new TestableLogger(config);

    @AfterEach
    void clearThreadLocal() throws Exception {
        requestLogThreadLocal().remove();
    }

    @SuppressWarnings("unchecked")
    private ThreadLocal<CRequestLog> requestLogThreadLocal() throws Exception {
        Field field = CFeignLogger.class.getDeclaredField("REQUEST_THREAD_LOCAL");
        field.setAccessible(true);
        return (ThreadLocal<CRequestLog>) field.get(logger);
    }

    private CRequestLog currentRequestLog() throws Exception {
        return requestLogThreadLocal().get();
    }

    // ==================== enableLog 判定（logRequest 写入 REQUEST_THREAD_LOCAL） ====================

    @Test
    void testLogRequestDisabledDoesNothing() throws Exception {

        config.setEnable(false);
        requestLogThreadLocal().set(new CRequestLog());

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // enable=false 时不覆盖/写入本次标记，保留原值
        Assertions.assertNotNull(currentRequestLog());
    }

    @Test
    void testLogRequestHostWhiteListHit() throws Exception {

        config.setEnable(true);
        config.setHostWhiteList(CSet.of("api.example.com"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        CRequestLog requestLog = currentRequestLog();
        Assertions.assertNotNull(requestLog);
        Assertions.assertEquals("GET", requestLog.getMethod());
    }

    @Test
    void testLogRequestPathWhiteListHit() throws Exception {

        config.setEnable(true);
        config.setPathWhiteList(CSet.of("/foo"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://other.example.com/foo"));

        Assertions.assertNotNull(currentRequestLog());
    }

    @Test
    void testLogRequestApiWhiteListHit() throws Exception {

        config.setEnable(true);
        config.setApiWhiteList(CSet.of("ApiClass"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        Assertions.assertNotNull(currentRequestLog());
    }

    @Test
    void testLogRequestHostBlackListHit() throws Exception {

        config.setEnable(true);
        config.setHostBlackList(CSet.of("api.example.com"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // 命中黑名单且未命中白名单 -> 写入 null 覆盖
        Assertions.assertNull(currentRequestLog());
    }

    @Test
    void testLogRequestWhiteListWinsOverBlackList() throws Exception {

        config.setEnable(true);
        config.setHostWhiteList(CSet.of("api.example.com"));
        config.setHostBlackList(CSet.of("api.example.com"));

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        // 白名单优先于黑名单
        Assertions.assertNotNull(currentRequestLog());
    }

    @Test
    void testLogRequestNoListLogAllFalse() throws Exception {

        config.setEnable(true);
        config.setLogAll(false);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        Assertions.assertNull(currentRequestLog());
    }

    @Test
    void testLogRequestNoListLogAllTrue() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/foo"));

        Assertions.assertNotNull(currentRequestLog());
    }

    // ==================== 请求日志模型构建 ====================

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

    @Test
    void testSetRequestLogPathWithoutQuery() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        logger.logRequest("cfg", Logger.Level.FULL, buildRequest("http://api.example.com/order/list"));

        Assertions.assertEquals("/order/list", currentRequestLog().getPath());
    }

    @Test
    void testSetRequestLogBodyText() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        Request request = buildRequest("http://api.example.com/post", "{\"name\":\"张三\"}", "application/json");
        logger.logRequest("cfg", Logger.Level.FULL, request);

        CRequestLog requestLog = currentRequestLog();
        Assertions.assertEquals("{\"name\":\"张三\"}", requestLog.getReq());
    }

    @Test
    void testSetRequestLogBodyNonText() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        Request request = buildRequest("http://api.example.com/post",
            new byte[]{1, 2, 3}, "application/octet-stream");
        logger.logRequest("cfg", Logger.Level.FULL, request);

        Assertions.assertEquals("[not text body]", currentRequestLog().getReq());
    }

    @Test
    void testSetRequestLogBodyEmpty() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);

        Request request = buildRequest("http://api.example.com/post",
            new byte[0], "application/json");
        logger.logRequest("cfg", Logger.Level.FULL, request);

        // 空 body -> getBodyText 返回 null
        Assertions.assertNull(currentRequestLog().getReq());
    }

    @Test
    void testSetRequestLogHeadersImmutableWhenEnabled() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);
        config.setEnableHeader(true);

        Request request = buildRequest("http://api.example.com/foo", "X-Trace", Collections.singletonList("abc"));

        logger.logRequest("cfg", Logger.Level.FULL, request);

        Map<String, Collection<String>> headers = currentRequestLog().getHeaders();
        Assertions.assertNotNull(headers);
        Assertions.assertEquals(Collections.singletonList("abc"), headers.get("X-Trace"));
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, headers::clear);
    }

    @Test
    void testSetRequestLogHeadersNullWhenDisabled() throws Exception {

        config.setEnable(true);
        config.setLogAll(true);
        config.setEnableHeader(false);

        Request request = buildRequest("http://api.example.com/foo", "X-Trace", Collections.singletonList("abc"));

        logger.logRequest("cfg", Logger.Level.FULL, request);

        Assertions.assertNull(currentRequestLog().getHeaders());
    }

    // ==================== logAndRebufferResponse ====================

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
        // dealResponse 已清除线程标记，避免线程复用泄漏
        Assertions.assertNull(currentRequestLog());
    }

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

    /** feign.Target 最小测试实现 */
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

    /** 将 protected 方法提升为 public 便于测试 */
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
