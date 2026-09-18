package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

/**
 * <p>
 * Description: CHttpMessageNotReadableExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 请求体不可读异常 的处理结果。</li>
 *   <li>按线上实际场景构造用例（请求体缺失，异常消息里带控制器方法签名）。</li>
 *   <li>额外断言错误文案<b>不含</b>异常自身消息：该消息含服务端内部结构，属不应透出的信息。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对请求体不可读异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据线上报错：{@code HttpMessageNotReadableException: Required request body is missing: ...sidebarLogin(...)}。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult} 错误结果，且不泄露内部消息。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：请求体不可读异常的 {@code handle} 处理路径与文案口径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路；JSON 格式错误等其它触发原因（与缺失同路，同一处理器覆盖）。</li>
 * </ul>
 * <h2>请求体不可读异常处理</h2>
 * <ul>
 *   <li>1.1 请求体不可读异常的处理结果（handle）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
public class CHttpMessageNotReadableExceptionHandlerTests {

    private final CHttpMessageNotReadableExceptionHandler handler = new CHttpMessageNotReadableExceptionHandler();

    /**
     * 对应测试用例 1.1：验证请求体不可读异常的处理结果
     */
    @Test
    public void handle() {
        // 复现线上场景：请求体缺失（注意异常自身消息含控制器方法签名，属内部信息）
        val e = new HttpMessageNotReadableException(
            "Required request body is missing: public void FooController.bar(FooReq)",
            new MockHttpInputMessage(new byte[0])
        );

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("请求体缺失或格式不正确", result.getMessage());
        Assertions.assertFalse(result.getMessage().contains("Required request body is missing"));
    }

}
