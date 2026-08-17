package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.web.exception.handler.CClientAbortExceptionHandler;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CClientAbortExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 CClientAbortExceptionHandler.handle：仅记录日志、不抛出异常</p>
 *
 * @since 2026/8/16
 */
public class CClientAbortExceptionHandlerTests {

    private final CClientAbortExceptionHandler handler = new CClientAbortExceptionHandler();

    @Test
    public void handle() {
        Assertions.assertDoesNotThrow(() -> handler.handle(new ClientAbortException("client abort")));
    }

}
