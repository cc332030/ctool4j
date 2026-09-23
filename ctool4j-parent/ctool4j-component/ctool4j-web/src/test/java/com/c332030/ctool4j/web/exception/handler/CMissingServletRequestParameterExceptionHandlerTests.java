package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;

/**
 * <p>
 * Description: CMissingServletRequestParameterExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 必填请求参数缺失异常 的处理结果。</li>
 *   <li>断言分两层：统一错误结果（错误码/文案），以及文案<b>保留参数名且不含</b>异常自身消息。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对必填请求参数缺失异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据 Spring 该异常的消息形态：{@code Required request parameter 'id' for method parameter type java.lang.Long is not present}，
 *   含 Java 类型名，属不应透出的内部细节。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult} 错误结果且文案保留参数名。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：必填请求参数缺失异常的 {@code handle} 处理路径与文案口径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路；其余请求绑定异常（如必填请求头缺失）。</li>
 * </ul>
 * <h2>必填请求参数缺失异常处理</h2>
 * <ul>
 *   <li>1.1 必填请求参数缺失异常的处理结果（handle）</li>
 *   <li>1.2 参数名为 null：只返回固定文案、不拼 {@code null}（handle_nullParameterName）</li>
 *   <li>1.3 异常对象为 null（非预期入参）：返回固定文案、不抛 NPE（handle_nullException）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.1
 */
public class CMissingServletRequestParameterExceptionHandlerTests {

    private final CMissingServletRequestParameterExceptionHandler handler =
        new CMissingServletRequestParameterExceptionHandler();

    /**
     * 对应测试用例 1.1：验证必填请求参数缺失异常的处理结果
     */
    @Test
    public void handle() {
        val e = new MissingServletRequestParameterException("id", "java.lang.Long");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        // 保留参数名：调用方据此修正请求
        Assertions.assertEquals("缺少必填参数：id", result.getMessage());
        // 不透出异常自身消息（含 Java 类型名）
        Assertions.assertFalse(result.getMessage().contains("Required request parameter"));
    }

    /**
     * 对应测试用例 1.2：参数名为 null 时只返回固定文案（不拼 {@code null}）
     */
    @Test
    public void handle_nullParameterName() {
        // 边界：参数名为 null（构造器的 parameterName 传 null）
        val e = new MissingServletRequestParameterException(null, "java.lang.Long");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("缺少必填参数", result.getMessage());
    }

    /**
     * 对应测试用例 1.3：异常对象为 null（非预期入参）返回固定文案、不抛 NPE
     * <p>Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null，该分支运行时不可达；
     * 用例固化"经容器显式传 null 也不 NPE"的兜底契约（兜底不读 {@code getParameterName()}）。</p>
     */
    @Test
    public void handle_nullException() {
        // 边界：异常对象为 null
        CStrResult<Void> result = handler.handle(null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("缺少必填参数", result.getMessage());
    }

}
