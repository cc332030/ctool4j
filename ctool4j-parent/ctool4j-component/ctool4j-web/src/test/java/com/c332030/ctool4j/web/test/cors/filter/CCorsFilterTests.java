package com.c332030.ctool4j.web.test.cors.filter;

import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.filter.CCorsFilter;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.mockito.Mockito.*;

/**
 * <p>
 * Description: CCorsFilterTests
 * </p>
 *
 * <p>覆盖 CCorsFilter.doFilter：未启用时放行、预检请求直接 204 结束、普通请求放行</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CCorsFilter.doFilter：跨域头输出与 OPTIONS 预检处理 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/MockMvc 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CCorsFilter.doFilter：跨域头输出与 OPTIONS 预检处理</h2>
 * <ul>
 *   <li>1.1 doFilter_whenNotEnabled（doFilter_whenNotEnabled）</li>
 *   <li>1.2 doFilter_whenEnabledAndOptions（doFilter_whenEnabledAndOptions）</li>
 *   <li>1.3 doFilter_whenEnabledAndGet（doFilter_whenEnabledAndGet）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */

public class CCorsFilterTests {

    private final CCorsFilter filter = new CCorsFilter();

    private CCorsConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    /**
     * 每个用例执行后的清理
     */
    @AfterEach
    public void tearDown() {
        CCorsUtils.setConfig(null);
    }

    /**
     * 对应测试用例 1.1：doFilter_whenNotEnabled
     */
    @Test
    public void doFilter_whenNotEnabled() throws Exception {
        // 未启用跨域时原样放行
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    /**
     * 对应测试用例 1.2：doFilter_whenEnabledAndOptions
     */
    @Test
    public void doFilter_whenEnabledAndOptions() throws Exception {
        // 启用跨域 + OPTIONS 预检：设置 204 且不继续链
        config.setEnable(true);
        CCorsUtils.setConfig(config);
        request.setMethod("OPTIONS");

        filter.doFilter(request, response, chain);

        Assertions.assertEquals(MockHttpServletResponse.SC_NO_CONTENT, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    /**
     * 对应测试用例 1.3：doFilter_whenEnabledAndGet
     */
    @Test
    public void doFilter_whenEnabledAndGet() throws Exception {
        // 启用跨域 + 普通 GET：继续链
        config.setEnable(true);
        CCorsUtils.setConfig(config);
        request.setMethod("GET");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

}
