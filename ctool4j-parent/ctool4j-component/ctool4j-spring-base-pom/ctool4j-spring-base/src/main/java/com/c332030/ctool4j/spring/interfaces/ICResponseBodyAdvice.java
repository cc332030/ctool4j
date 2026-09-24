package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

/**
 * <p>
 * Description: ICResponseBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICResponseBodyAdvice} 为 Spring MVC 响应体增强（{@code ResponseBodyAdvice}）的<b>抽象契约</b>：
 * 与 {@code org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice} 同形，
 * 但把 {@code ServerHttpRequest}/{@code ServerHttpResponse} 换成抽象层的
 * {@link CHttpRequest} / {@link CHttpResponse}。</p>
 * <ul>
 *   <li>{@code supports}：本增强是否作用于该返回值类型，默认全部支持</li>
 *   <li>{@code beforeBodyWrite}：写出响应体之前改写响应体（如统一包装、脱敏）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么要这一层</b>：Spring 的 {@code beforeBodyWrite} 收到的是
 *   {@code ServerHttpRequest}/{@code ServerHttpResponse}，Servlet 应用下其实现是
 *   {@code ServletServerHttpRequest}/{@code ServletServerHttpResponse}（两侧包名不同）；若要直接拿 Servlet 请求/响应，
 *   实现方就得写死某一侧的包。本接口把它换成抽象层类型，由两侧的 {@code CResponseBodyAdvice} 负责从
 *   {@code ServletServerHttpRequest} 解包并转换。</li>
 *   <li><b>解包集中在一处</b>：解包与转换只在两侧适配器里做一次，实现类不再出现
 *   {@code ServletServerHttpRequest} 或 {@code HttpServletRequest}。</li>
 *   <li><b>保留 Spring 的其余参数</b>：{@link MethodParameter}、{@link MediaType}、
 *   {@link HttpMessageConverter} 都是 Spring 自有类型、与 Servlet 包无关，直接沿用。</li>
 *   <li><b>与 {@code ResponseBodyAdvice} 的关系</b>：两侧适配器用多继承同时接入 Spring 的接口与本接口，
 *   故下游注解（{@code @ControllerAdvice}）与注册方式不变。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>{@code supports} 默认返回 {@code true}（与 Spring 侧默认一致），实现类可覆写以收窄生效范围。</li>
 *   <li>无其它兜底：本接口不做解包失败处理，解包由适配器负责（非 Servlet 环境下的行为见适配器文档）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 javax 与 jakarta 两套容器间可切换的响应体增强实现；使用方实现两侧模块提供的
 *   {@code CResponseBodyAdvice}（同名类），代码零改动。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要直接读写响应字节流、或需要 {@code ServerHttpResponse} 专有能力的场景不适用——
 *   本层只暴露抽象层公共面。</li>
 *   <li>非 Servlet 环境（WebFlux）不适用：那套没有 {@code ServletServerHttpRequest}，两侧适配器解包不成立。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>泛型 {@code T} 跟随 Spring 的 {@code ResponseBodyAdvice<T>}，不额外改变类型参数语义。</li>
 *   <li>本接口不继承 Spring 的 {@code ResponseBodyAdvice}，理由与 {@code ICHandlerInterceptor} 相同：
 *   继承会把 Spring 的方法签名（含 {@code ServerHttpRequest}）带进来，与抽象层方法形成两套入口。</li>
 * </ul>
 *
 * @param <T> 响应体类型
 * @since 2026/9/24
 * @version 1.0
 */
public interface ICResponseBodyAdvice<T> {

    /**
     * 判断本增强是否作用于该返回值类型
     *
     * @param returnType    返回值方法参数
     * @param converterType 选定的消息转换器类型
     * @return true 表示生效（默认全部生效）
     */
    default boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 写出响应体之前改写响应体
     *
     * @param body                  原响应体；可能为 null
     * @param returnType            返回值方法参数
     * @param selectedContentType   选定的内容类型
     * @param selectedConverterType 选定的消息转换器类型
     * @param request               请求
     * @param response              响应
     * @return 改写后的响应体（可为原值或新对象）
     */
    @Nullable
    T beforeBodyWrite(
        @Nullable T body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        CHttpRequest request,
        CHttpResponse response
    );

}
