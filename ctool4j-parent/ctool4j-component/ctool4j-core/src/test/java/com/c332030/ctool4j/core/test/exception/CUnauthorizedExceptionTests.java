package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CUnauthorizedExceptionTests
 * </p>
 *
 * <p>覆盖 {@code CUnauthorizedException} 的标准构造集合（Lombok {@code @StandardException} 生成）：
 * 无参 / 消息 / 消息 + 原因，保证异常可作为「未认证」语义载体被上层处理器识别。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>异常本身无逻辑分支，用例按构造重载做等价类覆盖，断言 message/cause 传递正确。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参、消息、消息+原因三种构造。</li>
 *   <li>未覆盖：HTTP 401 响应（由 ctool4j-web 的 {@code CUnauthorizedExceptionHandlerTests} 覆盖）。</li>
 * </ul>
 * <h2>构造输出</h2>
 * <ul>
 *   <li>1.1 消息构造：message 传递、cause 为 null（construct_withMessage）</li>
 *   <li>1.2 消息+原因构造：message 与 cause 均传递（construct_withMessageAndCause）</li>
 *   <li>1.3 无参构造：message 为 null（construct_noArgs）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.0
 */
class CUnauthorizedExceptionTests {

    /**
     * <p>对应测试用例 1.1：消息构造</p>
     */
    @Test
    void construct_withMessage() {
        CUnauthorizedException e = new CUnauthorizedException("未授权");

        Assertions.assertEquals("未授权", e.getMessage());
        Assertions.assertNull(e.getCause());
    }

    /**
     * <p>对应测试用例 1.2：消息 + 原因构造</p>
     */
    @Test
    void construct_withMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("boom");
        CUnauthorizedException e = new CUnauthorizedException("未授权", cause);

        Assertions.assertEquals("未授权", e.getMessage());
        Assertions.assertSame(cause, e.getCause());
    }

    /**
     * <p>对应测试用例 1.3：无参构造</p>
     */
    @Test
    void construct_noArgs() {
        CUnauthorizedException e = new CUnauthorizedException();

        Assertions.assertNull(e.getMessage());
        Assertions.assertNull(e.getCause());
    }

}
