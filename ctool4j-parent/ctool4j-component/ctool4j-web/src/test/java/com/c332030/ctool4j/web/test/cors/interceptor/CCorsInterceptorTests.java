package com.c332030.ctool4j.web.test.cors.interceptor;

import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.interceptor.CCorsInterceptor;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * <p>
 * Description: CCorsInterceptorTests
 * </p>
 *
 * <p>覆盖 CCorsInterceptor.preHandle：未启用时放行、预检请求返回 false、普通请求放行</p>
 *
 * @since 2026/8/16
 */
public class CCorsInterceptorTests {

    private final CCorsInterceptor interceptor = new CCorsInterceptor();

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
        CCorsUtils.setConfig(null);
    }

    @Test
    public void preHandle_whenNotEnabled() {
        // 未启用跨域时放行
        boolean result = interceptor.preHandle(request, response, new Object());

        Assertions.assertTrue(result);
    }

    @Test
    public void preHandle_whenEnabledAndOptions() {
        // 启用跨域 + OPTIONS 预检：返回 false 且响应 204
        config.setEnable(true);
        CCorsUtils.setConfig(config);
        request.setMethod("OPTIONS");

        boolean result = interceptor.preHandle(request, response, new Object());

        Assertions.assertFalse(result);
        Assertions.assertEquals(MockHttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    public void preHandle_whenEnabledAndGet() {
        // 启用跨域 + 普通 GET：放行
        config.setEnable(true);
        CCorsUtils.setConfig(config);
        request.setMethod("GET");

        boolean result = interceptor.preHandle(request, response, new Object());

        Assertions.assertTrue(result);
    }

}
