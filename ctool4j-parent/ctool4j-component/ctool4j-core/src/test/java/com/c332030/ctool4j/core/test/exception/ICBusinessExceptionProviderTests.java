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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「默认 getMessageExceptionFunction 抛异常 / 委托创建 / null error」三个维度组织。</li>
 *   <li>用匿名实现覆盖 getMessageExceptionFunction 返回指定异常类型，验证 getExceptionFunction 委托。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对默认抛 UnsupportedOperationException 与委托 formatResMessage 的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认 getMessageExceptionFunction 抛 UnsupportedOperationException；覆盖后 getExceptionFunction</li>
 *   <li>委托创建异常（含 cause）；error 为 null 时消息仅含扩展信息。</li>
 *   <li>未覆盖：无（覆盖了默认异常与委托分支）。</li>
 * </ul>
 * <h2>默认 getMessageExceptionFunction</h2>
 * <ul>
 *   <li>1.1 默认调用抛 UnsupportedOperationException（defaultGetMessageExceptionFunction_throws）</li>
 * </ul>
 * <h2>委托创建（getExceptionFunction）</h2>
 * <ul>
 *   <li>2.1 覆盖后创建：{@code [200] ok: detail}，cause 正确（getExceptionFunction_delegates）</li>
 *   <li>2.2 error 为 null：消息仅含 {@code only-extend}（getExceptionFunction_nullRes）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class ICBusinessExceptionProviderTests {

    /**
     * 对应测试用例 1.1：默认调用抛 UnsupportedOperationException
     */
    @Test
    public void defaultGetMessageExceptionFunction_throws() {

        ICBusinessExceptionProvider<RuntimeException> provider = new ICBusinessExceptionProvider<RuntimeException>() {
        };
        CBiFunction<String, Throwable, RuntimeException> fn = provider.getMessageExceptionFunction();

        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> fn.apply("msg", null));

    }

    /**
     * 对应测试用例 2.1：覆盖后创建：{@code [200] ok: detail}，cause 正确
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
     * 对应测试用例 2.2：error 为 null：消息仅含 {@code only-extend}
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
