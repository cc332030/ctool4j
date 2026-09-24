package com.c332030.ctool4j.web.cors.advice;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.spring.interfaces.CResponseBodyAdvice;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

/**
 * <p>
 * Description: CCorsResponseBodyAdvice
 * </p>
 *
 * <p>
 * CORS 备用方案：Filter 已自动生效时无需注册本 Advice；
 * 需要时由使用方手动注册
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsResponseBodyAdvice} 为跨域的<b>备用方案</b>（{@code @ControllerAdvice} 已被注释，需使用方手动注册），
 * 实现 {@code CResponseBodyAdvice&lt;Object&gt;}（{@code ctool4j-spring-javax} 提供的抽象层版），
 * 在响应体写入前调用 {@code CCorsUtils.handle(request, response)} 输出 CORS 头，然后原样返回响应体。</p>
 * <p>核心方法 {@code beforeBodyWrite(...)}：</p>
 * <ul>
 *   <li>入参已是抽象层 {@code CHttpRequest}/{@code CHttpResponse}（由适配器从
 *   {@code ServletServerHttpRequest}/{@code ServletServerHttpResponse} 解包并转换）</li>
 *   <li>调用 {@code CCorsUtils.handle} 设置 CORS 响应头，返回原响应体 {@code body}</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <p><b>用抽象层类型，无需解包</b></p>
 * <ul>
 *   <li>解包与转换集中在两侧适配器（{@code CResponseBodyAdvice}），本类只处理抽象层请求/响应，
 *   不出现任何 Servlet 类型。</li>
 * </ul>
 * <p><b>复用处理逻辑</b></p>
 * <ul>
 *   <li>复用 {@code CCorsUtils.handle} 统一的 CORS 头输出逻辑，不重复实现。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>当 {@code CCorsFilter} 已自动生效时，无需注册本 Advice；本 Advice 供无法使用 Filter 的场景手动注册。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code CCorsUtils.handle} 内部异常</td>
 *     <td>被 CCorsUtils 自身 try-catch 记录日志，不影响响应写出</td>
 *   </tr>
 *   <tr>
 *     <td>body 为 null</td>
 *     <td>原样返回 null</td>
 *   </tr>
 * </table>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>Filter 已生效时无需使用；未注册则本 Advice 不生效。</li>
 *   <li>非 Servlet 容器（WebFlux）不适用：适配器的解包依赖 {@code ServletServerHttpRequest}。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>需手动注册才生效；跨域处理统一收敛在 {@code CCorsUtils}。</li>
 * </ul>
 *
 * @since 2025/11/12
 * @version 1.1
 */
@CustomLog
//@ControllerAdvice
@AllArgsConstructor
public class CCorsResponseBodyAdvice implements CResponseBodyAdvice<Object> {

    /**
     * 响应写入前处理 CORS 头
     *
     * @param body                   响应体
     * @param returnType             返回类型
     * @param selectedContentType    选中的内容类型
     * @param selectedConverterType  选中的转换器类型
     * @param request                请求
     * @param response               响应
     * @return 原响应体
     */
    @Nullable
    @Override
    public Object beforeBodyWrite(
        @Nullable Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        CHttpRequest request,
        CHttpResponse response
    ) {
        CCorsUtils.handle(request, response);
        return body;
    }

}
