package com.c332030.ctool4j.web.advice;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: ICBaseResponseBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>默认方法：</p>
 * <ul>
 *   <li>{@code supports(...)}：默认返回 true</li>
 *   <li>{@code beforeBodyWrite(...)}（ServerHttpRequest 版本）：从 {@code ServletServerHttpRequest}/{@code ServletServerHttpResponse}</li>
 *   <li>解包出 {@code HttpServletRequest}/{@code HttpServletResponse}，转发给抽象 HTTP 版本</li>
 * </ul>
 * <p>抽象方法：</p>
 * <ul>
 *   <li>{@code beforeBodyWrite(body, returnType, selectedContentType, selectedConverterType, request, response)}</li>
 *   <li>（HTTP 版本）：实现类只需处理 HTTP 请求/响应</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未覆写抽象方法</td>
 *     <td>实现类必须实现 HTTP 版本抽象方法</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要介入响应体写出的 Advice 实现继承本接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code supports} 默认 true；实现类必须实现 HTTP 版本 {@code beforeBodyWrite}。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>解包转发</b></p>
 * <ul>
 *   <li>基类处理 Servlet 解包，实现类无需关心 ServerHttpRequest 转换，直接处理 HTTP 版本。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
public interface ICBaseResponseBodyAdvice<T> extends ResponseBodyAdvice<T> {

    @Override
    default boolean supports(
        MethodParameter returnType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

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

        val httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        val httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        return beforeBodyWrite(
            body, returnType, selectedContentType, selectedConverterType,
            httpServletRequest,
            httpServletResponse
        );
    }

    @Nullable
    T beforeBodyWrite(
        @Nullable T body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        HttpServletRequest request,
        HttpServletResponse response
    );

}
