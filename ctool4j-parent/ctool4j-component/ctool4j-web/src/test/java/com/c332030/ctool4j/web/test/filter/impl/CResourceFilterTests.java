package com.c332030.ctool4j.web.test.filter.impl;

import com.c332030.ctool4j.web.constant.ResourceUrlConstants;
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
 * @since 2026/8/16
 */
public class CResourceFilterTests {

    private final CResourceFilter filter = new CResourceFilter();

    @Test
    public void doFilter_whenIgnoreResource() throws Exception {
        // 命中忽略资源 URL：返回 204 且不继续链
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(ResourceUrlConstants.FAVICON_ICO_URL);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Assertions.assertEquals(MockHttpServletResponse.SC_NO_CONTENT, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

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
