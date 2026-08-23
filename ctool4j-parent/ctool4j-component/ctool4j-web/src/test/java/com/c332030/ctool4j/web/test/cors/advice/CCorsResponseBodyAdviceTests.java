package com.c332030.ctool4j.web.test.cors.advice;

import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.advice.CCorsResponseBodyAdvice;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.HashSet;

/**
 * <p>
 * Description: CCorsResponseBodyAdviceTests
 * </p>
 *
 * <p>覆盖 CCorsResponseBodyAdvice.beforeBodyWrite：原样返回响应体，启用跨域时设置响应头</p>
 *
 * @since 2026/8/16
 */

public class CCorsResponseBodyAdviceTests {

    private final CCorsResponseBodyAdvice advice = new CCorsResponseBodyAdvice();

    private CCorsConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() throws Exception {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // MethodParameter 需基于具体方法构造，parameterIndex=-1 表示方法返回值
        parameter = new MethodParameter(getClass().getDeclaredMethod("beforeBodyWrite_whenNotEnabled"), -1);
    }

    private MethodParameter parameter;

    @AfterEach
    public void tearDown() {
        CCorsUtils.setConfig(null);
    }

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void beforeBodyWrite_whenNotEnabled() {
        // 未启用跨域时原样返回响应体
        val body = new Object();
        Object result = advice.beforeBodyWrite(
            body, parameter, MediaType.APPLICATION_JSON, null,
            request, response
        );

        Assertions.assertSame(body, result);
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void beforeBodyWrite_whenEnabled() {
        // 启用跨域时设置响应头并原样返回响应体；
        // allowedOrigins 需包含 Origin 的 host（handleDo 用 getHostWithPort 归一化后匹配）
        config.setEnable(true);
        config.setAllowedOrigins(new HashSet<>(Collections.singletonList("example.com")));
        CCorsUtils.setConfig(config);
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "http://example.com");
        val body = new Object();

        Object result = advice.beforeBodyWrite(
            body, parameter, MediaType.APPLICATION_JSON, null,
            request, response
        );

        Assertions.assertSame(body, result);
        Assertions.assertEquals(
            "http://example.com",
            response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
        );
    }

}
