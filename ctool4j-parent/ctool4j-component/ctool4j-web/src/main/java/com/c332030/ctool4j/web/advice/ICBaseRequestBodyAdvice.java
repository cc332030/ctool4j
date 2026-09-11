package com.c332030.ctool4j.web.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * <p>
 * Description: ICBaseRequestBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICBaseRequestBodyAdvice} 为请求体处理的基类接口，继承 Spring {@code RequestBodyAdvice}， 提供默认空实现（不做任何处理、原样透传）。</p>
 * <p>默认方法：</p>
 * <ul>
 *   <li>{@code supports(...)}：默认返回 true（所有请求体都支持）</li>
 *   <li>{@code beforeBodyRead(...)}：默认原样返回 inputMessage</li>
 *   <li>{@code afterBodyRead(...)}：默认原样返回 body</li>
 *   <li>{@code handleEmptyBody(...)}：默认原样返回 body</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未覆写任何方法</td>
 *     <td>请求体原样透传，不介入</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要介入请求体读取/转换的 Advice 实现继承本接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code supports} 默认 true，实现类若需按条件支持需覆写。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认空实现</b></p>
 * <ul>
 *   <li>基类默认全部透传，实现类只需覆写需要介入的节点，简化使用。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
public interface ICBaseRequestBodyAdvice extends RequestBodyAdvice {

    @Override
    default boolean supports(
        MethodParameter methodParameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    default HttpInputMessage beforeBodyRead(
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) throws IOException {
        return inputMessage;
    }

    @Override
    default Object afterBodyRead(
        Object body,
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return body;
    }

    @Nullable
    @Override
    default Object handleEmptyBody(
        @Nullable Object body,
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return body;
    }
}
