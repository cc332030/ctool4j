package com.c332030.ctool4j.web.test.filter.impl;

import com.c332030.ctool4j.web.constant.CResourceUrlConstants;
import com.c332030.ctool4j.web.filter.impl.CResourceFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.mockito.Mockito.*;

/**
 * <p>
 * Description: CResourceFilterTests
 * </p>
 *
 * <p>覆盖 CResourceFilter.doFilter：命中忽略资源 URL 时返回 204，其余放行</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CResourceFilter.doFilter：忽略静态资源 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/MockMvc 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CResourceFilter.doFilter：忽略静态资源</h2>
 * <ul>
 *   <li>1.1 doFilter_whenIgnoreResource（doFilter_whenIgnoreResource）</li>
 *   <li>1.2 doFilter_whenNormalResource（doFilter_whenNormalResource）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */

public class CResourceFilterTests {

    private final CResourceFilter filter = new CResourceFilter();

    /**
     * 对应测试用例 1.1：doFilter_whenIgnoreResource
     */
    @Test
    public void doFilter_whenIgnoreResource() throws Exception {
        // 命中忽略资源 URL：返回 204 且不继续链
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(CResourceUrlConstants.FAVICON_ICO_URL);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Assertions.assertEquals(MockHttpServletResponse.SC_NO_CONTENT, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    /**
     * 对应测试用例 1.2：doFilter_whenNormalResource
     */
    @Test
    public void doFilter_whenNormalResource() throws Exception {
        // 未命中忽略资源 URL：放行
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

}
