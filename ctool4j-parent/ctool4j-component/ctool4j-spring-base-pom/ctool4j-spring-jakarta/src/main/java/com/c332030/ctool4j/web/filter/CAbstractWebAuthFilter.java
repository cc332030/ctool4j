package com.c332030.ctool4j.web.filter;

import com.c332030.ctool4j.interfaces.CFilterChain;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.model.CFilterChainAdapter;
import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: CAbstractWebAuthFilter
 * </p>
 *
 * <p>认证过滤器继承链的最顶层基类：承载"认证过滤器"的类型契约与<b>过滤器骨架</b>。
 * 位于两侧模块（javax / jakarta 同名同包），切换到哪一侧由依赖决定，下游代码不变。</p>
 *
 * <p>继承链自下而上：本类（两侧模块：类型契约 + Servlet ↔ 抽象层骨架）→
 * {@code CAbstractBaseAuthFilter}（auth-base：会话加载等非 Security 公共逻辑，<b>只用抽象层类型</b>）→
 * {@code CAbstractAuthFilter}（auth-spring：Security 认证信息构造，业务直接继承）。</p>
 *
 * <p>安全过滤器链装配时需按"认证过滤器"这一确定类型注入：若直接按 {@link OncePerRequestFilter} 注入，
 * 会与 Spring Boot 内置的同类型 bean（字符编码过滤器、请求体表单内容过滤器等）产生注入歧义。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)}：Servlet 骨架，
 *   把容器对象包装为抽象层对象后交给抽象层版本（子类<b>不应</b>覆写本方法）。</li>
 *   <li>{@code doFilterInternal(CHttpRequest, CHttpResponse, CFilterChain)}：抽象层骨架，默认直接放行；
 *   子类覆写它来插入认证逻辑，从而不出现任何 Servlet 类型。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>骨架下沉到两侧、逻辑留在业务层</b>：Spring 的过滤器模板方法签名固定为 Servlet 类型、无法抽象，
 *   所以只有这一层"解包/包装"必须两侧各一份；认证逻辑改写成抽象层方法，javax 与 jakarta 共用同一套实现。</li>
 *   <li><b>抽象层版本默认为放行</b>：不含认证逻辑的过滤器直接继承即可；需要认证时覆写抽象层版本。</li>
 *   <li><b>异常面收窄</b>：抽象层版本的 {@code CFilterChain#doFilter} 只声明 {@link IOException}，
 *   故覆写方不必处理 {@code ServletException}；Servlet 骨架仍按 Spring 约定声明两者。</li>
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
 *     <td>子类未覆写抽象层版本</td>
 *     <td>直接放行，不做任何认证处理</td>
 *   </tr>
 *   <tr>
 *     <td>子类覆写了 Servlet 版本</td>
 *     <td>绕过抽象层骨架，两侧同构即被破坏（属误用，文档明示不应如此）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>ctool4j 认证过滤器继承链（auth-base、auth-spring）的顶层基类。</li>
 *   <li>安全过滤器链装配时，认证过滤器的注入类型契约。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>非请求认证用途的过滤器不应继承本类，否则会命中认证过滤器的注入契约。</li>
 *   <li>非 Servlet 环境（WebFlux）不适用：骨架依赖 {@code OncePerRequestFilter} 与 Servlet 请求/响应。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本类只承载契约与骨架，认证能力由继承者提供。</li>
 *   <li>骨架每请求做一次包装（两次对象创建），属既有 Filter 链的正常开销。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/3/19
 * @version 1.3
 */
public abstract class CAbstractWebAuthFilter extends OncePerRequestFilter {

    /**
     * Servlet 骨架：包装为抽象层对象后委托抽象层版本（子类不应覆写本方法）
     *
     * @param request     容器请求
     * @param response    容器响应
     * @param filterChain 容器过滤器链
     * @throws ServletException 由 Spring 过滤器模板约定声明
     * @throws IOException      链路读写失败时
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        doFilterInternal(
            CHttpServletRequest.of(request),
            CHttpServletResponse.of(response),
            CFilterChainAdapter.of(filterChain)
        );

    }

    /**
     * 抽象层骨架：默认直接放行，子类覆写以插入认证逻辑
     *
     * @param request     请求
     * @param response    响应
     * @param filterChain 过滤器链
     * @throws IOException 链路读写失败时
     */
    protected void doFilterInternal(
        @NonNull CHttpRequest request,
        @NonNull CHttpResponse response,
        @NonNull CFilterChain filterChain
    ) throws IOException {
        filterChain.doFilter(request, response);
    }

}
