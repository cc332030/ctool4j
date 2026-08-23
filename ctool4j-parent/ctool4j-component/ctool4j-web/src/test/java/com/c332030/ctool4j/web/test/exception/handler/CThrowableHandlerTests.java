package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CThrowableHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CThrowableHandlerTests
 * </p>
 *
 * <p>覆盖 CThrowableHandler.handle：兜底返回默认 500 与固定消息</p>
 *
 * @since 2026/8/16
 * <p>`com.c332030.ctool4j.web.exception.handler.CThrowableHandler`（CThrowableHandler）的测试用例</p>
 */
public class CThrowableHandlerTests {

    private final CThrowableHandler handler = new CThrowableHandler();

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new RuntimeException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("未知异常", result.getMessage());
    }

}
