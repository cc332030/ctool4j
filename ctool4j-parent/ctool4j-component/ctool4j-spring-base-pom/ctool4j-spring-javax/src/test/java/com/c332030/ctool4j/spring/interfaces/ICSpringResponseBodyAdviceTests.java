package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * <p>
 * Description: ICSpringResponseBodyAdviceTests
 * </p>
 *
 * <p>覆盖两侧适配接口 {@code ICSpringResponseBodyAdvice} 的桥接行为：{@code ServletServerHttpRequest/Response}
 * 被解包并包装为抽象层对象后转交抽象层方法，返回值原样透传，{@code supports} 沿用基层契约默认实现。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 ICSpringResponseBodyAdvice：ServerHttpRequest ↔ 抽象层 桥接 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse 构造真实 Servlet 请求/响应，再包成 Spring 的 Servlet 系实现。</li>
 *   <li>白盒视角：断言抽象层方法收到的是包装后的对象（与容器对象不同实例、但承载同一份请求信息）。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：真实 Spring MVC 容器下的响应体写出链路（由集成测试覆盖）；非 Servlet 环境（WebFlux）不在适用面内。</li>
 * </ul>
 *
 * <h2>ICSpringResponseBodyAdvice：ServerHttpRequest ↔ 抽象层桥接</h2>
 * <ul>
 *   <li>1.1 beforeBodyWrite_bridgesRequestAndResponse（请求/响应被解包并包装后透传）</li>
 *   <li>1.2 beforeBodyWrite_propagatesReturnValue（抽象层返回值原样透传）</li>
 *   <li>1.3 supports_defaultTrue（未覆写时按基层契约默认全部生效）</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public class ICSpringResponseBodyAdviceTests {

    private ServletServerHttpRequest serverHttpRequest;

    private ServletServerHttpResponse serverHttpResponse;

    private MethodParameter returnType;

    private RecordingAdvice advice;

    /**
     * 每个用例执行前的准备
     *
     * @throws Exception 反射取方法失败时
     */
    @BeforeEach
    public void setUp() throws Exception {

        val servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/demo");
        val servletResponse = new MockHttpServletResponse();

        serverHttpRequest = new ServletServerHttpRequest(servletRequest);
        serverHttpResponse = new ServletServerHttpResponse(servletResponse);

        // MethodParameter 需基于具体方法构造，parameterIndex=-1 表示方法返回值
        returnType = new MethodParameter(getClass().getDeclaredMethod("dummyReturnType"), -1);

        advice = new RecordingAdvice();

    }

    /**
     * 对应测试用例 1.1：请求/响应被解包并包装后透传
     */
    @Test
    public void beforeBodyWrite_bridgesRequestAndResponse() {

        // 正例：桥接后抽象层方法收到非空请求/响应，且不是容器对象本身
        val body = new Object();

        advice.beforeBodyWrite(
            body, returnType, MediaType.APPLICATION_JSON, null,
            serverHttpRequest, serverHttpResponse
        );

        Assertions.assertNotNull(advice.abstractRequest);
        Assertions.assertNotNull(advice.abstractResponse);
        Assertions.assertEquals("/demo", advice.abstractRequest.getRequestURI());

    }

    /**
     * 对应测试用例 1.2：抽象层返回值原样透传
     */
    @Test
    public void beforeBodyWrite_propagatesReturnValue() {

        // 正例：抽象层改写响应体后，桥接把改写结果作为返回值透传
        advice.replacedBody = "replaced";

        val result = advice.beforeBodyWrite(
            new Object(), returnType, MediaType.APPLICATION_JSON, null,
            serverHttpRequest, serverHttpResponse
        );

        Assertions.assertEquals("replaced", result);

    }

    /**
     * 对应测试用例 1.3：未覆写时默认全部生效
     */
    @Test
    public void supports_defaultTrue() {

        // 边界：只实现接口、不覆写 supports 时，沿用基层契约默认实现（全部生效）
        val defaultAdvice = new ICSpringResponseBodyAdvice<Object>() {
            @Override
            public Object beforeBodyWrite(
                Object body,
                MethodParameter returnType,
                MediaType selectedContentType,
                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                CHttpRequest request,
                CHttpResponse response
            ) {
                return body;
            }
        };

        Assertions.assertTrue(defaultAdvice.supports(returnType, null));

    }

    /**
     * 供构造 {@link MethodParameter} 用的哑方法（无实现）
     *
     * @return 哑返回值
     */
    private Object dummyReturnType() {
        return null;
    }

    /**
     * 记录入参的增强替身：只覆写抽象层版本的方法（这正是桥接要验证的用法）
     */
    private static class RecordingAdvice implements ICSpringResponseBodyAdvice<Object> {

        /**
         * 抽象层方法收到的请求
         */
        CHttpRequest abstractRequest;

        /**
         * 抽象层方法收到的响应
         */
        CHttpResponse abstractResponse;

        /**
         * 抽象层方法返回的响应体（null 表示原样返回入参 body）
         */
        Object replacedBody;

        @Override
        public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            CHttpRequest request,
            CHttpResponse response
        ) {
            this.abstractRequest = request;
            this.abstractResponse = response;
            return null == replacedBody ? body : replacedBody;
        }

    }

}
