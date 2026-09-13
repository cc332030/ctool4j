package com.c332030.ctool4j.auth.filter;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.session.config.CAbstractSessionMockConfig;
import com.c332030.ctool4j.session.interfaces.ICSession;
import com.c332030.ctool4j.session.service.CAbstractBaseSessionService;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>
 * Description: CAbstractBaseAuthFilterTests
 * </p>
 * <p>{@code com.c332030.ctool4j.auth.filter.CAbstractBaseAuthFilter}（CAbstractBaseAuthFilter）的测试用例</p>
 *
 * <p>覆盖与 Spring Security 无关的公共部分：过滤器骨架（异常静默 + 无条件放行）、mock 会话与真实会话的
 * 优先级与降级、认证信息设置钩子的默认实现。Security 的认证构造见 auth-spring 的
 * {@code CAbstractAuthFilterTests}。</p>
 *
 * <p><b>用例设计思路</b>：按「过滤器骨架 / 认证加载分支 / mock 会话取值」三个维度组织；</p>
 * <ul>
 *   <li>被测类的两条认证设置路径以「记录入参的替身」观察：mock 分支经 {@code setMockAuthentication}、
 *       真实会话分支经 {@code setAuthentication}，其余逻辑（模板方法、{@code loadMockSession}、
 *       {@code loadSession}）均走被测类真实实现，不覆写、不 mock；</li>
 *   <li>会话服务替身继承 {@code CAbstractBaseSessionService} 并实现 {@code getDefaultNull()}——该替身位于
 *       本包（{@code com.c332030.ctool4j.auth.filter}，与 {@code CAbstractBaseSessionService} 不同包），
 *       其可编译即证明「子类可在任意包直接继承会话服务基类」；</li>
 *   <li>依赖字段为私有（项目 lombok 配置默认 private），按项目规则经 {@code CMethodHandleUtils} 的
 *       字段 setter 句柄注入，不新增仅测试用的 setter；</li>
 *   <li>过滤器链用记录调用次数的替身，断言各分支（含异常路径）均放行、且异常不向上抛。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据 {@code CAbstractBaseAuthFilter} javadoc 对分支优先级（mock 优先、命中即短路）、
 * 兜底（会话不存在不设置认证、异常静默放行）与 mock 未配置时降级的约定；依据等价类/边界值/分支覆盖
 * （启用/未启用/未配置、会话存在/不存在、认证设置成功/抛异常）。</p>
 *
 * <p><b>覆盖场景</b>：{@code doFilterInternal} 正常与异常路径；{@code loadAuthentication} 的 mock 命中、
 * mock 未配置降级、mock 未启用、会话不存在、mock 认证默认实现；{@code loadMockSession} 的未启用、
 * {@code enable} 为 null、启用且有会话、启用且无会话。{@code loadSession} 为直通委托，不单独覆盖。</p>
 * <p><b>未覆盖</b>：真实 Redis 会话存取与 jwt 解析（分别由 {@code CAbstractBaseSessionServiceTests}、
 * {@code CAuthUtilsTests} 的用例覆盖）；{@code OncePerRequestFilter} 自身的请求去重（框架行为，由 Spring 保证）。</p>
 *
 * <h2>过滤器骨架</h2>
 * <ul>
 *   <li>1.1 会话命中：设置认证信息后放行（doFilterInternal_authenticationSetAndPassThrough）</li>
 *   <li>1.2 认证设置抛异常：静默捕获、不向上抛、仍放行（doFilterInternal_authenticationError_passesThrough）</li>
 * </ul>
 * <h2>认证加载分支</h2>
 * <ul>
 *   <li>2.1 mock 命中：走 mock 认证且不读真实会话（loadAuthentication_mockHit_skipsRealSession）</li>
 *   <li>2.2 mock 启用但未配置会话：降级读真实会话（loadAuthentication_mockWithoutSession_fallsBack）</li>
 *   <li>2.3 mock 未启用：读真实会话（loadAuthentication_mockDisabled_usesRealSession）</li>
 *   <li>2.4 会话不存在：不设置认证信息（loadAuthentication_noSession_skipsAuthentication）</li>
 *   <li>2.5 mock 认证默认实现：委托普通认证（loadAuthentication_mockHit_defaultMockAuthentication）</li>
 * </ul>
 * <h2>mock 会话取值</h2>
 * <ul>
 *   <li>3.1 未启用返回 null（loadMockSession_disabled_returnsNull）</li>
 *   <li>3.2 {@code enable} 为 null 视为未启用返回 null（loadMockSession_nullEnable_returnsNull）</li>
 *   <li>3.3 启用且有会话返回该会话（loadMockSession_enabledWithSession_returnsSession）</li>
 *   <li>3.4 启用但会话为空返回 null（loadMockSession_enabledWithoutSession_returnsNull）</li>
 * </ul>
 *
 * @since 2026/9/13
 * @version 1.0
 * @see CAbstractBaseAuthFilter
 */
class CAbstractBaseAuthFilterTests {

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    /**
     * 会话服务替身：提供「当前请求对应的真实会话」，隔离 Redis 与 jwt 配置；同时验证跨包继承的可行性
     */
    private SessionServiceStub sessionService;

    /**
     * 每个用例用新的会话服务替身，避免用例间状态（返回值与调用计数）串扰
     */
    @BeforeEach
    public void setUp() {
        sessionService = new SessionServiceStub();
    }

    // ---------- 过滤器骨架 ----------

    /**
     * 对应测试用例 1.1：会话命中时设置认证信息并放行
     */
    @Test
    void doFilterInternal_authenticationSetAndPassThrough() throws Exception {

        // 正例：真实会话命中 → setAuthentication(session) 且过滤器链放行一次
        val session = new SessionStub();
        val filter = newFilter(newMockConfig(null, null));
        val chain = new FilterChainStub();
        sessionService.sessionToLoad = session;

        filter.doFilterInternal(request, response, chain);

        Assertions.assertSame(session, filter.authenticated);
        Assertions.assertEquals(1, chain.count);

    }

    /**
     * 对应测试用例 1.2：认证设置抛异常时静默捕获、仍放行
     */
    @Test
    void doFilterInternal_authenticationError_passesThrough() {

        // 异常路径：setAuthentication 抛异常被捕获（不向上抛），过滤器链仍放行
        val filter = newFilter(newMockConfig(null, null));
        filter.failOnAuthentication = true;
        val chain = new FilterChainStub();
        sessionService.sessionToLoad = new SessionStub();

        Assertions.assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

        Assertions.assertNull(filter.authenticated);
        Assertions.assertEquals(1, chain.count);

    }

    // ---------- 认证加载分支 ----------

    /**
     * 对应测试用例 2.1：mock 命中时走 mock 认证且不读真实会话
     */
    @Test
    void loadAuthentication_mockHit_skipsRealSession() {

        // 正例：mock 启用且配置了会话 → 走 mock 认证，且短路真实会话读取
        val mockSession = new SessionStub();
        val filter = newFilter(newMockConfig(true, mockSession));

        filter.loadAuthentication(request);

        Assertions.assertSame(mockSession, filter.mockAuthenticated);
        Assertions.assertNull(filter.authenticated);
        Assertions.assertEquals(0, sessionService.loadSessionCount);

    }

    /**
     * 对应测试用例 2.2：mock 启用但未配置会话时降级读真实会话
     */
    @Test
    void loadAuthentication_mockWithoutSession_fallsBack() {

        // 边界：mock 启用但会话未配置 → 不短路，降级为真实会话读取
        val session = new SessionStub();
        val filter = newFilter(newMockConfig(true, null));
        sessionService.sessionToLoad = session;

        filter.loadAuthentication(request);

        Assertions.assertSame(session, filter.authenticated);
        Assertions.assertNull(filter.mockAuthenticated);
        Assertions.assertEquals(1, sessionService.loadSessionCount);
        Assertions.assertSame(request, sessionService.lastRequest);

    }

    /**
     * 对应测试用例 2.3：mock 未启用时读真实会话
     */
    @Test
    void loadAuthentication_mockDisabled_usesRealSession() {

        // 正例：mock 未启用 → 不取 mock 会话，读真实会话
        val session = new SessionStub();
        val filter = newFilter(newMockConfig(false, new SessionStub()));
        sessionService.sessionToLoad = session;

        filter.loadAuthentication(request);

        Assertions.assertSame(session, filter.authenticated);
        Assertions.assertNull(filter.mockAuthenticated);
        Assertions.assertEquals(1, sessionService.loadSessionCount);
        Assertions.assertSame(request, sessionService.lastRequest);

    }

    /**
     * 对应测试用例 2.4：会话不存在时不设置认证信息
     */
    @Test
    void loadAuthentication_noSession_skipsAuthentication() {

        // 兜底：查不到会话（loadSession 返回 null）→ 两条认证设置路径均不触发
        val filter = newFilter(newMockConfig(false, null));

        filter.loadAuthentication(request);

        Assertions.assertNull(filter.authenticated);
        Assertions.assertNull(filter.mockAuthenticated);
        Assertions.assertEquals(1, sessionService.loadSessionCount);

    }

    /**
     * 对应测试用例 2.5：mock 认证的默认实现委托普通认证
     */
    @Test
    void loadAuthentication_mockHit_defaultMockAuthentication() {

        // 分支：未覆写 setMockAuthentication 时，mock 会话走默认实现（委托 setAuthentication）
        val mockSession = new SessionStub();
        val filter = new DefaultMockFilterStub();
        inject(filter, "sessionService", sessionService);
        inject(filter, "sessionMockConfig", newMockConfig(true, mockSession));

        filter.loadAuthentication(request);

        Assertions.assertSame(mockSession, filter.authenticated);
        Assertions.assertEquals(0, sessionService.loadSessionCount);

    }

    // ---------- mock 会话取值 ----------

    /**
     * 对应测试用例 3.1：mock 未启用返回 null
     */
    @Test
    void loadMockSession_disabled_returnsNull() {

        // 边界：enable=false → 不取会话
        val filter = newFilter(newMockConfig(false, new SessionStub()));

        Assertions.assertNull(filter.loadMockSession());

    }

    /**
     * 对应测试用例 3.2：{@code enable} 为 null 视为未启用返回 null
     */
    @Test
    void loadMockSession_nullEnable_returnsNull() {

        // 边界：包装类型 Boolean 为 null（配置缺失）→ 视为未启用，不拆箱 NPE
        val filter = newFilter(newMockConfig(null, new SessionStub()));

        Assertions.assertNull(filter.loadMockSession());

    }

    /**
     * 对应测试用例 3.3：mock 启用且有会话时返回该会话
     */
    @Test
    void loadMockSession_enabledWithSession_returnsSession() {

        // 正例：enable=true 且会话已配置 → 返回配置的会话本身
        val mockSession = new SessionStub();
        val filter = newFilter(newMockConfig(true, mockSession));

        Assertions.assertSame(mockSession, filter.loadMockSession());

    }

    /**
     * 对应测试用例 3.4：mock 启用但会话为空返回 null
     */
    @Test
    void loadMockSession_enabledWithoutSession_returnsNull() {

        // 边界：enable=true 但会话未配置 → 返回 null（由调用方降级）
        val filter = newFilter(newMockConfig(true, null));

        Assertions.assertNull(filter.loadMockSession());

    }

    /**
     * 构造被测过滤器替身并注入依赖
     *
     * @param mockConfig mock 会话配置
     * @return 过滤器替身
     */
    private FilterStub newFilter(CAbstractSessionMockConfig<SessionStub> mockConfig) {

        val filter = new FilterStub();
        inject(filter, "sessionService", sessionService);
        inject(filter, "sessionMockConfig", mockConfig);
        return filter;

    }

    /**
     * 构造 mock 会话配置替身
     *
     * @param enable  是否启用
     * @param session 会话（可为 null）
     * @return mock 会话配置替身
     */
    private MockConfigStub newMockConfig(Boolean enable, SessionStub session) {

        val config = new MockConfigStub();
        config.setEnable(enable);
        config.setSession(session);
        return config;

    }

    /**
     * 注入被测类声明的私有依赖字段
     *
     * <p>依赖字段按项目 lombok 配置默认为 private，不为此新增 setter；反射访问统一经
     * {@code CMethodHandleUtils} 的字段 setter 句柄（项目规则的反射入口）。</p>
     *
     * @param target    被测对象
     * @param fieldName 字段名（{@link CAbstractBaseAuthFilter} 中声明）
     * @param value     字段值
     */
    private void inject(Object target, String fieldName, Object value) {

        try {
            val field = CAbstractBaseAuthFilter.class.getDeclaredField(fieldName);
            CMethodHandleUtils.getSetterHandle(field).invoke(target, value);
        } catch (Throwable t) {
            throw new AssertionError(t);
        }

    }

    /**
     * 会话测试替身（{@link ICSession} 实现，不依赖 Spring Security）
     */
    @Data
    public static class SessionStub implements ICSession {

        private String token;

    }

    /**
     * 会话服务测试替身：跨包继承 {@link CAbstractBaseSessionService}（位于 {@code session.service} 包）并实现
     * {@code getDefaultNull()}，同时记录 {@code loadSession} 的入参与调用次数
     */
    private static class SessionServiceStub extends CAbstractBaseSessionService<SessionStub> {

        /**
         * {@code loadSession} 返回值（null 表示会话不存在）
         */
        SessionStub sessionToLoad;

        /**
         * {@code loadSession} 收到的请求（null 表示未被调用）
         */
        HttpServletRequest lastRequest;

        /**
         * {@code loadSession} 调用次数
         */
        int loadSessionCount;

        @Override
        public SessionStub loadSession(HttpServletRequest request) {
            loadSessionCount++;
            lastRequest = request;
            return sessionToLoad;
        }

        @Override
        public SessionStub getDefaultNull() {
            return sessionToLoad;
        }

    }

    /**
     * mock 会话配置测试替身（绑定 {@link SessionStub}）
     */
    private static class MockConfigStub extends CAbstractSessionMockConfig<SessionStub> {

    }

    /**
     * 被测过滤器替身：记录两条认证设置路径的入参，并可构造认证设置抛异常的路径
     */
    private static class FilterStub extends CAbstractBaseAuthFilter<SessionStub> {

        /**
         * {@code setAuthentication} 收到的会话（null 表示未被调用）
         */
        SessionStub authenticated;

        /**
         * {@code setMockAuthentication} 收到的会话（null 表示未被调用）
         */
        SessionStub mockAuthenticated;

        /**
         * true 时 {@code setAuthentication} 抛异常（用于覆盖异常路径）
         */
        boolean failOnAuthentication;

        @Override
        protected void setAuthentication(SessionStub session) {

            if(failOnAuthentication) {
                throw new IllegalStateException("authentication error");
            }

            this.authenticated = session;

        }

        @Override
        protected void setMockAuthentication(SessionStub session) {
            this.mockAuthenticated = session;
        }

    }

    /**
     * 未覆写 {@code setMockAuthentication} 的过滤器替身：覆盖「mock 认证默认委托普通认证」分支
     */
    private static class DefaultMockFilterStub extends CAbstractBaseAuthFilter<SessionStub> {

        /**
         * {@code setAuthentication} 收到的会话（null 表示未被调用）
         */
        SessionStub authenticated;

        @Override
        protected void setAuthentication(SessionStub session) {
            this.authenticated = session;
        }

    }

    /**
     * 过滤器链测试替身：记录放行调用次数
     */
    private static class FilterChainStub implements FilterChain {

        /**
         * 放行调用次数
         */
        int count;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException {
            count++;
        }

    }

}
