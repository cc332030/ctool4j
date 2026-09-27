package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * Description: ICSpringHandlerInterceptorTests
 * </p>
 *
 * <p>覆盖两侧适配接口 {@code ICSpringHandlerInterceptor} 的桥接行为：Servlet 请求/响应被包装为抽象层对象后转交
 * 抽象层方法，且返回值与调用次序原样透传。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 ICSpringHandlerInterceptor：Servlet ↔ 抽象层 桥接 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse 构造真实请求场景；抽象层方法由测试替身覆写并记录入参。</li>
 *   <li>白盒视角：断言"包装后的对象不是容器对象、但承载同一份请求信息"，以及返回值与各回调的透传。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：真实 Spring MVC 容器下的拦截器链装配与执行顺序（由集成测试覆盖）。</li>
 * </ul>
 *
 * <h2>ICSpringHandlerInterceptor：Servlet ↔ 抽象层桥接</h2>
 * <ul>
 *   <li>1.1 preHandle_bridgesRequestAndResponse（请求/响应被包装为抽象层对象并透传）</li>
 *   <li>1.2 preHandle_propagatesReturnValue（抽象层返回值原样透传）</li>
 *   <li>1.3 postHandle_passesModelAndView（视图对象原样透传）</li>
 *   <li>1.4 afterCompletion_passesException（异常对象原样透传）</li>
 *   <li>1.5 preHandle_defaultImplementationAllowsThrough（未覆写时按基层契约默认放行）</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public class ICSpringHandlerInterceptorTests {

    private MockHttpServletRequest servletRequest;

    private MockHttpServletResponse servletResponse;

    private RecordingInterceptor interceptor;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        interceptor = new RecordingInterceptor();
    }

    /**
     * 对应测试用例 1.1：请求/响应被包装为抽象层对象并透传
     */
    @Test
    public void preHandle_bridgesRequestAndResponse() throws Exception {

        // 正例：桥接后抽象层方法收到非空请求/响应，且不是容器对象本身
        servletRequest.setRequestURI("/demo");

        val result = interceptor.preHandle(servletRequest, servletResponse, new Object());

        Assertions.assertTrue(result);
        Assertions.assertNotNull(interceptor.abstractRequest);
        Assertions.assertNotNull(interceptor.abstractResponse);
        Assertions.assertNotSame(servletRequest, interceptor.abstractRequest);
        Assertions.assertEquals("/demo", interceptor.abstractRequest.getRequestURI());

    }

    /**
     * 对应测试用例 1.2：抽象层返回值原样透传
     */
    @Test
    public void preHandle_propagatesReturnValue() throws Exception {

        // 反例：抽象层返回 false（中断请求）时，桥接不得吞掉该返回值
        interceptor.preHandleResult = false;

        val result = interceptor.preHandle(servletRequest, servletResponse, new Object());

        Assertions.assertFalse(result);

    }

    /**
     * 对应测试用例 1.3：视图对象原样透传
     */
    @Test
    public void postHandle_passesModelAndView() throws Exception {

        // 正例：postHandle 的 ModelAndView 原样透传（桥接不做加工）
        val modelAndView = new ModelAndView("demo");

        interceptor.postHandle(servletRequest, servletResponse, new Object(), modelAndView);

        Assertions.assertSame(modelAndView, interceptor.modelAndView);
        Assertions.assertEquals(1, interceptor.postHandleCount);

    }

    /**
     * 对应测试用例 1.4：异常对象原样透传
     */
    @Test
    public void afterCompletion_passesException() throws Exception {

        // 正例：afterCompletion 的异常原样透传（为 null 时也应到达抽象层方法）
        val ex = new IllegalStateException("boom");

        interceptor.afterCompletion(servletRequest, servletResponse, new Object(), ex);

        Assertions.assertSame(ex, interceptor.exception);
        Assertions.assertEquals(1, interceptor.afterCompletionCount);

    }

    /**
     * 对应测试用例 1.5：未覆写时按基层契约默认放行
     */
    @Test
    public void preHandle_defaultImplementationAllowsThrough() throws Exception {

        // 边界：只实现接口、不覆写任何方法时，走基层契约默认实现（放行 + 空回调）
        val defaultInterceptor = new ICSpringHandlerInterceptor() { };

        val result = defaultInterceptor.preHandle(servletRequest, servletResponse, new Object());

        Assertions.assertTrue(result);

        // 空回调不抛异常即为通过（默认实现为空实现）
        Assertions.assertDoesNotThrow(() ->
            defaultInterceptor.afterCompletion(servletRequest, servletResponse, new Object(), null));

    }

    /**
     * 记录入参的拦截器替身：只覆写抽象层版本的方法（这正是桥接要验证的用法）
     */
    private static class RecordingInterceptor implements ICSpringHandlerInterceptor {

        /**
         * 抽象层 preHandle 返回值（默认放行）
         */
        boolean preHandleResult = true;

        /**
         * 抽象层方法收到的请求
         */
        CHttpRequest abstractRequest;

        /**
         * 抽象层方法收到的响应
         */
        CHttpResponse abstractResponse;

        /**
         * 抽象层 postHandle 收到的视图
         */
        ModelAndView modelAndView;

        /**
         * 抽象层 afterCompletion 收到的异常
         */
        Exception exception;

        /**
         * postHandle 调用次数
         */
        int postHandleCount;

        /**
         * afterCompletion 调用次数
         */
        int afterCompletionCount;

        @Override
        public boolean preHandle(CHttpRequest request, CHttpResponse response, Object handler) {
            this.abstractRequest = request;
            this.abstractResponse = response;
            return preHandleResult;
        }

        @Override
        public void postHandle(CHttpRequest request, CHttpResponse response, Object handler, ModelAndView modelAndView) {
            this.postHandleCount++;
            this.modelAndView = modelAndView;
        }

        @Override
        public void afterCompletion(CHttpRequest request, CHttpResponse response, Object handler, Exception ex) {
            this.afterCompletionCount++;
            this.exception = ex;
        }

    }

}
