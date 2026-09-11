package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.web.exception.handler.CHttpMessageNotWritableExceptionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * <p>
 * Description: CHttpMessageNotWritableExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 响应消息不可写异常 的处理结果。</li>
 *   <li>返回错误结果/直接结束响应，不抛出。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对响应消息不可写异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界）：返回错误结果/直接结束响应，不抛出。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：响应消息不可写异常的 {@code handle} 处理路径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路。</li>
 * </ul>
 * <h2>响应消息不可写异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证响应消息不可写异常处理结果</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CHttpMessageNotWritableExceptionHandlerTests {

    private final CHttpMessageNotWritableExceptionHandler handler = new CHttpMessageNotWritableExceptionHandler();

        /**
         * 对应测试用例 1.1：验证响应消息不可写异常处理结果
         */
    @Test
    public void handle() {
        Assertions.assertDoesNotThrow(() -> handler.handle(new HttpMessageNotWritableException("not writable")));
    }

}
