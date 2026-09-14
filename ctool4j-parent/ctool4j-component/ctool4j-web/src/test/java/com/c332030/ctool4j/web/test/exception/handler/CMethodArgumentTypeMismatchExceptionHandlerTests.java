package com.c332030.ctool4j.web.test.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.exception.handler.CMethodArgumentTypeMismatchExceptionHandler;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * <p>
 * Description: CMethodArgumentTypeMismatchExceptionHandlerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器并调用 {@code handle}，验证 方法参数类型不匹配异常 的处理结果。</li>
 *   <li>断言分两层：统一错误结果（错误码/文案），以及文案<b>保留参数名且不含</b>异常自身消息。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对方法参数类型不匹配异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据 Spring 该异常的消息形态：{@code Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'}，
 *   含全限定 Java 类型名，属不应透出的内部细节。</li>
 *   <li>依据测试方法（正例/边界）：返回 {@code CStrResult} 错误结果且文案保留参数名。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：方法参数类型不匹配异常的 {@code handle} 处理路径与文案口径。</li>
 *   <li>未覆盖：真实 MVC 容器下的异常拦截链路；请求体内部字段类型错误（由
 *   {@code CHttpMessageNotReadableExceptionHandler} 覆盖）。</li>
 * </ul>
 * <h2>方法参数类型不匹配异常处理</h2>
 * <ul>
 *   <li>1.1 方法参数类型不匹配异常的处理结果（handle）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
public class CMethodArgumentTypeMismatchExceptionHandlerTests {

    private final CMethodArgumentTypeMismatchExceptionHandler handler =
        new CMethodArgumentTypeMismatchExceptionHandler();

    /**
     * 对应测试用例 1.1：验证方法参数类型不匹配异常的处理结果
     */
    @Test
    public void handle() {
        // 构造器末两位为 MethodParameter / cause，本用例只验证文案口径，故传 null
        val e = new MethodArgumentTypeMismatchException("abc", Integer.class, "id", null, null);

        CStrResult<Void> result = handler.handle(e);

        Assertions.assertEquals("500", result.getCode());
        // 保留参数名：调用方据此修正请求
        Assertions.assertEquals("参数类型不正确：id", result.getMessage());
        // 不透出异常自身消息（含全限定 Java 类型名与嵌套异常信息）
        Assertions.assertFalse(result.getMessage().contains("Failed to convert"));
    }

}
