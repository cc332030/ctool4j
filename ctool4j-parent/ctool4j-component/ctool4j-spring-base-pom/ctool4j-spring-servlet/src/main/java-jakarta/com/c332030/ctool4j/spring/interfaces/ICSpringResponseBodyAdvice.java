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
 * Description: ICSpringResponseBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICSpringResponseBodyAdvice} 是基层契约 {@link ICResponseBodyAdvice} 在 <b>jakarta</b> 侧的落地（适配接口）：
 * 同时继承 Spring 的 {@code ResponseBodyAdvice} 与基层契约，把 {@code ServletServerHttpRequest}/{@code ServletServerHttpResponse}
 * 解包成抽象层请求/响应后交给基层契约的抽象方法。</p>
 * <ul>
 *   <li>{@code beforeBodyWrite}：解包并转换后转交抽象层版本（实现类只实现抽象层版本）</li>
 *   <li>{@code supports}：沿用基层契约的默认实现（默认全部生效）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>命名分层</b>：{@code IC} 表示接口；基层契约用正名 {@link ICResponseBodyAdvice}（容器无关、最顶层），
 *   本接口是<b>底层适配</b>（性质类似 {@code sun.*} 的桥接层，只做类型转换、不含业务），故加 {@code Spring} 限定。</li>
 *   <li><b>桥接只做类型转换</b>：解包依赖 Servlet 系实现（{@link ServletServerHttpRequest}），
 *   非 Servlet 环境（WebFlux）会在强转处失败——该场景本就不适用。</li>
 *   <li><b>同名接口</b>：本接口在 {@code src/main/java-jakarta} 与 {@code src/main/java-javax} 两份中<b>同包同名</b>，
 *   由 JDK 档位选用其一，使用方无需切换依赖模块即可切换容器。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>{@code supports} 默认返回 true（与 Spring 侧默认一致），实现类可覆写以收窄生效范围。</li>
 *   <li>无其它兜底：解包失败不做处理（容器不匹配属误用）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 jakarta 与 javax 两套容器间可切换的响应体增强实现。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要直接读写响应字节流、或需要 {@code ServerHttpResponse} 专有能力的场景不适用——本层只暴露抽象层公共面。</li>
 *   <li>非 Servlet 环境（WebFlux）不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>泛型 {@code T} 跟随 Spring 的 {@code ResponseBodyAdvice<T>}，不额外改变类型参数语义。</li>
 *   <li>解包与转换集中在本接口一处，实现类不再出现 {@code ServletServerHttpRequest} 或 Servlet 类型。</li>
 * </ul>
 *
 * @param <T> 响应体类型
 * @since 2026/9/24
 * @version 1.2
 */
public interface ICSpringResponseBodyAdvice<T> extends ICResponseBodyAdvice<T>, ResponseBodyAdvice<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ICResponseBodyAdvice.super.supports(returnType, converterType);
    }

    /**
     * Servlet 桥接：解包后转交抽象层版本
     *
     * @param body                  响应体
     * @param returnType            返回类型
     * @param selectedContentType   选定的内容类型
     * @param selectedConverterType 选定的转换器类型
     * @param request               容器请求
     * @param response              容器响应
     * @return 改写后的响应体
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

        // 解包后按抽象层签名调用，实现类只实现抽象层版本，避免出现第二套入口
        return beforeBodyWrite(
            body, returnType, selectedContentType, selectedConverterType,
            httpRequest,
            httpResponse
        );

    }

}
