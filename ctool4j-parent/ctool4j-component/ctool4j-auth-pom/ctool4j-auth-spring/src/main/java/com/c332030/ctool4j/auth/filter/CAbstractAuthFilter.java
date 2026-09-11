package com.c332030.ctool4j.auth.filter;

import com.c332030.ctool4j.auth.config.CAbstractSpringSecurityMockSessionConfig;
import com.c332030.ctool4j.auth.util.CAuthUtils;
import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.session.service.CAbstractSessionService;
import com.c332030.ctool4j.spring.security.filter.CAbstractJwtFilter;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import com.c332030.ctool4j.web.util.CTokenUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: CAbstractAuthFilter
 * </p>
 *
 * <p>认证 JWT 过滤器抽象基类，继承 {@link CAbstractJwtFilter}。请求进入时：可选注入 mock 会话 → 解析 jwt 得到 token
 * → 加载会话 → 构造 Spring Security 认证信息写入安全上下文。</p>
 *
 * <ul>
 *   <li>匿名判定：抽象方法 {@link #isAnonymous} 返回 true 视为匿名，构造 {@link AnonymousAuthenticationToken}
 *       （key 用固定常量 {@code ANONYMOUS_KEY}，避免随会话 token 变化影响 hashCode）；否则构造
 *       {@link UsernamePasswordAuthenticationToken#authenticated}。</li>
 *   <li>凭据：会话不持有 credentials，构造认证时一律传 {@code null}。</li>
 * </ul>
 *
 * <p>注意：{@code loadToken} 抛出的异常会被 {@code doFilterInternal} 捕获并继续放行（由后续 Security 授权规则拦截），
 * 若期望"解析失败即拒绝"需在此处改造；mock（{@link CAbstractSpringSecurityMockSessionConfig}）仅供开发/测试。</p>
 *
 * @author c332030
 * @since 2026/3/16
 * @version 1.0
 */
@CustomLog
public abstract class CAbstractAuthFilter<SESSION extends ICSecuritySession> extends CAbstractJwtFilter {

    /**
     * 匿名认证 token 的固定 key（用于 hashCode/equals，避免随会话 token 变化）
     */
    private static final String ANONYMOUS_KEY = "anonymous";

    @Autowired
    CAbstractSpringSecurityMockSessionConfig<SESSION> mockSessionConfig;

    @Autowired
    CAbstractSessionService<SESSION> sessionService;

    /**
     * 认证过滤器主流程：解析并加载 token 后放行。
     *
     * <p>{@code loadToken} 的异常在本方法内被捕获记 error 日志（不中断链路），随后无条件继续
     * {@code filterChain.doFilter}，由后续 Spring Security 授权规则拦截未认证请求；
     * 该"失败静默放行"为刻意设计，见类级说明。</p>
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤器链，认证结果写入安全上下文后继续执行
     * @throws ServletException 链路内下游过滤器/Servlet 抛出时透传
     * @throws IOException      链路内下游过滤器/Servlet 抛出时透传
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            loadToken(request);
        } catch (Exception e) {
            log.error("loadToken error", e);
        }

        filterChain.doFilter(request, response);

    }

    private void loadToken(HttpServletRequest request) {

        if(loadMockSession()) {
            return;
        }

        // 可能收到其他系统误传的 token：jwt 解析失败（内部静默返回 null）或按 token 查不到会话时直接返回，
        // 不写入认证信息，请求保持未认证状态，交由后续 Spring Security 授权规则决定放行/拦截；
        // 解析失败静默处理不影响接口安全。
        val jwt = CTokenUtils.getHeaderToken(request);
        val token = CAuthUtils.getTokenByJwt(jwt);
        if(CValidUtils.isNotValid(token)) {
            log.debug("no token");
            return;
        }
        val session = sessionService.get(token);
        if(session == null) {
            log.debug("can't find session, token: {}", token);
            return;
        }
        CTokenUtils.setToken(request, token);

        val authentication = isAnonymous(session)
            ? new AnonymousAuthenticationToken(ANONYMOUS_KEY, session, CSpringSecurityUtils.ANONYMOUS_AUTHORITIES)
            : UsernamePasswordAuthenticationToken.authenticated(session, null, session.getAuthorities());

        CSpringSecurityUtils.setAuthentication(authentication);

    }

    /**
     * 判断会话是否为匿名（未认证）
     *
     * @param session 会话
     * @return true 表示匿名/未认证，将构造匿名认证信息；false 表示已认证
     */
    public abstract boolean isAnonymous(SESSION session);

    private boolean loadMockSession() {

        if(!mockSessionConfig.getEnable()) {
            return false;
        }

        val session = mockSessionConfig.getSession();
        if(null == session) {
            log.warn("未配置 mock session");
            return false;
        }
        val authentication = UsernamePasswordAuthenticationToken.authenticated(
            session,
            null,
            session.getAuthorities()
        );
        CSpringSecurityUtils.setAuthentication(authentication);

        return true;
    }

}
