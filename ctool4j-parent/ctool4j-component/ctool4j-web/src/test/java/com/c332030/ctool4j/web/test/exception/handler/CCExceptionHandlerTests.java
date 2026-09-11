package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.core.exception.CException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CCExceptionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 CCExceptionHandler.handle：统一返回默认 500 与异常消息</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 通用异常 的处理结果。</li>
 *   <li>返回 {@code CStrResult.error(e.getMessage())}（code 默认 500）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对通用异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult.error(e.getMessage())}（code 默认 500）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：通用异常的 {@code handle} 处理路径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路。</li>
 * </ul>
 * <h2>通用异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证通用异常处理结果</li>
 * </ul>
 *
 * <p>`com.c332030.ctool4j.web.exception.handler.CCExceptionHandler`（CCExceptionHandler）的测试用例</p>
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CCExceptionHandlerTests {

    private final CCExceptionHandler handler = new CCExceptionHandler();

        /**
         * 对应测试用例 1.1：验证通用异常处理结果
         */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new CException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("boom", result.getMessage());
    }

}
