package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CThrowableHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CThrowableHandlerTests
 * </p>
 *
 * <p>覆盖 CThrowableHandler.handle：兜底返回默认 500 与固定消息</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 未识别异常 的处理结果。</li>
 *   <li>兜底返回默认 500 与固定消息{@code 未知异常}。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对未识别异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界）：兜底返回默认 500 与固定消息{@code 未知异常}。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：未识别异常的 {@code handle} 处理路径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路。</li>
 * </ul>
 * <h2>未识别异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证未识别异常处理结果</li>
 * </ul>
 *
 * <p>`com.c332030.ctool4j.web.exception.handler.CThrowableHandler`（CThrowableHandler）的测试用例</p>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CThrowableHandlerTests {

    private final CThrowableHandler handler = new CThrowableHandler();

        /**
         * 对应测试用例 1.1：验证未识别异常处理结果
         */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new RuntimeException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("未知异常", result.getMessage());
    }

}
