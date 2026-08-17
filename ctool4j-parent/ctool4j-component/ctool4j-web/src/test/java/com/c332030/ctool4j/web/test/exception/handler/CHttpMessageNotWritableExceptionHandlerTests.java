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
 * <p>覆盖 CHttpMessageNotWritableExceptionHandler.handle：仅记录日志、不抛出异常</p>
 *
 * @since 2026/8/16
 */
public class CHttpMessageNotWritableExceptionHandlerTests {

    private final CHttpMessageNotWritableExceptionHandler handler = new CHttpMessageNotWritableExceptionHandler();

    @Test
    public void handle() {
        Assertions.assertDoesNotThrow(() -> handler.handle(new HttpMessageNotWritableException("not writable")));
    }

}
