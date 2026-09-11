package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CExceptionTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「无参 / 消息 / 原因 / 消息+原因」四种构造覆盖。</li>
 *   <li>无参断言 message/cause 为 null；消息构造断言消息；原因构造断言 cause 引用；组合构造断言两者。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @StandardException 生成构造的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：四种标准构造。</li>
 *   <li>未覆盖：无（覆盖了全部构造）。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 无参：message/cause 为 null（defaultConstructor）</li>
 *   <li>1.2 消息：message 正确（messageConstructor）</li>
 *   <li>1.3 原因：cause 引用正确（causeConstructor）</li>
 *   <li>1.4 消息+原因：两者正确（messageAndCauseConstructor）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CExceptionTests {

    /**
     * 对应测试用例 1.1：无参：message/cause 为 null
     */
    @Test
    public void defaultConstructor() {

        CException ex = new CException();
        Assertions.assertNull(ex.getMessage());
        Assertions.assertNull(ex.getCause());

    }

    /**
     * 对应测试用例 1.2：消息：message 正确
     */
    @Test
    public void messageConstructor() {

        CException ex = new CException("boom");
        Assertions.assertEquals("boom", ex.getMessage());

    }

    /**
     * 对应测试用例 1.3：原因：cause 引用正确
     */
    @Test
    public void causeConstructor() {

        Throwable cause = new IllegalStateException("cause");
        CException ex = new CException(cause);
        Assertions.assertSame(cause, ex.getCause());

    }

    /**
     * 对应测试用例 1.4：消息+原因：两者正确
     */
    @Test
    public void messageAndCauseConstructor() {

        Throwable cause = new IllegalStateException("cause");
        CException ex = new CException("boom", cause);
        Assertions.assertEquals("boom", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());

    }

}
