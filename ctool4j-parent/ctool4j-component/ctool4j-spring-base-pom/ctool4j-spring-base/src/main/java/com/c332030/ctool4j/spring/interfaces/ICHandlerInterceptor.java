package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * Description: ICHandlerInterceptor
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICHandlerInterceptor} 为 Spring MVC 处理器拦截器的<b>抽象契约</b>：与
 * {@code org.springframework.web.servlet.HandlerInterceptor} 同形，但请求/响应参数换成抽象层的
 * {@link CHttpRequest} / {@link CHttpResponse}。</p>
 * <ul>
 *   <li>{@code preHandle}：进入处理器之前调用；返回 {@code false} 中断后续处理，默认返回 {@code true}</li>
 *   <li>{@code postHandle}：处理器执行完、视图渲染之前调用，默认空实现</li>
 *   <li>{@code afterCompletion}：整个请求完成后调用（用于清理），默认空实现</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么要这一层</b>：Spring 的 {@code HandlerInterceptor} 在方法签名里写死了 Servlet 的
 *   {@code HttpServletRequest}/{@code HttpServletResponse}（javax 与 jakarta 是两套不同的类），实现方无法回避；
 *   本接口把三个方法改成抽象层类型，由两侧的 {@code ICSpringHandlerInterceptor} 继承 Spring 接口并把参数转换过来，
 *   使用方实现 {@code ICSpringHandlerInterceptor} 时只写抽象层类型、不接触 Servlet 包。</li>
 *   <li><b>保留 {@link ModelAndView}</b>：{@code postHandle} 的视图模型参数属 Spring 自有类型、不含 Servlet 包，
 *   直接沿用、不额外抽象。</li>
 *   <li><b>异常契约沿用 Spring</b>：三个方法均为 {@code throws Exception}——拦截器允许抛任意异常，
 *   由 DispatcherServlet 交给异常解析链处理，本层不改写该契约。</li>
 *   <li><b>默认实现与 Spring 对齐</b>：{@code preHandle} 默认放行、另两个默认空实现，实现类按需覆写。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：本接口只声明契约，不捕获实现抛出的异常（与 Spring 语义一致，交由容器的异常解析链处理）。</li>
 *   <li>默认方法即"什么都不做"的兜底，避免实现类被迫写空方法。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 javax 与 jakarta 两套容器间可切换的拦截器实现；使用方实现两侧模块提供的
 *   {@code ICSpringHandlerInterceptor}（同名类），代码零改动。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要 Spring 专有参数（如 {@code HttpServletRequest} 的会话、二进制流）的场景不适用——
 *   本层只暴露抽象层公共面，需要时在两侧实现里直接取底层对象。</li>
 *   <li>非 Spring MVC 环境不适用（本接口不参与任何注册，注册由 MVC 配置承担）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本接口<b>不继承</b> Spring 的 {@code HandlerInterceptor}：继承会让接口同时背上两套同名的 servlet 参数方法，
 *   下游实现时必须二选一、且不同容器下接口本身也不同——正是要避免的形态。</li>
 *   <li>只承载 Spring MVC 拦截器的三个回调：不额外增加 ordering、async 等容器语义（由注册侧决定）。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface ICHandlerInterceptor {

    /**
     * 进入处理器之前调用；返回 false 则中断后续处理
     *
     * @param request  请求
     * @param response 响应
     * @param handler  被调用的处理器
     * @return true 表示继续处理链
     * @throws Exception 拦截器自身失败时
     */
    default boolean preHandle(CHttpRequest request, CHttpResponse response, Object handler) throws Exception {
        return true;
    }

    /**
     * 处理器执行完、视图渲染之前调用
     *
     * @param request      请求
     * @param response     响应
     * @param handler      被调用的处理器
     * @param modelAndView 处理器返回的视图模型；可能为 null
     * @throws Exception 拦截器自身失败时
     */
    default void postHandle(
        CHttpRequest request,
        CHttpResponse response,
        Object handler,
        @Nullable ModelAndView modelAndView
    ) throws Exception {
    }

    /**
     * 整个请求完成后调用，用于清理资源
     *
     * @param request  请求
     * @param response 响应
     * @param handler  被调用的处理器
     * @param ex       处理链抛出的异常；无异常时为 null
     * @throws Exception 拦截器自身失败时
     */
    default void afterCompletion(
        CHttpRequest request,
        CHttpResponse response,
        Object handler,
        @Nullable Exception ex
    ) throws Exception {
    }

}
