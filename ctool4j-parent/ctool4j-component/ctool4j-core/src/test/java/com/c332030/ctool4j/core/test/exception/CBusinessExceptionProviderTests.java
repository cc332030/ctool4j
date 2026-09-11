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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「函数创建异常 / 扩展信息为 null」两个分支组织。</li>
 *   <li>用 CBusinessExceptionTestsRes（ICRes）提供 code/msg，验证函数创建的异常消息与字段。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 getExceptionFunction 返回 CBusinessException::new 的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getExceptionFunction 非空且创建异常正确；扩展信息 null 时消息为 {@code [code] msg}。</li>
 *   <li>未覆盖：无（覆盖了函数调用与 null 扩展分支）。</li>
 * </ul>
 * <h2>创建异常函数</h2>
 * <ul>
 *   <li>1.1 getExceptionFunction：创建 {@code [100] boom: detail} 异常，cause 与 msgExtend 正确（getExceptionFunction）</li>
 *   <li>1.2 null 扩展：msgExtend null 时消息为 {@code [100] boom}（getExceptionFunction_nullExtend）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CBusinessExceptionProviderTests {

    /**
     * 对应测试用例 1.1：创建 {@code [100] boom: detail} 异常，cause 与 msgExtend 正确
     */
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

    /**
     * 对应测试用例 1.2：null 扩展：msgExtend null 时消息为 {@code [100] boom}
     */
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
