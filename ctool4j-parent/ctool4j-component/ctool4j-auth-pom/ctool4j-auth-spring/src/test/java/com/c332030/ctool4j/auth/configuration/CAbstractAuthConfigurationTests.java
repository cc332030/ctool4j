package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CAbstractAuthConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CAbstractAuthConfiguration} 的唯一逻辑：默认过滤器 bean 把匿名判定委派给子类覆写的
 * {@code isAuthAnonymous}；子类不覆写时使用默认实现（按"非匿名"处理）。
 * </p>
 *
 * <p><b>用例刻意置于被测类之外的包</b>（{@code com.c332030.ctool4j.auth.configuration}）：业务子类与本类
 * 必然不同包，本类的业务用法就是跨包继承——若 {@code isAuthAnonymous} 采用包私有可见性，本测试类的子类声明
 * <b>不构成覆写</b>：包私有不会导致编译错误，而是静默走默认实现，1.1 会因此失败（这正是要靠用例捕捉的形态）。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接调用 {@code @Bean} 方法（{@code cAuthFilter()}）取得过滤器对象，<b>不启动容器</b>：本类的逻辑只有
 *   "把判定接进过滤器"一件事，而起容器会连带装配过滤器依赖的会话服务（其自身又依赖 Redis 与配置），
 *   与待验证的契约无关。</li>
 *   <li>委派关系用"同一判定的两个分支"证明：判 true / 判 false 各一次，断言过滤器返回值与子类覆写一致——
 *   若默认过滤器不再委派（如固定返回 false），1.1 即失败。</li>
 *   <li>默认值单列一例（1.2）：子类不覆写时得到"非匿名"的默认结果，且该结果与"会话自称匿名"无关——
 *   把默认语义固定下来，避免它随实现漂移。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 {@link CAbstractAuthConfiguration} 的 javadoc：默认过滤器 bean 委派 {@code isAuthAnonymous}，
 *   业务可按需覆写、不覆写时用默认实现（非匿名）。</li>
 *   <li>依据 {@code CAbstractAuthFilter#isAnonymous} 的约定：返回 true 构造匿名认证信息、false 为已认证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认过滤器对子类覆写的委派（两个分支）；子类不覆写时的默认值；跨包覆写可用（可见性为
 *   {@code protected}）。</li>
 *   <li>未覆盖：容器级装配与 {@code @ConditionalOnMissingBean} 条件（非自动配置类上的条件语义与
 *   {@code CAbstractAuthBaseConfiguration} 同一机制，由 auth-base 的 {@code CAbstractAuthBaseConfigurationTests}
 *   固化）；过滤器的认证构造与会话加载（由 {@code CAbstractAuthFilterTests}、{@code CAbstractBaseAuthFilterTests}
 *   覆盖）。</li>
 * </ul>
 * <h2>默认过滤器装配</h2>
 * <ul>
 *   <li>1.1 默认过滤器的匿名判定委派给子类覆写，两个分支一致（cAuthFilter_isAnonymous_delegatesToSubclass）</li>
 *   <li>1.2 子类不覆写时使用默认实现：按"非匿名"处理（cAuthFilter_isAuthAnonymousNotOverridden_defaultsToNotAnonymous）</li>
 * </ul>
 *
 * @since 2026/9/15
 * @version 1.1
 * @see CAbstractAuthConfiguration
 */
class CAbstractAuthConfigurationTests {

    private final UserAuthConfiguration configuration = new UserAuthConfiguration();

    /**
     * 对应测试用例 1.1：默认过滤器的匿名判定委派给子类覆写（两个分支）
     */
    @Test
    void cAuthFilter_isAnonymous_delegatesToSubclass() {
        val filter = configuration.cAuthFilter();

        // 正例：判定结果与业务子类覆写一致（委派而非固定值）
        Assertions.assertNotNull(filter);
        Assertions.assertTrue(filter.isAnonymous(new TestSession(true)));
        Assertions.assertFalse(filter.isAnonymous(new TestSession(false)));
    }

    /**
     * 对应测试用例 1.2：子类不覆写时使用默认实现，按"非匿名"处理
     */
    @Test
    void cAuthFilter_isAuthAnonymousNotOverridden_defaultsToNotAnonymous() {
        val filter = new UserAuthConfigurationWithoutOverride().cAuthFilter();

        // 边界：默认实现固定返回 false——即便会话自称匿名也不做匿名判定（"有会话即已认证"，需业务显式覆写）
        Assertions.assertFalse(filter.isAnonymous(new TestSession(true)));
        Assertions.assertFalse(filter.isAnonymous(new TestSession(false)));
    }

    /**
     * 业务侧装配子类：固定会话类型 + 覆写匿名判定（复现业务用法，且与被测类不同包）
     */
    @Configuration
    static class UserAuthConfiguration extends CAbstractAuthConfiguration<TestSession> {

        @Override
        protected boolean isAuthAnonymous(TestSession session) {
            return session.isAnonymous();
        }

    }

    /**
     * 业务侧装配子类（<b>不覆写</b> {@code isAuthAnonymous}）：复现"允许不实现"的用法
     */
    @Configuration
    static class UserAuthConfigurationWithoutOverride extends CAbstractAuthConfiguration<TestSession> {

    }

    /**
     * 会话桩：{@link ICSecuritySession} 无抽象方法，空实现 + 匿名标记即可
     */
    static class TestSession implements ICSecuritySession {

        private final boolean anonymous;

        TestSession(boolean anonymous) {
            this.anonymous = anonymous;
        }

        boolean isAnonymous() {
            return anonymous;
        }

    }

}
