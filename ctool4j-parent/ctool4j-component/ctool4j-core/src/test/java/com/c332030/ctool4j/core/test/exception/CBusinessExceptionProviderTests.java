package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.exception.CBusinessExceptionProvider;
import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBusinessExceptionProviderTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CBusinessExceptionProviderTests {

    @Test
    public void getExceptionFunction() {

        CBusinessExceptionProvider provider = new CBusinessExceptionProvider();
        CTriFunction<ICRes<?>, String, Throwable, CBusinessException> fn = provider.getExceptionFunction();
        Assertions.assertNotNull(fn);

        Throwable cause = new IllegalStateException("cause");
        CBusinessException ex = fn.apply(CBusinessExceptionTestsRes.of("100", "boom"), "detail", cause);
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());
        Assertions.assertEquals("detail", ex.getMsgExtend());

    }

    @Test
    public void getExceptionFunction_nullExtend() {

        CBusinessExceptionProvider provider = new CBusinessExceptionProvider();
        CTriFunction<ICRes<?>, String, Throwable, CBusinessException> fn = provider.getExceptionFunction();

        CBusinessException ex = fn.apply(CBusinessExceptionTestsRes.of("100", "boom"), null, null);
        Assertions.assertEquals("[100] boom", ex.getMessage());

    }

    /**
     * 测试用 ICRes 实现
     */
    @Getter
    @RequiredArgsConstructor
    static class CBusinessExceptionTestsRes implements ICRes<Object> {

        private final String code;
        private final String msg;

        static CBusinessExceptionTestsRes of(String code, String msg) {
            return new CBusinessExceptionTestsRes(code, msg);
        }

    }

}
