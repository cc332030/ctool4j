package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CIllegalArgumentExceptionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CIllegalArgumentExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 CIllegalArgumentExceptionHandler.handle：统一返回默认 500 与异常消息</p>
 *
 * @since 2026/8/16
 * <p>`com.c332030.ctool4j.web.exception.handler.CIllegalArgumentExceptionHandler`（CIllegalArgumentExceptionHandler）的测试用例</p>
 */
public class CIllegalArgumentExceptionHandlerTests {

    private final CIllegalArgumentExceptionHandler handler = new CIllegalArgumentExceptionHandler();

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new IllegalArgumentException("illegal arg"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("illegal arg", result.getMessage());
    }

}
