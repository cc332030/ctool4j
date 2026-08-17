package com.c332030.ctool4j.web.test.cors.util;

import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CCorsUtilsTests
 * </p>
 *
 * <p>覆盖 CCorsUtils 的 handleOptions/handle/handleDo 跨域逻辑，
 * 不依赖 Spring 容器，通过静态 setter 注入 CCorsConfig</p>
 *
 * @since 2026/8/16
 */
public class CCorsUtilsTests {

    private CCorsConfig config;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    public void tearDown() {
        // 还原静态 config，避免污染其他用例
        CCorsUtils.setConfig(null);
    }

    private void enable() {
        config.setEnable(Boolean.TRUE);
        CCorsUtils.setConfig(config);
    }

    // ---------- handleOptions ----------

    @Test
    public void handleOptions_whenDisable() {
        // 反例：未开启跨域时，OPTIONS 请求不处理
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    public void handleOptions_whenEnableAndOptions() {
        // 正例：开启跨域且为 OPTIONS 请求时，返回 204 并视为已处理
        enable();
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertTrue(handled);
        Assertions.assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    public void handleOptions_whenEnableAndNotOptions() {
        // 反例：开启跨域但非 OPTIONS 请求时，不处理
        enable();
        request.setMethod("GET");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
    }

    @Test
    public void handleOptions_whenOptionsIgnoreCase() {
        // 边界：方法名大小写不敏感时仍视为 OPTIONS
        enable();
        request.setMethod("options");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertTrue(handled);
    }

    @Test
    public void handleOptions_whenConfigNull() {
        // 异常：config 为 null 时异常被吞掉并返回 false
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
    }

    // ---------- handle ----------

    @Test
    public void handle_whenEnable() {
        // 正例：开启跨域时委托 handleDo 设置跨域响应头
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        CCorsUtils.handle(request, response);

        Assertions.assertEquals("https://example.com",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handle_whenDisable() {
        // 反例：未开启跨域时，不设置任何跨域响应头
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        CCorsUtils.handle(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // ---------- handleDo ----------

    @Test
    public void handleDo_whenNoOrigin() {
        // 反例：无 Origin 请求头时，不做跨域处理
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handleDo_whenSameOrigin() {
        // 反例：同源请求（HOST 与 Origin 主机一致）时，不设置跨域响应头
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "http://localhost:8080");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handleDo_whenOriginNotAllowed() {
        // 反例：Origin 不在允许列表中时，不设置跨域响应头
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://other.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handleDo_whenMethodNotAllowed() {
        // 反例：请求方法不在允许方法列表中时，不设置跨域响应头
        enable();
        request.setMethod("DELETE");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));
        config.setAllowedMethods(new LinkedHashSet<String>(Collections.singletonList("GET")));

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handleDo_whenAllowedHeadersAll() {
        // 正例：允许全部请求头时，ALLOW_HEADERS 为通配符
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));
        config.setAllowedHeaders(Collections.singleton(CCorsConfig.ALL));

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals("https://example.com",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals(CCorsConfig.ALL,
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        Assertions.assertEquals("true",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        Assertions.assertEquals("GET",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    public void handleDo_whenAllowedHeadersSpecific() {
        // 正例：允许指定请求头时，ALLOW_HEADERS 为逗号拼接的列表
        enable();
        request.setMethod("POST");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));
        config.setAllowedMethods(Collections.singleton("POST"));

        Set<String> allowedHeaders = new LinkedHashSet<String>();
        allowedHeaders.add(HttpHeaders.AUTHORIZATION);
        allowedHeaders.add(HttpHeaders.CONTENT_TYPE);
        config.setAllowedHeaders(allowedHeaders);

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals("https://example.com",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals("Authorization,Content-Type",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        Assertions.assertEquals("true",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        Assertions.assertEquals("POST",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

}
