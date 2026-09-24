package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;

import lombok.val;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <p>
 * Description: ICBaseResponseBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICBaseResponseBodyAdvice} 是 {@link CResponseBodyAdvice} 在 <b>jakarta</b> 侧的落地（适配器）：
 * 同时继承 Spring 的 {@code ResponseBodyAdvice} 与抽象契约 {@link CResponseBodyAdvice}，
 * 从 {@code ServletServerHttpRequest}/{@code ServletServerHttpResponse} 解包出 Servlet 请求/响应并包装成抽象层类型。</p>
 * <ul>
 *   <li>{@code supports}：桥接到抽象契约的默认实现（默认全部生效）</li>
 *   <li>{@code beforeBodyWrite}（ServerHttpRequest 版本）：解包 + 包装后转交抽象契约的 {@code beforeBodyWrite}</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>多继承接入两侧</b>：{@code extends CResponseBodyAdvice<T>, ResponseBodyAdvice<T>}——Spring 侧负责被
 *   {@code @ControllerAdvice} 发现与调用，抽象契约侧负责给使用方提供与容器无关的签名。</li>
 *   <li><b>解包只在此处</b>：{@code ServletServerHttpRequest#getServletRequest} 与
 *   {@code ServletServerHttpResponse#getServletResponse} 是本类唯一接触 Servlet 类型的地方，
 *   随后立即经 {@link CHttpServletRequest#of} / {@link CHttpServletResponse#of} 包装。</li>
 *   <li><b>同名类</b>：本类在 {@code ctool4j-spring-jakarta} 与 {@code ctool4j-spring-javax} 中<b>同包同名</b>，
 *   切换依赖模块即切换容器，业务代码与 import 均不变。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>{@code supports} 默认返回 {@code true}（与 Spring 侧默认一致），使用方可覆写以收窄生效范围。</li>
 *   <li>解包按类型强转进行：Spring MVC 在 Servlet 应用下传入的恒为 Servlet 系实现，故强转不会失败；
 *   非 Servlet 环境（WebFlux）不适用本接口。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>运行在 jakarta（Spring Boot 3.x / Servlet 5.0+）容器下、需要改写响应体的 {@code @ControllerAdvice} 实现。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要 {@code ServerHttpResponse} 专有能力（直接写字节流、获取 headers 容器等）时不适用——
 *   这些能力不在抽象层公共面内。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>使用方覆写时须用抽象层参数类型（{@code CHttpRequest}/{@code CHttpResponse}）；
 *   覆写 Spring 版本的同名方法会绕过抽象层。</li>
 *   <li>解包依赖 Servlet 系实现：容器不是 Servlet 栈时（WebFlux）会在强转处抛 {@code ClassCastException}，
 *   本接口不做兜底（该场景本就不适用）。</li>
 * </ul>
 *
 * @param <T> 响应体类型
 * @since 2026/9/24
 * @version 1.0
 */
public interface ICBaseResponseBodyAdvice<T> extends CResponseBodyAdvice<T>, ResponseBodyAdvice<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return CResponseBodyAdvice.super.supports(returnType, converterType);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    default T beforeBodyWrite(
        @Nullable T body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request,
        ServerHttpResponse response
    ) {

        val servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        val servletResponse = ((ServletServerHttpResponse) response).getServletResponse();

        val httpRequest = CHttpServletRequest.of(servletRequest);
        val httpResponse = CHttpServletResponse.of(servletResponse);

        // 解包后按抽象层签名调用本接口的抽象方法（由实现类实现），避免出现第二套入口
        return beforeBodyWrite(
            body, returnType, selectedContentType, selectedConverterType,
            httpRequest,
            httpResponse
        );

    }

}
