package com.c332030.ctool4j.web.cors.interceptor;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.CCorsOriginConfig;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;

/**
 * <p>
 * Description: CCorsInterceptorTests
 * </p>
 *
 * <p>覆盖 CCorsInterceptor.preHandle：未启用时放行、预检请求返回 false、普通请求放行</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CCorsInterceptor.preHandle：跨域头输出与 OPTIONS 预检处理 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/MockMvc 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CCorsInterceptor.preHandle：跨域头输出与 OPTIONS 预检处理</h2>
 * <ul>
 *   <li>1.1 preHandle_whenNotEnabled（preHandle_whenNotEnabled）</li>
 *   <li>1.2 preHandle_whenEnabledAndOptions（preHandle_whenEnabledAndOptions）</li>
 *   <li>1.3 preHandle_whenEnabledAndGet（preHandle_whenEnabledAndGet）</li>
 *   <li>1.4 preHandle_whenOriginDisabled（preHandle_whenOriginDisabled）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.1
 */

public class CCorsInterceptorTests {

    private final CCorsInterceptor interceptor = new CCorsInterceptor();

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
        CCorsUtils.setConfig(null);
    }

    /**
     * 对应测试用例 1.1：preHandle_whenNotEnabled
     */
    @Test
    public void preHandle_whenNotEnabled() {
        // 未启用跨域时放行
        boolean result = interceptor.preHandle(CHttpServletRequest.of(request), CHttpServletResponse.of(response), new Object());

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.2：preHandle_whenEnabledAndOptions
     */
    @Test
    public void preHandle_whenEnabledAndOptions() {
        // 启用跨域 + OPTIONS 预检：返回 false 且响应 204
        config.setEnable(true);
        CCorsUtils.setConfig(config);
        request.setMethod("OPTIONS");

        boolean result = interceptor.preHandle(CHttpServletRequest.of(request), CHttpServletResponse.of(response), new Object());

        Assertions.assertFalse(result);
        Assertions.assertEquals(MockHttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    /**
     * 对应测试用例 1.3：preHandle_whenEnabledAndGet
     */
    @Test
    public void preHandle_whenEnabledAndGet() {
        // 启用跨域 + 普通 GET：放行
        config.setEnable(true);
        CCorsUtils.setConfig(config);
        request.setMethod("GET");

        boolean result = interceptor.preHandle(CHttpServletRequest.of(request), CHttpServletResponse.of(response), new Object());

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.4：preHandle_whenOriginDisabled
     */
    @Test
    public void preHandle_whenOriginDisabled() {
        // 域名级未启用：放行且不输出跨域头
        config.setEnable(true);
        config.setOrigins(Collections.singletonMap("example.com", new CCorsOriginConfig()));
        CCorsUtils.setConfig(config);
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com");
        request.addHeader(HttpHeaders.HOST, "localhost:8080");

        boolean result = interceptor.preHandle(CHttpServletRequest.of(request), CHttpServletResponse.of(response), new Object());

        Assertions.assertTrue(result);
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

}
