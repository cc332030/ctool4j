package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBusinessExceptionTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CBusinessExceptionTests {

    @Test
    public void errorOnly() {

        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"));
        Assertions.assertEquals("[100] boom", ex.getMessage());
        Assertions.assertEquals("100", ex.getError().getCode());
        Assertions.assertNull(ex.getMsgExtend());

    }

    @Test
    public void errorAndMsgExtend() {

        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"), "detail");
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());
        Assertions.assertEquals("detail", ex.getMsgExtend());

    }

    @Test
    public void errorAndCause() {

        Throwable cause = new IllegalStateException("cause");
        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"), cause);
        Assertions.assertEquals("[100] boom", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());

    }

    @Test
    public void errorMsgExtendCause() {

        Throwable cause = new IllegalStateException("cause");
        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"), "detail", cause);
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());
        Assertions.assertEquals("detail", ex.getMsgExtend());

    }

    @Test
    public void nullError() {

        CBusinessException ex = new CBusinessException(null, "only-extend");
        Assertions.assertEquals("only-extend", ex.getMessage());
        Assertions.assertNull(ex.getError());

    }

    /**
     * 测试用 ICRes 实现
     */
    @Getter
    @RequiredArgsConstructor
    static class TestRes implements ICRes<Object> {

        private final String code;
        private final String msg;

        static TestRes of(String code, String msg) {
            return new TestRes(code, msg);
        }

    }

}
