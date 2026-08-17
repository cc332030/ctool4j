package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CMethodArgumentNotValidExceptionHandler;
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
 * <p>覆盖 CMethodArgumentNotValidExceptionHandler.handle：拼接全部字段错误信息</p>
 *
 * @since 2026/8/16
 */
public class CMethodArgumentNotValidExceptionHandlerTests {

    private final CMethodArgumentNotValidExceptionHandler handler = new CMethodArgumentNotValidExceptionHandler();

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
     * 供构造 MethodParameter 使用的样例方法
     */
    @SuppressWarnings("unused")
    private static void sample(String name) {
        // 无实现
    }

}
