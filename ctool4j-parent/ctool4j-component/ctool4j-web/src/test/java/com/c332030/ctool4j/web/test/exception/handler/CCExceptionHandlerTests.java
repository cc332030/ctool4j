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
 * @since 2026/8/16
 * <p>`com.c332030.ctool4j.web.exception.handler.CCExceptionHandler`（CCExceptionHandler）的测试用例</p>
 */
public class CCExceptionHandlerTests {

    private final CCExceptionHandler handler = new CCExceptionHandler();

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new CException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("boom", result.getMessage());
    }

}
