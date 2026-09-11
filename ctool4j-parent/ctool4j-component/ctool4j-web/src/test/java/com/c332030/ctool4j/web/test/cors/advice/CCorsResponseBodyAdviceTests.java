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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CCorsResponseBodyAdvice.beforeBodyWrite：输出 CORS 头并原样返回响应体 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/MockMvc 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CCorsResponseBodyAdvice.beforeBodyWrite：输出 CORS 头并原样返回响应体</h2>
 * <ul>
 *   <li>1.1 beforeBodyWrite_whenNotEnabled（beforeBodyWrite_whenNotEnabled）</li>
 *   <li>1.2 beforeBodyWrite_whenEnabled（beforeBodyWrite_whenEnabled）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */

public class CCorsResponseBodyAdviceTests {

    private final CCorsResponseBodyAdvice advice = new CCorsResponseBodyAdvice();

    private CCorsConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() throws Exception {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // MethodParameter 需基于具体方法构造，parameterIndex=-1 表示方法返回值
        parameter = new MethodParameter(getClass().getDeclaredMethod("beforeBodyWrite_whenNotEnabled"), -1);
    }

    private MethodParameter parameter;

    /**
     * 每个用例执行后的清理
     */
    @AfterEach
    public void tearDown() {
        CCorsUtils.setConfig(null);
    }

    /**
     * 对应测试用例 1.1：beforeBodyWrite_whenNotEnabled
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
     * 对应测试用例 1.2：beforeBodyWrite_whenEnabled
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
