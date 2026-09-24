package com.c332030.ctool4j.model;

import com.c332030.ctool4j.exception.CServletException;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.interfaces.CRequestDispatcher;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * <p>
 * Description: CRequestDispatcherAdapter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestDispatcherAdapter} 是 {@link CRequestDispatcher} 在 <b>javax</b> 侧的落地（适配器）：
 * 包装 {@code javax.servlet.RequestDispatcher}，把抽象层的转发调用解包成 javax 请求/响应后交给底层转发器。</p>
 * <p>创建入口：{@link #of(RequestDispatcher)}；取用入口是
 * {@link CHttpServletRequest#getRequestDispatcher(String)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>两个动作共用一个调用骨架</b>：{@code forward} 与 {@code include} 只差底层动作，
 *   解包与异常包装收敛到私有方法 {@code invoke}，避免同一段逻辑写两遍。</li>
 *   <li><b>异常包装</b>：底层 {@code ServletException} 统一包装为 {@link CServletException}（运行时），
 *   原因经 {@code cause} 保留。</li>
 *   <li><b>无状态</b>：除底层转发器外不持有状态。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>创建时校验底层转发器非空（{@link #of(RequestDispatcher)} 的 {@code null} 入参快速失败）；
 *   容器侧取不到转发器时返回 {@code null}，由 {@link CHttpServletRequest#getRequestDispatcher(String)} 原样透出，
 *   调用方须判空。</li>
 *   <li>解包失败（传入的请求/响应不是本侧适配器）时抛 {@link IllegalArgumentException}，不静默放过。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>javax 容器下需要在过滤器/拦截器中把请求转发或并入其它资源的场景。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>转发前响应已提交时抛 {@code IllegalStateException}（容器语义），本适配器不拦截、不包装。</li>
 *   <li>只包装 javax 侧的转发器；抽象层对象与该侧适配器一一对应，不跨 javax/jakarta 混用。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CRequestDispatcherAdapter implements CRequestDispatcher {

    private final RequestDispatcher dispatcher;

    /**
     * 以 javax 请求转发器创建适配器
     *
     * @param dispatcher javax 请求转发器，不可为 null
     * @return 适配器实例
     * @throws NullPointerException 请求转发器为 null 时
     */
    public static CRequestDispatcherAdapter of(RequestDispatcher dispatcher) {
        Objects.requireNonNull(dispatcher, "dispatcher");
        return new CRequestDispatcherAdapter(dispatcher);
    }

    /**
     * 把请求转发给目标资源
     *
     * @param request  请求
     * @param response 响应
     * @throws IOException 读写请求或响应失败时
     */
    @Override
    public void forward(CHttpRequest request, CHttpResponse response) throws IOException {
        invoke(request, response, dispatcher::forward);
    }

    /**
     * 把目标资源的输出并入当前响应
     *
     * @param request  请求
     * @param response 响应
     * @throws IOException 读写请求或响应失败时
     */
    @Override
    public void include(CHttpRequest request, CHttpResponse response) throws IOException {
        invoke(request, response, dispatcher::include);
    }

    /**
     * 底层转发动作：与 {@code RequestDispatcher} 的 {@code forward} / {@code include} 同形，
     * 只为把受检异常原样带出——{@code java.util.function.BiConsumer} 不声明受检异常，承载不了
     */
    @FunctionalInterface
    private interface CDispatcherAction {

        /**
         * 执行底层转发动作
         *
         * @param request  Servlet 请求
         * @param response Servlet 响应
         * @throws ServletException 容器转发失败时
         * @throws IOException      读写请求或响应失败时
         */
        void accept(ServletRequest request, ServletResponse response) throws ServletException, IOException;

    }

    /**
     * 解包请求/响应并执行底层转发动作；底层 {@code ServletException} 包装为 {@link CServletException}
     *
     * @param request  请求
     * @param response 响应
     * @param action   底层转发动作（{@code RequestDispatcher#forward} / {@code #include}）
     * @throws IOException 读写请求或响应失败时
     */
    private void invoke(
        CHttpRequest request,
        CHttpResponse response,
        CDispatcherAction action
    ) throws IOException {

        val httpRequest = CHttpServletRequest.unwrap(request);
        val httpResponse = CHttpServletResponse.unwrap(response);

        try {
            action.accept(httpRequest, httpResponse);
        } catch (ServletException e) {
            throw new CServletException(e);
        }

    }

}
