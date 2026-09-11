package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CHttpRequestMethodNotSupportedExceptionHandler;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * <p>
 * Description: CHttpRequestMethodNotSupportedExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 请求方法不支持异常 的处理结果。</li>
 *   <li>返回 {@code CStrResult} 错误结果。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对请求方法不支持异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult} 错误结果。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：请求方法不支持异常的 {@code handle} 处理路径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路。</li>
 * </ul>
 * <h2>请求方法不支持异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证请求方法不支持异常处理结果</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CHttpRequestMethodNotSupportedExceptionHandlerTests {

    private final CHttpRequestMethodNotSupportedExceptionHandler handler =
        new CHttpRequestMethodNotSupportedExceptionHandler();

        /**
         * 对应测试用例 1.1：验证请求方法不支持异常处理结果
         */
    @Test
    public void handle() {
        val e = new HttpRequestMethodNotSupportedException("DELETE", "method not supported");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertNotNull(result.getMessage());
    }

}
