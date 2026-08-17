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
 * <p>覆盖 CHttpRequestMethodNotSupportedExceptionHandler.handle：统一返回默认 500</p>
 *
 * @since 2026/8/16
 */
public class CHttpRequestMethodNotSupportedExceptionHandlerTests {

    private final CHttpRequestMethodNotSupportedExceptionHandler handler =
        new CHttpRequestMethodNotSupportedExceptionHandler();

    @Test
    public void handle() {
        val e = new HttpRequestMethodNotSupportedException("DELETE", "method not supported");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertNotNull(result.getMessage());
    }

}
