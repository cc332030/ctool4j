package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Description: CMethodArgumentNotValidExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 参数校验失败异常 的处理结果。</li>
 *   <li>返回 {@code CStrResult.error(...)}，可处理无字段错误场景。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对参数校验失败异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult.error(...)}，可处理无字段错误场景。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：参数校验失败异常的 {@code handle} 处理路径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路。</li>
 * </ul>
 * <h2>参数校验失败异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证参数校验失败异常处理结果</li>
 *   <li>1.3 异常对象为 null（非预期入参）：返回固定错误结果、不抛 NPE（handle_nullException）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.1
 */
public class CMethodArgumentNotValidExceptionHandlerTests {

    private final CMethodArgumentNotValidExceptionHandler handler = new CMethodArgumentNotValidExceptionHandler();

        /**
         * 对应测试用例 1.1：验证参数校验失败异常处理结果
         */
    @Test
    public void handle() throws NoSuchMethodException {
        val parameter = new MethodParameter(
            CMethodArgumentNotValidExceptionHandlerTests.class.getDeclaredMethod("sample", String.class),
            0
        );
        val bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(
            new FieldError("sample", "name", "不能为空"),
            new FieldError("sample", "age", "必须为正数")
        ));

        CStrResult<Void> result = handler.handle(new MethodArgumentNotValidException(parameter, bindingResult));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("name 不能为空，age 必须为正数", result.getMessage());
    }

        /**
         * 对应测试用例 1.2
         */
    @Test
    public void handle_whenNoFieldError() throws NoSuchMethodException {
        val parameter = new MethodParameter(
            CMethodArgumentNotValidExceptionHandlerTests.class.getDeclaredMethod("sample", String.class),
            0
        );
        val bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.emptyList());

        CStrResult<Void> result = handler.handle(new MethodArgumentNotValidException(parameter, bindingResult));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("", result.getMessage());
    }

    /**
     * 对应测试用例 1.3：异常对象为 null（非预期入参）返回固定错误结果、不抛 NPE
     * <p>Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null，该分支运行时不可达；
     * 用例固化"经容器显式传 null 也不 NPE"的兜底契约（兜底不读 {@code getBindingResult()}）。</p>
     */
    @Test
    public void handle_nullException() {
        // 边界：异常对象为 null
        CStrResult<Void> result = handler.handle(null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("参数校验失败", result.getMessage());
    }

    /**
     * 供构造 MethodParameter 使用的样例方法
     */
    @SuppressWarnings("unused")
    private static void sample(String name) {
        // 无实现
    }

}
