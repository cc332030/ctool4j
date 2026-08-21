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
 * @since 2026/8/16
 */

public class CCorsFilterTests {

    private final CCorsFilter filter = new CCorsFilter();

    private CCorsConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    public void setUp() {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @AfterEach
    public void tearDown() {
        CCorsUtils.setConfig(null);
    }

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void doFilter_whenNotEnabled() throws Exception {
        // 未启用跨域时原样放行
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    /**
     * 对应测试用例 1.2
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
     * 对应测试用例 1.3
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
