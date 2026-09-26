package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * <p>
 * Description: CHttpRequestMethodNotSupportedExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 请求方法不支持异常 的处理结果。</li>
 *   <li>返回 {@code CStrResult} 错误结果。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对请求方法不支持异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult} 错误结果。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：请求方法不支持异常的 {@code handle} 处理路径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路。</li>
 * </ul>
 * <h2>请求方法不支持异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证请求方法不支持异常处理结果</li>
 *   <li>1.2 异常消息为 null：用固定文案兜底，避免 {@code message: null}（handle_nullMessage）</li>
 *   <li>1.3 异常对象为 null（非预期入参）：返回固定文案、不抛 NPE（handle_nullException）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.1
 */
public class CHttpRequestMethodNotSupportedExceptionHandlerTests {

    private final CHttpRequestMethodNotSupportedExceptionHandler handler =
        new CHttpRequestMethodNotSupportedExceptionHandler();

        /**
         * 对应测试用例 1.1：验证请求方法不支持异常处理结果
         */
    @Test
    public void handle() {
        // Spring 7 移除了 (String, String) 构造，改用 (String)；消息由框架按方法名生成
        val e = new HttpRequestMethodNotSupportedException("DELETE");

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertNotNull(result.getMessage());
    }

    /**
     * 对应测试用例 1.2：异常消息为 null 时用固定文案兜底（避免返回 {@code message: null}）
     */
    @Test
    public void handle_nullMessage() {
        // 边界：消息为 null。Spring 7 的 (String) 构造必生成非空消息，无法直接构造 null 消息，
        // 故用覆写 getMessage 的子类复现「消息为 null」这一边界（等价于 jdk8 档位的 (null, null) 构造）
        val e = new HttpRequestMethodNotSupportedException("DELETE") {

            @Override
            public String getMessage() {
                return null;
            }
        };

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("请求方法不支持", result.getMessage());
    }

    /**
     * 对应测试用例 1.3：异常对象为 null（非预期入参）返回固定文案、不抛 NPE
     */
    @Test
    public void handle_nullException() {
        // 边界：异常对象为 null
        CStrResult<Void> result = handler.handle(null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("请求方法不支持", result.getMessage());
    }

}
