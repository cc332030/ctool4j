package com.c332030.ctool4j.spring.security.util;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CSecurityResponseWriterTests
 * </p>
 *
 * <p>覆盖 {@code CSecurityResponseWriter.writeJsonError} 两个重载：状态码、内容类型、字符集、响应体文案（默认文案与自定义文案）。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CSecurityResponseWriter.writeJsonError：安全错误响应写出 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletResponse 构造真实响应场景，贴近真实使用，不依赖外部服务。</li>
 *   <li>只断言写出结果（状态码 / 内容类型 / 编码 / 响应体），不断言序列化实现细节。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 *
 * <h2>CSecurityResponseWriter.writeJsonError：安全错误响应写出</h2>
 * <ul>
 *   <li>1.1 writeJsonError_unauthorizedDefaultMessage（未认证：状态码与内容类型、默认文案）</li>
 *   <li>1.2 writeJsonError_customMessage（自定义文案透传）</li>
 *   <li>1.3 writeJsonError_blankMessageFallsBack（空白文案回退状态码默认文案）</li>
 *   <li>1.4 writeJsonError_forbidden（403 用法，与服务端拒绝路径一致）</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public class CSecurityResponseWriterTests {

    /**
     * 对应测试用例 1.1：未认证默认文案
     */
    @Test
    public void writeJsonError_unauthorizedDefaultMessage() throws Exception {

        // 正例：401 + application/json + UTF-8，响应体含状态码默认文案
        val response = new MockHttpServletResponse();

        CSecurityResponseWriter.writeJsonError(HttpStatus.UNAUTHORIZED, response);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        Assertions.assertTrue(response.getContentType().contains("application/json"));
        Assertions.assertEquals(StandardCharsets.UTF_8.name(), response.getCharacterEncoding());

        val content = response.getContentAsString();
        Assertions.assertTrue(content.contains(String.valueOf(HttpStatus.UNAUTHORIZED.value())));
        Assertions.assertTrue(content.contains(HttpStatus.UNAUTHORIZED.getReasonPhrase()));

    }

    /**
     * 对应测试用例 1.2：自定义文案透传
     */
    @Test
    public void writeJsonError_customMessage() throws Exception {

        // 正例：入参文案进入响应体
        val response = new MockHttpServletResponse();

        CSecurityResponseWriter.writeJsonError(HttpStatus.UNAUTHORIZED, "无有效登录用户", response);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("无有效登录用户"));

    }

    /**
     * 对应测试用例 1.3：空白文案回退状态码默认文案
     */
    @Test
    public void writeJsonError_blankMessageFallsBack() throws Exception {

        // 边界：message 为空白时回退为状态码默认文案，不写出空串
        val response = new MockHttpServletResponse();

        CSecurityResponseWriter.writeJsonError(HttpStatus.FORBIDDEN, "   ", response);

        val content = response.getContentAsString();
        Assertions.assertTrue(content.contains(HttpStatus.FORBIDDEN.getReasonPhrase()));

    }

    /**
     * 对应测试用例 1.4：403 用法
     */
    @Test
    public void writeJsonError_forbidden() {

        // 正例：访问拒绝路径（服务端过滤器/处理器统一走这里）
        val response = new MockHttpServletResponse();

        CSecurityResponseWriter.writeJsonError(HttpStatus.FORBIDDEN, response);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

    }

}
