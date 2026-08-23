package com.c332030.ctool4j.spring.security.core;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

/**
 * <p>
 * Description: CAccessDeniedHandlerTests
 * </p>
 * <p>
 * 覆盖访问被拒绝时的错误响应输出：状态码 403、默认提示文案与请求路径拼接。
 * 通过 Spring 的 Mock 请求/响应运行完整 writeJsonError 链路，不依赖 Spring 容器。
 * </p>
 *
 * @since 2026/8/17
 */
class CAccessDeniedHandlerTests {

    private final CAccessDeniedHandler handler = new CAccessDeniedHandler();

        /**
     * 对应测试用例 1.1
     */
    @Test
    void testHandle_write403WithReasonAndUrl() throws Exception {
        // 正例：输出 403，文案为默认 ReasonPhrase + 请求路径
        val request = new MockHttpServletRequest("GET", "/api/user");
        val response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        Assertions.assertTrue(response.getContentType().contains("application/json"));
        val content = response.getContentAsString();
        Assertions.assertTrue(content.contains("403"));
        Assertions.assertTrue(content.contains(HttpStatus.FORBIDDEN.getReasonPhrase()));
        Assertions.assertTrue(content.contains("/api/user"));
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void testHandle_withQueryStringUsesRequestUri() throws Exception {
        // 边界：带查询串时仍以 requestURI 作为路径
        val request = new MockHttpServletRequest("GET", "/api/user");
        request.setQueryString("a=1");
        val response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        val content = response.getContentAsString();
        Assertions.assertTrue(content.contains("/api/user"));
        Assertions.assertFalse(content.contains("a=1"));
    }
}
