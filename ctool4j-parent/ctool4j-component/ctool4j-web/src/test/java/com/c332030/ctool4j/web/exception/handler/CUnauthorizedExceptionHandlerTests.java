package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>
 * Description: CUnauthorizedExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 {@code CUnauthorizedExceptionHandler.handle}：业务码固定 401、消息取异常消息（为空回退 401 原因短语），
 * 随附 HTTP 响应状态 401（未授权对外以响应体业务码 401 为准）。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器调用 handle（与同目录其他处理器用例一致），不启容器。</li>
 *   <li>业务码随响应体断言；HTTP 响应状态通过 {@code @ResponseStatus} 注解固化（未授权附设 401，防回改）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：有消息、无消息、HTTP 状态声明。</li>
 *   <li>未覆盖：容器装配条件（{@code @ConditionalOnMissingExceptionHandler} 语义由该注解自身用例覆盖）与真实 MVC 链路。</li>
 * </ul>
 * <h2>handle 分支输出</h2>
 * <ul>
 *   <li>1.1 消息非空：code "401" + 原消息（handle_withMessage）</li>
 *   <li>1.2 消息为空：message 回退 401 状态原因短语（handle_withoutMessage）</li>
 *   <li>1.3 HTTP 响应状态声明为 401（handle_declaresHttp401）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.1
 * @see CUnauthorizedExceptionHandler
 * @see "doc/design/web/unauthorized-401.adoc"
 */
class CUnauthorizedExceptionHandlerTests {

    private final CUnauthorizedExceptionHandler handler = new CUnauthorizedExceptionHandler();

    /**
     * <p>对应测试用例 1.1：消息非空时透传消息，业务码固定 401</p>
     */
    @Test
    void handle_withMessage() {
        CStrResult<Void> result = handler.handle(new CUnauthorizedException("未授权"));

        Assertions.assertEquals("401", result.getCode());
        Assertions.assertEquals("未授权", result.getMessage());
        Assertions.assertNull(result.getData());
    }

    /**
     * <p>对应测试用例 1.2：消息为空时回退 401 状态原因短语</p>
     */
    @Test
    void handle_withoutMessage() {
        CStrResult<Void> result = handler.handle(new CUnauthorizedException());

        Assertions.assertEquals("401", result.getCode());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), result.getMessage());
    }

    /**
     * <p>对应测试用例 1.3：HTTP 响应状态显式声明为 401（本模块唯一设置 HTTP 响应状态的处理器）</p>
     */
    @Test
    void handle_declaresHttp401() throws NoSuchMethodException {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(
            CUnauthorizedExceptionHandler.class.getMethod("handle", CUnauthorizedException.class),
            ResponseStatus.class);

        Assertions.assertNotNull(responseStatus, "必须显式声明 HTTP 响应状态：未授权应为 401");
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseStatus.code());
    }

}
