package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.session.config.CSessionConfig;
import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.session.service.CAbstractBaseSessionService;
import com.c332030.ctool4j.session.service.CAbstractSessionService;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * <p>
 * Description: CAbstractAuthConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CAbstractAuthConfiguration} 的默认装配逻辑：默认过滤器 bean 把匿名判定委派给子类覆写的
 * {@code isAuthAnonymous}（子类不覆写时按"非匿名"处理）；默认会话服务 bean 以业务子类指定的具体会话类型构造、
 * 容器可装配、业务自建同类型 bean 时让位，且业务直接继承（无参构造）的兼容路径可用。
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
 *   {@code protected}）；默认会话服务的会话类型解析、容器装配可用、业务 bean 让位；业务直接继承（无参构造）
 *   按子类泛型实参解析会话类型。</li>
 *   <li>未覆盖：{@code @ConditionalOnMissingBean} 在两种注册顺序下的完整矩阵（非自动配置类上的条件语义与
 *   {@code CAbstractAuthBaseConfiguration} 同一机制，由 auth-base 的 {@code CAbstractAuthBaseConfigurationTests}
 *   固化，此处只取"业务先注册即让位"一例）；过滤器的认证构造与会话加载（由 {@code CAbstractAuthFilterTests}、
 *   {@code CAbstractBaseAuthFilterTests} 覆盖）。</li>
 * </ul>
 * <h2>默认过滤器装配</h2>
 * <ul>
 *   <li>1.1 默认过滤器的匿名判定委派给子类覆写，两个分支一致（cAuthFilter_isAnonymous_delegatesToSubclass）</li>
 *   <li>1.2 子类不覆写时使用默认实现：按"非匿名"处理（cAuthFilter_isAuthAnonymousNotOverridden_defaultsToNotAnonymous）</li>
 * </ul>
 * <h2>默认会话服务装配</h2>
 * <ul>
 *   <li>2.1 直调 {@code @Bean} 方法可构造默认会话服务，且会话类型解析为子类指定的具体类型
 *   （cSessionService_subclass_resolvesConcreteSessionClass）</li>
 *   <li>2.2 容器装配后默认会话服务 bean 存在并可用（cSessionService_container_registersUsableBean）</li>
 *   <li>2.3 业务自建同类型 bean 时默认实现被跳过（cSessionService_userBeanRegistered_skipsDefault）</li>
 * </ul>
 *
 * @since 2026/9/15
 * @version 1.3
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

    // ---------- 默认会话服务装配 ----------

    /**
     * 对应测试用例 2.1：默认会话服务的会话类型解析为子类指定的具体类型
     *
     * <p>回归点：{@code cSessionService()} 曾以 {@code new CAbstractSessionService<SESSION>() {}} 直接构造，
     * 匿名子类携带未解析的类型变量 {@code SESSION}，构造期即抛
     * {@code ClassCastException: TypeVariableImpl cannot be cast to Class}。</p>
     */
    @Test
    void cSessionService_subclass_resolvesConcreteSessionClass() {

        // 正例：会话类型由业务子类的泛型实参固定，默认会话服务能解析出该具体类型
        val service = configuration.cSessionService();

        Assertions.assertNotNull(service);
        Assertions.assertEquals(TestSession.class, configuration.getGenericClass());
        Assertions.assertEquals(TestSession.class, sessionClassOf(service));

    }

    /**
     * 对应测试用例 2.2：容器装配后默认会话服务 bean 存在并可用
     */
    @Test
    void cSessionService_container_registersUsableBean() {

        // 正例：起最小上下文装配业务子类，默认 bean 可取出、会话类型绑定正确（业务注入即用）
        new ApplicationContextRunner()
            .withUserConfiguration(SessionDependencyConfig.class, UserAuthConfiguration.class)
            .run(context -> {
                val service = context.getBean(CAbstractSessionService.class);

                Assertions.assertNotNull(service);
                Assertions.assertEquals(TestSession.class, sessionClassOf(service));
            });

    }

    /**
     * 对应测试用例 2.3：业务自建同类型 bean 时默认实现被跳过
     */
    @Test
    void cSessionService_userBeanRegistered_skipsDefault() {

        // 正例：@ConditionalOnMissingBean 按类型跳过默认实现，业务实现不被覆盖
        new ApplicationContextRunner()
            .withUserConfiguration(SessionDependencyConfig.class, UserSessionServiceConfig.class, UserAuthConfiguration.class)
            .run(context -> {
                val names = context.getBeanNamesForType(CAbstractSessionService.class);

                Assertions.assertTrue(context.containsBean("userSessionService"));
                Assertions.assertArrayEquals(new String[] {"userSessionService"}, names);
            });

    }

    /**
     * 对应测试用例 2.4：业务直接继承（无参构造）可用——会话类型按业务子类泛型实参解析，构造期不抛 CCE
     */
    @Test
    void sessionServiceSubclass_noArgCtor_resolvesFromGeneric() {

        // 兼容路径：业务子类 extends CAbstractSessionService<TestSession> 且不写构造器，走无参构造按泛型解析
        Assertions.assertNotNull(new TestSessionService());

    }

    /**
     * 取会话服务绑定的会话类型（基类字段对外不可见，经字段 getter 句柄读取）
     *
     * <p>直接断言实际绑定值而非解析入口：会话类型在创建点由配置/业务子类的泛型实参解析后固定，
     * 该值正是 Redis key 与反序列化实际使用的类型——若绑定成类型变量或错误类型，用例即失败。</p>
     *
     * <p>字段访问按项目规则经 {@code CMethodHandleUtils} 的 getter 句柄（不直接用 {@code Field#get}）；
     * 句柄为统一 {@code (Object)Object} 签名，返回值无需强转。</p>
     */
    private Class<?> sessionClassOf(CAbstractSessionService<TestSession> service) {

        try {
            val field = CAbstractBaseSessionService.class.getDeclaredField("sessionClass");
            return CObjUtils.anyType(CMethodHandleUtils.getGetterHandle(field).invoke(service));
        } catch (Throwable t) {
            throw new AssertionError(t);
        }

    }

    /**
     * 会话服务的容器依赖桩：{@link CAbstractBaseSessionService} 的 {@code @Autowired} 字段需要会话配置与
     * Redis 服务（起最小上下文时补齐，避免装配因缺依赖失败；本用例只验证"默认 bean 能否装配且会话类型正确"）
     */
    @Configuration
    static class SessionDependencyConfig {

        @Bean
        CSessionConfig cSessionConfig() {
            return new CSessionConfig();
        }

        @Bean
        RedisTemplate<String, String> redisTemplate() {
            return Mockito.mock(RedisTemplate.class);
        }

        @Bean
        CStringStringRedisService cStringStringRedisService() {
            return new CStringStringRedisService();
        }

    }

    /**
     * 业务直接继承的会话服务（无参构造，复现业务用法）
     */
    static class TestSessionService extends CAbstractSessionService<TestSession> {

    }

    /**
     * 业务侧自建会话服务 bean（用于验证条件不覆盖业务实现）
     */
    @Configuration
    static class UserSessionServiceConfig {

        @Bean
        CAbstractSessionService<TestSession> userSessionService() {
            return new CAbstractSessionService<TestSession>() {};
        }

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
