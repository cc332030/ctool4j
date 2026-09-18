package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCBusinessExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 CCBusinessExceptionHandler.handle：error 为 null 时回退到默认 500，
 * error 非 null 时透传业务错误码并拼接扩展信息</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 {@code handle} 的 error 为 null / 非 null 无扩展 / 非 null 有扩展三条分支。</li>
 *   <li>error 为 null 时回退到默认 500；error 非 null 时透传业务错误码并拼接扩展信息。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 error 兜底、错误码透传、扩展信息拼接的约定。</li>
 *   <li>依据测试方法（分支覆盖/边界）：error null、error 无 msgExtend、error 有 msgExtend。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：error 为 null 返回默认 500；error 非 null 无扩展返回错误消息本身；error 非 null 有扩展拼接 {@code msg: extend}。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常链路（当前直接调用 handler）。</li>
 * </ul>
 * <h2>业务异常处理</h2>
 * <ul>
 *   <li>1.1 error 为 null：返回默认 500 + 扩展消息（{@code handle_whenErrorNull}）</li>
 *   <li>1.2 error 非 null 无扩展：透传错误码与错误消息（{@code handle_whenErrorWithoutMsgExtend}）</li>
 *   <li>1.3 error 非 null 有扩展：追加扩展信息（{@code handle_whenErrorWithMsgExtend}）</li>
 *   <li>1.4 异常对象为 null（非预期入参）：返回默认错误结果、不抛 NPE（{@code handle_whenExceptionNull}）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/8/16
 * @version 1.1
 */
public class CCBusinessExceptionHandlerTests {

    private final CCBusinessExceptionHandler handler = new CCBusinessExceptionHandler();

        /**
         * 对应测试用例 1.1：error 为 null：返回默认 500 + 扩展消息（{@code handle_whenErrorNull}）
         */
    @Test
    public void handle_whenErrorNull() {
        // 边界：error 为 null 时返回默认 500
        val e = new CBusinessException(null, "only-extend");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("only-extend", result.getMessage());
    }

        /**
         * 对应测试用例 1.2：error 非 null 无扩展：透传错误码与错误消息（{@code handle_whenErrorWithoutMsgExtend}）
         */
    @Test
    public void handle_whenErrorWithoutMsgExtend() {
        // 正例：error 非 null 且无扩展信息，message 取错误消息本身
        val e = new CBusinessException(TestRes.of("100", "boom"));

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("100", result.getCode());
        Assertions.assertEquals("boom", result.getMessage());
    }

        /**
         * 对应测试用例 1.3：error 非 null 有扩展：追加扩展信息（{@code handle_whenErrorWithMsgExtend}）
         */
    @Test
    public void handle_whenErrorWithMsgExtend() {
        // 正例：error 非 null 且有扩展信息，message 追加扩展
        val e = new CBusinessException(TestRes.of("100", "boom"), "detail");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("100", result.getCode());
        Assertions.assertEquals("boom: detail", result.getMessage());
    }

    /**
     * 对应测试用例 1.4：异常对象为 null（非预期入参）返回默认错误结果、不抛 NPE
     * <p>Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null，该分支运行时不可达；
     * 用例固化"经容器显式传 null 也不 NPE"的兜底契约（兜底不取异常内容，故 message 为固定文案）。</p>
     */
    @Test
    public void handle_whenExceptionNull() {
        // 边界：异常对象为 null
        CStrResult<Void> result = handler.handle(null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("业务异常", result.getMessage());
    }

    /**
     * 测试用 ICRes 实现
     */
    @Getter
    @RequiredArgsConstructor
    static class TestRes implements ICRes<String> {

        private final String code;
        private final String msg;

        static TestRes of(String code, String msg) {
            return new TestRes(code, msg);
        }

    }

}
