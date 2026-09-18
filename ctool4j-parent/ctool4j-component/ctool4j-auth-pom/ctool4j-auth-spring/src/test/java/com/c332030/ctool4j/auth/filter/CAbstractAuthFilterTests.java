package com.c332030.ctool4j.auth.filter;

import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 * Description: CAbstractAuthFilterTests
 * </p>
 * <p>{@code com.c332030.ctool4j.auth.filter.CAbstractAuthFilter}（CAbstractAuthFilter）的测试用例</p>
 *
 * <p>覆盖 Spring Security 认证构造部分：{@code setAuthentication} 的匿名/已认证两态、
 * {@code setMockAuthentication} 的无条件已认证，以及继承认证加载流程对 mock 分支的短路；
 * 与 Spring Security 无关的过滤器骨架、mock 会话取值与降级见 auth-base 的 {@code CAbstractBaseAuthFilterTests}。</p>
 *
 * <p><b>用例设计思路</b>：以「写入安全上下文的 {@code Authentication}」为断言对象（精确断言类型、主体、
 * 凭据与权限），而非仅断言未抛异常；</p>
 * <ul>
 *   <li>匿名/已认证两态由 {@code isAnonymous} 替身开关驱动，两分支分别断言
 *       {@link AnonymousAuthenticationToken}（权限取匿名权限）与
 *       {@link UsernamePasswordAuthenticationToken}（凭据为 null、权限取会话权限）；</li>
 *   <li>mock 分支的「不咨询匿名判定」用「{@code isAnonymous} 被调用即抛异常」的替身证明——
 *       若实现改为咨询匿名判定，用例即失败；</li>
 *   <li>会话来源（{@code loadSession}/{@code loadMockSession}）在替身中固定为返回值并计数，作为认证流程的
 *       输入接缝；认证构造与加载流程本身均走被测类真实实现，不覆写、不 mock；</li>
 *   <li>安全上下文为线程级静态状态，用例结束即清理，避免污染同 JVM 的其他用例。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据 {@code CAbstractAuthFilter} javadoc 对匿名判定（{@code isAnonymous}）、
 * 凭据为 null、mock 会话无条件已认证的约定；依据分支覆盖（匿名/已认证、mock 命中/未命中）。</p>
 *
 * <p><b>覆盖场景</b>：{@code setAuthentication} 已认证态、匿名态；{@code setMockAuthentication} 无条件已认证；
 * {@code loadAuthentication} 的 mock 命中短路、mock 未命中读真实会话。{@code isAnonymous} 为业务实现，
 * 仅按替身开关驱动，不覆盖业务判定逻辑。</p>
 * <p><b>未覆盖</b>：真实 Spring Security 过滤器链装配（{@code CSecurityConfiguration} 的容器级装配）。</p>
 *
 * <h2>认证信息构造</h2>
 * <ul>
 *   <li>1.1 非匿名：写入已认证的 UsernamePasswordAuthenticationToken（setAuthentication_notAnonymous）</li>
 *   <li>1.2 匿名：写入 AnonymousAuthenticationToken（setAuthentication_anonymous）</li>
 *   <li>1.3 mock：无条件已认证且不咨询匿名判定（setMockAuthentication_alwaysAuthenticated）</li>
 * </ul>
 * <h2>认证加载</h2>
 * <ul>
 *   <li>2.1 mock 命中：写 mock 认证且不读真实会话、不咨询匿名判定（loadAuthentication_mockHit）</li>
 *   <li>2.2 mock 未命中：读真实会话并写入认证（loadAuthentication_mockMiss_usesRealSession）</li>
 * </ul>
 *
 * @since 2026/9/13
 * @version 1.0
 * @see CAbstractAuthFilter
 */
class CAbstractAuthFilterTests {

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    /**
     * 清理测试状态：安全上下文为线程级静态状态，用例结束即清理，避免污染同 JVM 的其他用例
     */
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- 认证信息构造 ----------

    /**
     * 对应测试用例 1.1：非匿名会话写入已认证的 UsernamePasswordAuthenticationToken
     */
    @Test
    void setAuthentication_notAnonymous() {

        // 正例：isAnonymous=false → 已认证 token（主体为会话、凭据为 null、权限取会话权限）
        val session = new SessionStub();
        val filter = newFilter(false, false);

        filter.setAuthentication(session);

        val authentication = CSpringSecurityUtils.getAuthentication();
        Assertions.assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        Assertions.assertFalse(authentication instanceof AnonymousAuthenticationToken);
        Assertions.assertTrue(authentication.isAuthenticated());
        Assertions.assertSame(session, authentication.getPrincipal());
        Assertions.assertNull(authentication.getCredentials());
        Assertions.assertEquals(session.getAuthorities(), authentication.getAuthorities());

    }

    /**
     * 对应测试用例 1.2：匿名会话写入 AnonymousAuthenticationToken
     */
    @Test
    void setAuthentication_anonymous() {

        // 正例：isAnonymous=true → 匿名 token（主体为会话、权限取匿名权限）
        val session = new SessionStub();
        val filter = newFilter(true, false);

        filter.setAuthentication(session);

        val authentication = CSpringSecurityUtils.getAuthentication();
        Assertions.assertInstanceOf(AnonymousAuthenticationToken.class, authentication);
        Assertions.assertSame(session, authentication.getPrincipal());
        Assertions.assertEquals(CSpringSecurityUtils.ANONYMOUS_AUTHORITIES, authentication.getAuthorities());

    }

    /**
     * 对应测试用例 1.3：mock 会话无条件已认证且不咨询匿名判定
     */
    @Test
    void setMockAuthentication_alwaysAuthenticated() {

        // 分支：isAnonymous 被调用即抛异常 → mock 会话仍按已认证处理，证明不咨询匿名判定
        val session = new SessionStub();
        val filter = newFilter(false, true);

        filter.setMockAuthentication(session);

        val authentication = CSpringSecurityUtils.getAuthentication();
        Assertions.assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        Assertions.assertFalse(authentication instanceof AnonymousAuthenticationToken);
        Assertions.assertTrue(authentication.isAuthenticated());
        Assertions.assertSame(session, authentication.getPrincipal());
        Assertions.assertNull(authentication.getCredentials());
        Assertions.assertEquals(session.getAuthorities(), authentication.getAuthorities());

    }

    // ---------- 认证加载 ----------

    /**
     * 对应测试用例 2.1：mock 命中时写 mock 认证且不读真实会话、不咨询匿名判定
     */
    @Test
    void loadAuthentication_mockHit() {

        // 正例：mock 命中 → 短路真实会话读取与匿名判定，按已认证写入
        val mockSession = new SessionStub();
        val filter = newFilter(true, true);
        filter.mockSessionToLoad = mockSession;

        filter.loadAuthentication(request);

        val authentication = CSpringSecurityUtils.getAuthentication();
        Assertions.assertSame(mockSession, authentication.getPrincipal());
        Assertions.assertTrue(authentication.isAuthenticated());
        Assertions.assertEquals(0, filter.loadSessionCount);

    }

    /**
     * 对应测试用例 2.2：mock 未命中时读真实会话并写入认证
     */
    @Test
    void loadAuthentication_mockMiss_usesRealSession() {

        // 正例：mock 未命中 → 按当前请求读真实会话，非匿名的会话写入已认证 token
        val session = new SessionStub();
        val filter = newFilter(false, false);
        filter.sessionToLoad = session;

        filter.loadAuthentication(request);

        Assertions.assertEquals(1, filter.loadSessionCount);
        val authentication = CSpringSecurityUtils.getAuthentication();
        Assertions.assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        Assertions.assertSame(session, authentication.getPrincipal());

    }

    /**
     * 构造被测过滤器替身
     *
     * @param anonymous              {@code isAnonymous} 返回值
     * @param failIfAnonymousChecked true 时 {@code isAnonymous} 被调用即抛异常
     * @return 过滤器替身
     */
    private FilterStub newFilter(boolean anonymous, boolean failIfAnonymousChecked) {

        val filter = new FilterStub();
        filter.anonymous = anonymous;
        filter.failIfAnonymousChecked = failIfAnonymousChecked;
        return filter;

    }

    /**
     * Security 会话测试替身：权限固定，便于断言认证权限与会话权限一致
     */
    @Data
    public static class SessionStub implements ICSecuritySession {

        /**
         * 会话权限（固定值，供认证权限断言）
         */
        private static final Collection<GrantedAuthority> AUTHORITIES =
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_AUTH_FILTER_TEST"));

        private String token;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return AUTHORITIES;
        }

    }

    /**
     * 被测过滤器替身：会话来源固定为返回值并计数（认证流程的输入接缝），{@code isAnonymous} 由开关驱动
     */
    private static class FilterStub extends CAbstractAuthFilter<SessionStub> {

        /**
         * {@code loadSession} 返回值（null 表示会话不存在）
         */
        SessionStub sessionToLoad;

        /**
         * {@code loadMockSession} 返回值（null 表示 mock 未命中）
         */
        SessionStub mockSessionToLoad;

        /**
         * {@code loadSession} 调用次数
         */
        int loadSessionCount;

        /**
         * {@code isAnonymous} 返回值
         */
        boolean anonymous;

        /**
         * true 时 {@code isAnonymous} 被调用即抛异常（用于证明 mock 分支不咨询匿名判定）
         */
        boolean failIfAnonymousChecked;

        @Override
        protected SessionStub loadSession(HttpServletRequest request) {
            loadSessionCount++;
            return sessionToLoad;
        }

        @Override
        protected SessionStub loadMockSession() {
            return mockSessionToLoad;
        }

        @Override
        public boolean isAnonymous(SessionStub session) {

            if(failIfAnonymousChecked) {
                throw new IllegalStateException("isAnonymous should not be called");
            }

            return anonymous;

        }

    }

}
