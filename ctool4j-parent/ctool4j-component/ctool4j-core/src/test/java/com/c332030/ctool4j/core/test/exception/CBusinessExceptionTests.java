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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「仅错误码 / 错误码+附加 / 错误码+原因 / 全参数 / null 错误码」多个构造覆盖。</li>
 *   <li>用 {@code TestRes}（实现 ICRes）提供 code/msg，验证消息格式化 {@code [code] msg[: detail]}。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对消息格式化（formatResMessage）与字段设置的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：errorOnly、error+msgExtend、error+cause、error+msgExtend+cause、null error。</li>
 *   <li>未覆盖：无（覆盖了全部构造）。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 仅错误码：{@code [100] boom}，error 正确、msgExtend null（errorOnly）</li>
 *   <li>1.2 错误码+附加：{@code [100] boom: detail}，msgExtend 正确（errorAndMsgExtend）</li>
 *   <li>1.3 错误码+原因：{@code [100] boom}，cause 正确（errorAndCause）</li>
 *   <li>1.4 全参数：{@code [100] boom: detail}，cause 与 msgExtend 正确（errorMsgExtendCause）</li>
 *   <li>1.5 null 错误码：消息仅含 {@code only-extend}，error null（nullError）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CBusinessExceptionTests {

    /**
     * 对应测试用例 1.1：仅错误码：{@code [100] boom}，error 正确、msgExtend null
     */
    @Test
    public void errorOnly() {

        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"));
        Assertions.assertEquals("[100] boom", ex.getMessage());
        Assertions.assertEquals("100", ex.getError().getCode());
        Assertions.assertNull(ex.getMsgExtend());

    }

    /**
     * 对应测试用例 1.2：错误码+附加：{@code [100] boom: detail}，msgExtend 正确
     */
    @Test
    public void errorAndMsgExtend() {

        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"), "detail");
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());
        Assertions.assertEquals("detail", ex.getMsgExtend());

    }

    /**
     * 对应测试用例 1.3：错误码+原因：{@code [100] boom}，cause 正确
     */
    @Test
    public void errorAndCause() {

        Throwable cause = new IllegalStateException("cause");
        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"), cause);
        Assertions.assertEquals("[100] boom", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());

    }

    /**
     * 对应测试用例 1.4：全参数：{@code [100] boom: detail}，cause 与 msgExtend 正确
     */
    @Test
    public void errorMsgExtendCause() {

        Throwable cause = new IllegalStateException("cause");
        CBusinessException ex = new CBusinessException(TestRes.of("100", "boom"), "detail", cause);
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());
        Assertions.assertEquals("detail", ex.getMsgExtend());

    }

    /**
     * 对应测试用例 1.5：null 错误码：消息仅含 {@code only-extend}，error null
     */
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
