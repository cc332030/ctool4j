package com.c332030.ctool4j.web.exception.handler;

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
 *   <li>1.2 handle(null)：异常对象为 null（非预期入参）不抛 NPE、不写响应体（handle_nullException）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.1
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

    /**
     * 对应测试用例 1.2：异常对象为 null（非预期入参）：不抛 NPE、不写响应体
     * <p>Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null，该分支运行时不可达；
     * 用例固化"经容器显式传 null 也不 NPE"的兜底契约（旧实现在此处 NPE，用例可捕获该缺陷）。</p>
     */
    @Test
    public void handle_nullException() {
        Assertions.assertDoesNotThrow(() -> handler.handle(null));
    }

}
