package com.c332030.ctool4j.spring.security.core;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * Description: CAuthenticationEntryPointTests
 * </p>
 * <p>
 * 覆盖未认证访问时的错误响应输出：按异常类型生成提示文案并输出 401。
 * 通过 Spring 的 Mock 请求/响应运行完整 writeJsonError 链路，不依赖 Spring 容器。
 * </p>
 *
 * @since 2026/8/17
 */
class CAuthenticationEntryPointTests {

    private final CAuthenticationEntryPoint entryPoint = new CAuthenticationEntryPoint();

    private String run(AuthenticationException ex) throws Exception {
        val request = new MockHttpServletRequest("GET", "/api/user");
        val response = new MockHttpServletResponse();
        entryPoint.commence(request, response, ex);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        Assertions.assertTrue(response.getContentType().contains("application/json"));
        return response.getContentAsString();
    }

        /**
     * 对应测试用例 1.1
     */
    @Test
    void testCommence_credentialsNotFound() throws Exception {
        // 正例：无有效登录用户
        val content = run(new AuthenticationCredentialsNotFoundException("no user"));
        Assertions.assertTrue(content.contains("无有效登录用户"));
        Assertions.assertTrue(content.contains("/api/user"));
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void testCommence_badCredentials() throws Exception {
        // 正例：凭证错误，提示为异常消息
        val content = run(new BadCredentialsException("bad token"));
        Assertions.assertTrue(content.contains("bad token"));
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    void testCommence_otherExceptionUsesDefaultReason() throws Exception {
        // 边界：其他认证异常时提示回退为 401 默认文案
        val content = run(new AuthenticationException("other") {
            private static final long serialVersionUID = 1L;
        });
        Assertions.assertTrue(content.contains(HttpStatus.UNAUTHORIZED.getReasonPhrase()));
    }
}
