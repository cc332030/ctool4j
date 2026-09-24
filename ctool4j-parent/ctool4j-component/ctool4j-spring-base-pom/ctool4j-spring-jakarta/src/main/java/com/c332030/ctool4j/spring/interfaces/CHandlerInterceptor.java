package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;

import lombok.val;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CHandlerInterceptor
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHandlerInterceptor} 是 {@link ICHandlerInterceptor} 在 <b>jakarta</b> 侧的落地（适配器）：
 * 同时继承 Spring 的 {@code HandlerInterceptor} 与抽象契约 {@link ICHandlerInterceptor}，
 * 把 Spring 传来的 Servlet 请求/响应转换成抽象层类型后交给抽象契约的默认实现。</p>
 * <ul>
 *   <li>使用方（业务拦截器）实现本接口，只覆写抽象层版本的方法，接口本身提供 Servlet 侧的桥接</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>多继承接入两侧</b>：{@code extends ICHandlerInterceptor, HandlerInterceptor}——Spring 侧负责被容器发现与调用，
 *   抽象契约侧负责给使用方提供与容器无关的方法签名。</li>
 *   <li><b>桥接只做类型转换</b>：Servlet 请求/响应经 {@link CHttpServletRequest#of} / {@link CHttpServletResponse#of}
 *   包装后转交抽象契约，不做任何业务加工。</li>
 *   <li><b>同名类</b>：本类在 {@code ctool4j-spring-jakarta} 与 {@code ctool4j-spring-javax} 中<b>同包同名</b>，
 *   使用方切换依赖模块即可切换容器，业务代码与 import 均不变。</li>
 *   <li><b>仍是统一标记</b>：容器侧按本接口类型收集并注册拦截器（WebMvcConfigurer 的实际收集逻辑由编排模块承担）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：使用方未覆写任一方法时，走 {@link ICHandlerInterceptor} 的默认实现（放行 + 空实现），
 *   与 Spring {@code HandlerInterceptor} 的默认行为一致。</li>
 *   <li>Spring 侧传入的请求/响应恒为 HTTP 版本（容器保证），故 {@link CHttpServletRequest#of} 的强转不会失败。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>运行在 jakarta（Spring Boot 3.x / Servlet 5.0+）容器下的拦截器实现。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要读取会话、Cookie、二进制流等抽象层未暴露能力时不适用——这类需求要直接写容器侧的
 *   {@code HandlerInterceptor}，不经本接口。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>使用方若同时覆写 Spring 版本与抽象版本的方法（如误写 {@code preHandle(HttpServletRequest, ...)}），
 *   会覆盖桥接方法、绕过抽象层——按接口文档只覆写抽象层版本。</li>
 *   <li>本接口不参与注册（不含 {@code @Component}）：是否注册、注册顺序仍由编排模块与容器决定。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface CHandlerInterceptor extends ICHandlerInterceptor, HandlerInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {

        val httpRequest = CHttpServletRequest.of(request);
        val httpResponse = CHttpServletResponse.of(response);

        return ICHandlerInterceptor.super.preHandle(httpRequest, httpResponse, handler);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable ModelAndView modelAndView
    ) throws Exception {

        val httpRequest = CHttpServletRequest.of(request);
        val httpResponse = CHttpServletResponse.of(response);

        ICHandlerInterceptor.super.postHandle(httpRequest, httpResponse, handler, modelAndView);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception ex
    ) throws Exception {

        val httpRequest = CHttpServletRequest.of(request);
        val httpResponse = CHttpServletResponse.of(response);

        ICHandlerInterceptor.super.afterCompletion(httpRequest, httpResponse, handler, ex);

    }

}
