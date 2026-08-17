package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CCBusinessExceptionHandler;
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
 * @since 2026/8/16
 */
public class CCBusinessExceptionHandlerTests {

    private final CCBusinessExceptionHandler handler = new CCBusinessExceptionHandler();

    @Test
    public void handle_whenErrorNull() {
        // 边界：error 为 null 时返回默认 500
        val e = new CBusinessException(null, "only-extend");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("only-extend", result.getMessage());
    }

    @Test
    public void handle_whenErrorWithoutMsgExtend() {
        // 正例：error 非 null 且无扩展信息，message 取错误消息本身
        val e = new CBusinessException(TestRes.of("100", "boom"));

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("100", result.getCode());
        Assertions.assertEquals("boom", result.getMessage());
    }

    @Test
    public void handle_whenErrorWithMsgExtend() {
        // 正例：error 非 null 且有扩展信息，message 追加扩展
        val e = new CBusinessException(TestRes.of("100", "boom"), "detail");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("100", result.getCode());
        Assertions.assertEquals("boom: detail", result.getMessage());
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
