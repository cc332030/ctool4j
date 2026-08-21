package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.ICBusinessExceptionProvider;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: ICBusinessExceptionProviderTests
 * </p>
 *
 * @since 2025/12/12
 */
public class ICBusinessExceptionProviderTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void defaultGetMessageExceptionFunction_throws() {

        ICBusinessExceptionProvider<RuntimeException> provider = new ICBusinessExceptionProvider<RuntimeException>() {
        };
        CBiFunction<String, Throwable, RuntimeException> fn = provider.getMessageExceptionFunction();

        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> fn.apply("msg", null));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void getExceptionFunction_delegates() {

        ICBusinessExceptionProvider<IllegalStateException> provider = new ICBusinessExceptionProvider<IllegalStateException>() {
            @Override
            public CBiFunction<String, Throwable, IllegalStateException> getMessageExceptionFunction() {
                return (message, cause) -> new IllegalStateException(message, cause);
            }
        };

        CTriFunction<ICRes<?>, String, Throwable, IllegalStateException> fn = provider.getExceptionFunction();
        Throwable cause = new RuntimeException("cause");
        IllegalStateException ex = fn.apply(ICResTestsRes.of("200", "ok"), "detail", cause);

        Assertions.assertEquals("[200] ok: detail", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void getExceptionFunction_nullRes() {

        ICBusinessExceptionProvider<IllegalStateException> provider = new ICBusinessExceptionProvider<IllegalStateException>() {
            @Override
            public CBiFunction<String, Throwable, IllegalStateException> getMessageExceptionFunction() {
                return (message, cause) -> new IllegalStateException(message, cause);
            }
        };

        CTriFunction<ICRes<?>, String, Throwable, IllegalStateException> fn = provider.getExceptionFunction();
        IllegalStateException ex = fn.apply(null, "only-extend", null);
        Assertions.assertEquals("only-extend", ex.getMessage());

    }

    /**
     * 测试用 ICRes 实现
     */
    @Getter
    @RequiredArgsConstructor
    static class ICResTestsRes implements ICRes<Object> {

        private final String code;
        private final String msg;

        static ICResTestsRes of(String code, String msg) {
            return new ICResTestsRes(code, msg);
        }

    }

}
