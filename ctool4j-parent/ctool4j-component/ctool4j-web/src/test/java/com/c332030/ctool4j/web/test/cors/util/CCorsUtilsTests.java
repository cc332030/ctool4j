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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CCorsUtils.handleOptions/handle/handleDo：跨域校验与响应头设置 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/Mockito 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CCorsUtils.handleOptions/handle/handleDo：跨域校验与响应头设置</h2>
 * <ul>
 *   <li>1.1 handleOptions_whenDisable（handleOptions_whenDisable）</li>
 *   <li>1.2 handleOptions_whenEnableAndOptions（handleOptions_whenEnableAndOptions）</li>
 *   <li>1.3 handleOptions_whenEnableAndNotOptions（handleOptions_whenEnableAndNotOptions）</li>
 *   <li>1.4 handleOptions_whenOptionsIgnoreCase（handleOptions_whenOptionsIgnoreCase）</li>
 *   <li>1.5 handleOptions_whenConfigNull（handleOptions_whenConfigNull）</li>
 *   <li>1.6 handle_whenEnable（handle_whenEnable）</li>
 *   <li>1.7 handle_whenDisable（handle_whenDisable）</li>
 *   <li>1.8 handleDo_whenNoOrigin（handleDo_whenNoOrigin）</li>
 *   <li>1.9 handleDo_whenSameOrigin（handleDo_whenSameOrigin）</li>
 *   <li>1.10 handleDo_whenOriginNotAllowed（handleDo_whenOriginNotAllowed）</li>
 *   <li>1.11 handleDo_whenMethodNotAllowed（handleDo_whenMethodNotAllowed）</li>
 *   <li>1.12 handleDo_whenAllowedHeadersAll（handleDo_whenAllowedHeadersAll）</li>
 *   <li>1.13 handleDo_whenAllowedHeadersSpecific（handleDo_whenAllowedHeadersSpecific）</li>
 *   <li>1.14 handleDo_whenExposedHeadersDefault（handleDo_whenExposedHeadersDefault）</li>
 *   <li>1.15 handleDo_whenExposedHeadersAll（handleDo_whenExposedHeadersAll）</li>
 *   <li>1.16 handleDo_whenExposedHeadersEmpty（handleDo_whenExposedHeadersEmpty）</li>
 *   <li>1.17 handle_whenEnable_shouldExposeHeaders（handle_whenEnable_shouldExposeHeaders）</li>
 *   <li>1.18 handleDo_whenExposedHeadersMultiple（handleDo_whenExposedHeadersMultiple）</li>
 *   <li>1.19 handleDo_whenExposedHeadersNull（handleDo_whenExposedHeadersNull）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CCorsUtilsTests {

    private CCorsConfig config;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * 每个用例执行后的清理
     */
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

    /**
     * 对应测试用例 1.1：handleOptions_whenDisable
     */
    @Test
    public void handleOptions_whenDisable() {
        // 反例：未开启跨域时，OPTIONS 请求不处理
        CCorsUtils.setConfig(config);
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
        Assertions.assertEquals(200, response.getStatus());
    }

    /**
     * 对应测试用例 1.2：handleOptions_whenEnableAndOptions
     */
    @Test
    public void handleOptions_whenEnableAndOptions() {
        // 正例：开启跨域且为 OPTIONS 请求时，返回 204 并视为已处理
        enable();
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertTrue(handled);
        Assertions.assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    /**
     * 对应测试用例 1.3：handleOptions_whenEnableAndNotOptions
     */
    @Test
    public void handleOptions_whenEnableAndNotOptions() {
        // 反例：开启跨域但非 OPTIONS 请求时，不处理
        enable();
        request.setMethod("GET");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
    }

    /**
     * 对应测试用例 1.4：handleOptions_whenOptionsIgnoreCase
     */
    @Test
    public void handleOptions_whenOptionsIgnoreCase() {
        // 边界：方法名大小写不敏感时仍视为 OPTIONS
        enable();
        request.setMethod("options");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertTrue(handled);
    }

    /**
     * 对应测试用例 1.5：handleOptions_whenConfigNull
     */
    @Test
    public void handleOptions_whenConfigNull() {
        // 异常：config 为 null 时异常被吞掉并返回 false
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
    }

    // ---------- handle ----------

    /**
     * 对应测试用例 1.6：handle_whenEnable
     */
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

    /**
     * 对应测试用例 1.7：handle_whenDisable
     */
    @Test
    public void handle_whenDisable() {
        // 反例：未开启跨域时，不设置任何跨域响应头
        CCorsUtils.setConfig(config);
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        CCorsUtils.handle(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // ---------- handleDo ----------

    /**
     * 对应测试用例 1.8：handleDo_whenNoOrigin
     */
    @Test
    public void handleDo_whenNoOrigin() {
        // 反例：无 Origin 请求头时，不做跨域处理
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.9：handleDo_whenSameOrigin
     */
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

    /**
     * 对应测试用例 1.10：handleDo_whenOriginNotAllowed
     */
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

    /**
     * 对应测试用例 1.11：handleDo_whenMethodNotAllowed
     */
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

    /**
     * 对应测试用例 1.12：handleDo_whenAllowedHeadersAll
     */
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

    /**
     * 对应测试用例 1.13：handleDo_whenAllowedHeadersSpecific
     */
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

    /**
     * 对应测试用例 1.14：handleDo_whenExposedHeadersDefault
     */
    @Test
    public void handleDo_whenExposedHeadersDefault() {
        // 正例：默认 exposedHeaders 仅暴露 Authorization（无其它非简单响应头）
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(HttpHeaders.AUTHORIZATION,
                response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.15：handleDo_whenExposedHeadersAll
     */
    @Test
    public void handleDo_whenExposedHeadersAll() {
        // 正例：exposedHeaders 含 ALL 时，Expose-Headers 使用通配符
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));
        config.setExposedHeaders(Collections.singleton(CCorsConfig.ALL));

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(CCorsConfig.ALL,
                response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.16：handleDo_whenExposedHeadersEmpty
     */
    @Test
    public void handleDo_whenExposedHeadersEmpty() {
        // 边界：exposedHeaders 为空集合时不设置 Expose-Headers
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));
        config.setExposedHeaders(Collections.emptySet());

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.17：handle_whenEnable_shouldExposeHeaders
     */
    @Test
    public void handle_whenEnable_shouldExposeHeaders() {
        // 正例：普通（非 OPTIONS）实际响应同样暴露 Expose-Headers，前端 JS 才能读取响应头
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        CCorsUtils.handle(request, response);

        Assertions.assertEquals(HttpHeaders.AUTHORIZATION,
                response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.18：handleDo_whenExposedHeadersMultiple
     */
    @Test
    public void handleDo_whenExposedHeadersMultiple() {
        // 正例：exposedHeaders 含多个头时，Expose-Headers 为逗号拼接的列表
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));

        Set<String> exposedHeaders = new LinkedHashSet<String>();
        exposedHeaders.add(HttpHeaders.AUTHORIZATION);
        exposedHeaders.add("X-TOKEN");
        config.setExposedHeaders(exposedHeaders);

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals("Authorization,X-TOKEN",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.19：handleDo_whenExposedHeadersNull
     */
    @Test
    public void handleDo_whenExposedHeadersNull() {
        // 边界：exposedHeaders 为 null 时不设置 Expose-Headers（空安全）
        enable();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");
        config.setAllowedOrigins(Collections.singleton("example.com"));
        config.setExposedHeaders(null);

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

}
