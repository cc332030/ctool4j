package com.c332030.ctool4j.model;

import com.c332030.ctool4j.exception.CServletException;
import com.c332030.ctool4j.interfaces.CFilterChain;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Objects;

/**
 * <p>
 * Description: CFilterChainAdapter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFilterChainAdapter} 是 {@link CFilterChain} 在 <b>javax</b> 侧的落地（适配器）：
 * 包装 {@code javax.servlet.FilterChain}，把抽象层的放行调用解包成 javax 请求/响应后交给底层链。</p>
 * <p>创建入口：{@link #of(FilterChain)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>解包在放行时进行</b>：{@link CHttpServletRequest#unwrap} / {@link CHttpServletResponse#unwrap}
 *   取回底层对象——因此链上后续过滤器看到的仍是容器原生请求/响应，包装不外泄。</li>
 *   <li><b>异常包装</b>：底层 {@code ServletException} 是受检异常、无法出现在抽象层签名上，
 *   统一包装为 {@link CServletException}（运行时）向上抛，原因经 {@code cause} 保留。</li>
 *   <li><b>无状态</b>：除底层链外不持有状态，可安全对应"一次请求一个链"的容器语义。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>创建时校验底层链非空（{@link #of(FilterChain)} 的 {@code null} 入参快速失败）。</li>
 *   <li>解包失败（传入的请求/响应不是本侧适配器）时抛 {@link IllegalArgumentException}，不静默放过。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>javax 容器下 {@link CFilterAdapter} 放行请求，或需要在过滤器之间传递链对象的场景。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>只包装 javax 侧的链；抽象层对象与该侧适配器一一对应，不跨 javax/jakarta 混用。</li>
 *   <li>重复放行的行为由底层容器决定，本适配器不拦截。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CFilterChainAdapter implements CFilterChain {

    private final FilterChain chain;

    /**
     * 以 javax 过滤器链创建适配器
     *
     * @param chain javax 过滤器链，不可为 null
     * @return 适配器实例
     * @throws NullPointerException 过滤器链为 null 时
     */
    public static CFilterChainAdapter of(FilterChain chain) {
        Objects.requireNonNull(chain, "chain");
        return new CFilterChainAdapter(chain);
    }

    /**
     * 放行到链上的下一个过滤器：把抽象层请求/响应解包为 javax 对象后交给底层链
     *
     * @param request  请求
     * @param response 响应
     * @throws IOException 读写请求或响应失败时
     */
    @Override
    public void doFilter(CHttpRequest request, CHttpResponse response) throws IOException {

        val httpRequest = CHttpServletRequest.unwrap(request);
        val httpResponse = CHttpServletResponse.unwrap(response);

        try {
            chain.doFilter(httpRequest, httpResponse);
        } catch (ServletException e) {
            throw new CServletException(e);
        }

    }

}
