package com.c332030.ctool4j.auth.filter;

import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.val;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * <p>
 * Description: CAbstractAuthFilter
 * </p>
 *
 * <p>Spring Security 认证过滤器抽象基类，<b>业务侧直接继承本类</b>。在 auth-base 的
 * {@link CAbstractBaseAuthFilter}（过滤器骨架、mock 会话加载、会话加载）之上，只负责 Security 相关部分：
 * 将会话构造成 {@code Authentication} 并写入安全上下文。</p>
 *
 * <p>继承链自下而上：{@code com.c332030.ctool4j.web.filter.CAbstractWebAuthFilter}（web：类型契约）→
 * {@link CAbstractBaseAuthFilter}（auth-base：过滤器骨架与公共会话加载）→ 本类（auth-spring：Security 认证构造）。
 * 因此安全过滤器链按 web 的类型契约注入时，注入到的即业务继承本类而来的过滤器 bean。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@link #setAuthentication}：普通会话构造 Security 认证信息并写入安全上下文（匿名 / 已认证两态）。</li>
 *   <li>{@link #setMockAuthentication}：mock 会话无条件构造为已认证（不经 {@link #isAnonymous} 判定）。</li>
 *   <li>{@link #isAnonymous}：抽象方法，业务实现会话是否匿名的判定。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>匿名判定：{@link #isAnonymous} 返回 true 视为匿名，构造 {@link AnonymousAuthenticationToken}
 *       （key 用固定常量 {@code ANONYMOUS_KEY}，避免随会话 token 变化影响 hashCode）；否则构造
 *       {@link UsernamePasswordAuthenticationToken#authenticated}。</li>
 *   <li>凭据：会话不持有 credentials，构造认证时一律传 {@code null}。</li>
 *   <li>mock 会话语义：{@code enable = true} 且配置了会话时无条件视为已登录（见 {@link #setMockAuthentication}），
 *       与真实会话的匿名判定无关；仅供开发/测试，禁止生产启用。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>mock 未启用（或启用但未配置 session）</td>
 *     <td>走基类真实会话加载流程（未配置 session 时由基类记 warn 日志）</td>
 *   </tr>
 *   <tr>
 *     <td>会话不存在（未携带 token / 解析失败 / 查不到会话）</td>
 *     <td>不写认证信息，请求保持未认证状态（基类 {@link CAbstractBaseAuthFilter#loadAuthentication} 行为）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>引入 Spring Security 的请求级认证过滤器（业务继承并实现 {@link #isAnonymous}）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不引入 Spring Security 的场景应继承 {@link CAbstractBaseAuthFilter}，不应引入本类。</li>
 *   <li>"解析失败即拒绝请求"需覆写 {@link #doFilterInternal} 改造，基类失败静默放行。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>认证构造依赖 {@link CSpringSecurityUtils} 的线程内安全上下文，异步线程不自动传递。</li>
 *   <li>{@link #isAnonymous} 由业务实现，判定错误会导致匿名/已认证态与预期不符（如误把已登录会话判为匿名）。</li>
 * </ul>
 *
 * @param <SESSION> 会话类型（Security 相关，下界 {@link ICSecuritySession}）
 *
 * @author c332030
 * @since 2026/3/16
 * @version 1.1
 */
public abstract class CAbstractAuthFilter<SESSION extends ICSecuritySession> extends CAbstractBaseAuthFilter<SESSION> {

    /**
     * 匿名认证 token 的固定 key（用于 hashCode/equals，避免随会话 token 变化）
     */
    private static final String ANONYMOUS_KEY = "anonymous";

    /**
     * 构造普通会话的 Security 认证信息并写入当前安全上下文。
     *
     * <p>{@link #isAnonymous} 为 true 构造 {@link AnonymousAuthenticationToken}（权限取
     * {@link CSpringSecurityUtils#ANONYMOUS_AUTHORITIES}）；否则构造已认证的
     * {@link UsernamePasswordAuthenticationToken}（权限取 {@link ICSecuritySession#getAuthorities()}，凭据为 null）。</p>
     *
     * @param session 已加载的会话（非 null）
     */
    @Override
    protected void setAuthentication(SESSION session) {

        val authentication = isAnonymous(session)
            ? new AnonymousAuthenticationToken(ANONYMOUS_KEY, session, CSpringSecurityUtils.ANONYMOUS_AUTHORITIES)
            : UsernamePasswordAuthenticationToken.authenticated(session, null, session.getAuthorities());

        CSpringSecurityUtils.setAuthentication(authentication);

    }

    /**
     * 构造 mock 会话的 Security 认证信息：mock 会话语义上为已登录态，无条件按已认证处理，不经 {@link #isAnonymous} 判定。
     *
     * @param session mock 会话（非 null）
     */
    @Override
    protected void setMockAuthentication(SESSION session) {

        val authentication = UsernamePasswordAuthenticationToken.authenticated(
            session,
            null,
            session.getAuthorities()
        );
        CSpringSecurityUtils.setAuthentication(authentication);

    }

    /**
     * 判断会话是否为匿名（未认证）
     *
     * @param session 会话
     * @return true 表示匿名/未认证，将构造匿名认证信息；false 表示已认证
     */
    public abstract boolean isAnonymous(SESSION session);

}
