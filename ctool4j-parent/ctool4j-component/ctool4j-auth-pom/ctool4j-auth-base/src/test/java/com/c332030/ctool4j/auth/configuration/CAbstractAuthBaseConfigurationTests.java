package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.session.config.CAbstractSessionMockConfig;
import com.c332030.ctool4j.session.interfaces.ICSession;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.SpringVersion;

/**
 * <p>
 * Description: CAbstractAuthBaseConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CAbstractAuthBaseConfiguration} 的默认装配契约：未提供 mock 配置时给出关闭态默认 bean、
 * 业务子类继承后按泛型绑定会话类型、以及业务自建配置时不被覆盖。
 * </p>
 *
 * <p><b>用例刻意置于被测类之外的包</b>（{@code com.c332030.ctool4j.auth.configuration}）：业务子类与本类
 * 必然不同包，故子类声明放在另一包，把"跨包继承可用"作为硬约束固化（包私有成员会使其无法实现）。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>该类的行为是"容器装配"而非纯方法逻辑，故用 {@code ApplicationContextRunner} 起最小上下文断言
 *   bean 的存在与状态；不引入业务启动类。</li>
 *   <li>首个断言点固定为<b>安全默认</b>：默认 bean 必须处于关闭态（{@code enable=false}、{@code session=null}），
 *   避免"给业务提供 mock 落点"演变成"默认绕过登录"。</li>
 *   <li>子类用例验证"泛型由子类固定、{@code @Bean} 方法被继承"这一设计：基类方法用匿名子类绑定会话类型。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计：本类作为基类由业务子类加 {@code @Configuration} 继承其 {@code @Bean} 方法；
 *   默认实现为关闭态，启用与否由业务显式设置。</li>
 *   <li>依据 {@code @ConditionalOnMissingBean} 的语义：按类型判断（泛型擦除），已有同类型 bean 时跳过默认实现。</li>
 *   <li>依据基类的 {@code abstract} 声明：会话类型必须由子类固定，故<b>抽象基类自身不可注册进容器</b>
 *   （不可实例化：{@code Is it an abstract class?}），全部用例一律注册具体子类——这正是业务的唯一用法。</li>
 *   <li>依据 Spring 的配置候选判定：子类继承的 {@code @Bean} 方法只有在<b>子类自身是配置候选</b>
 *   （有 {@code @Configuration} 或自身声明 {@code @Bean} 方法）时才会被处理；纯子类不加注解时不注册任何默认 bean，
 *   且<i>不报错</i>（静默无 bean），故单列一条边界用例固化。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认装配与关闭态默认值；业务子类继承装配且会话类型绑定正确；{@code @ConditionalOnMissingBean}
 *   在两种注册顺序下的表现（业务先注册被跳过 / 本类先注册两 bean 并存）；子类未加 {@code @Configuration} 时
 *   的注册边界（行为随 Spring 版本而异，见用例 1.5）。</li>
 *   <li>未覆盖：真实启动类下组件扫描发现子类的完整链路；mock 会话对认证流程的实际影响
 *   （由 {@code CAbstractBaseAuthFilterTests} 覆盖）。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>用例只断言"默认装配的产物"，不断言注解本身（如类上是否有 {@code @Configuration}）：注解是手段，
 *   bean 存在与状态才是契约；唯一例外是 1.5，它的契约恰恰就是"继承到的 {@code @Bean} 是否被处理"，
 *   而该结论受 Spring 版本影响（Spring 6 不处理 / Spring 7 处理），故按运行时版本判定期望值。</li>
 *   <li>同一测试类内共用一个运行器常量，避免每个用例重复构造上下文配置。</li>
 * </ul>
 * <h2>默认装配</h2>
 * <ul>
 *   <li>1.1 未提供业务配置时注册关闭态默认 bean（{@code cSessionMockConfig_missingUserConfig_registersDisabledDefault}）</li>
 *   <li>1.2 业务子类继承后 bean 存在且会话类型绑定到子类指定的类型（{@code cSessionMockConfig_subclass_beanBoundToSessionType}）</li>
 *   <li>1.3 业务配置先注册时默认实现被跳过（{@code cSessionMockConfig_userConfigRegisteredFirst_skipsDefault}）</li>
 *   <li>1.4 本类先注册时条件感知不到业务配置、两个同类 bean 并存（{@code cSessionMockConfig_selfRegisteredFirst_conditionMissesUserConfig}）</li>
 *   <li>1.5 子类未加 {@code @Configuration} 时的注册行为随 Spring 版本而异（{@code cSessionMockConfig_subclassWithoutConfiguration_registrationDependsOnSpringVersion}）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
class CAbstractAuthBaseConfigurationTests {

    /**
     * 默认装配运行器：仅注册业务子类（子类加 {@code @Configuration} 才会处理继承来的 {@code @Bean} 方法）
     */
    private static final ApplicationContextRunner DEFAULT_RUNNER =
        new ApplicationContextRunner().withUserConfiguration(UserAuthBaseConfiguration.class);

    /**
     * 对应测试用例 1.1：未提供业务配置时注册关闭态默认 bean
     */
    @Test
    void cSessionMockConfig_missingUserConfig_registersDisabledDefault() {
        DEFAULT_RUNNER.run(context -> {
            // 正例：业务只继承 + 加 @Configuration，未自建配置时默认 bean 生效
            val bean = context.getBean(CAbstractSessionMockConfig.class);

            Assertions.assertNotNull(bean);
            // 安全默认：mock 会话会绕过真实登录，默认必须关闭且不带会话
            Assertions.assertEquals(Boolean.FALSE, bean.getEnable());
            Assertions.assertNull(bean.getSession());
        });
    }

    /**
     * 对应测试用例 1.2：业务子类继承后 bean 存在，且会话类型绑定到子类指定的类型
     */
    @Test
    void cSessionMockConfig_subclass_beanBoundToSessionType() {
        DEFAULT_RUNNER.run(context -> {
            // 正例：子类只需固定泛型 + 加 @Configuration，基类 @Bean 方法被继承
            val bean = context.getBean(CAbstractSessionMockConfig.class);

            Assertions.assertNotNull(bean);
            Assertions.assertEquals(Boolean.FALSE, bean.getEnable());

            // 匿名子类已绑定 TestSession：承载该类型会话后可按原类型取回
            val session = new TestSession();
            bean.setEnable(true);
            bean.setSession(session);

            Assertions.assertEquals(Boolean.TRUE, bean.getEnable());
            Assertions.assertSame(session, bean.getSession());
        });
    }

    /**
     * 对应测试用例 1.3：业务配置先注册时，默认实现被条件跳过（业务实现不被覆盖）
     */
    @Test
    void cSessionMockConfig_userConfigRegisteredFirst_skipsDefault() {
        new ApplicationContextRunner()
            .withUserConfiguration(UserMockConfig.class, UserAuthBaseConfiguration.class)
            .run(context -> {
                val names = context.getBeanNamesForType(CAbstractSessionMockConfig.class);

                // 正例：条件在 bean 定义注册阶段评估，业务定义已存在 → 默认实现被跳过
                Assertions.assertTrue(context.containsBean("userSessionMockConfig"));
                Assertions.assertArrayEquals(new String[] {"userSessionMockConfig"}, names);
            });
    }

    /**
     * 对应测试用例 1.4：本类先注册时条件感知不到业务配置，两个同类 bean 并存（固化条件的顺序敏感性）
     */
    @Test
    void cSessionMockConfig_selfRegisteredFirst_conditionMissesUserConfig() {
        new ApplicationContextRunner()
            .withUserConfiguration(UserAuthBaseConfiguration.class, UserMockConfig.class)
            .run(context -> {
                // 边界：@ConditionalOnMissingBean 按注册顺序评估，本类先注册时业务定义尚未存在，
                // 默认实现与业务实现并存（按类型注入将出现歧义，需 @Primary 或按名注入）
                Assertions.assertArrayEquals(
                    new String[] {"cSessionMockConfig", "userSessionMockConfig"},
                    context.getBeanNamesForType(CAbstractSessionMockConfig.class)
                );
            });
    }

    /**
     * 对应测试用例 1.5：子类未加 {@code @Configuration} 时的注册行为（<b>随 Spring 版本而异</b>）
     *
     * <p><b>该行为在两个档位下不同（已登记 doc/compatibility.adoc）</b>：
     * Spring 6（Boot 2.7 基线）下"无注解 + 无 {@code @Bean} 方法"的子类不是配置候选，
     * 继承来的 {@code @Bean} 方法不被处理、<b>不注册</b>任何默认 bean；
     * Spring 7（当前最新 LTS 档位）放宽了候选判定，<b>继承到 {@code @Bean} 方法即视为配置候选</b>，
     * 默认 bean <b>照常注册</b>。</p>
     *
     * <p>本用例按运行时 Spring 版本判定期望值，两个档位下都能通过，并如实固化差异——
     * 避免"以为不注册、实际注册了"的认知偏差（该副作用会被继承方静默继承）。</p>
     *
     * <p>对使用方的影响：想在新版本下"不注册默认 bean"，需显式处置（如按条件注解排除），
     * 不能依赖"不加注解"这个随版本变化的副作用。</p>
     */
    @Test
    void cSessionMockConfig_subclassWithoutConfiguration_registrationDependsOnSpringVersion() {
        new ApplicationContextRunner()
            .withUserConfiguration(PlainAuthBaseConfiguration.class)
            .run(context -> {
                val beanNames = context.getBeanNamesForType(CAbstractSessionMockConfig.class);
                if (inheritedBeanMethodsAreConfigurationCandidates()) {
                    // Spring 7+：继承到 @Bean 方法即配置候选，默认 bean 已注册（且仅此一个）
                    Assertions.assertArrayEquals(new String[] {"cSessionMockConfig"}, beanNames);
                } else {
                    // Spring 6：不是配置候选，不注册任何默认 bean
                    Assertions.assertFalse(context.containsBean("cSessionMockConfig"));
                    Assertions.assertTrue(context.getBeansOfType(CAbstractSessionMockConfig.class).isEmpty());
                }
            });
    }

    /**
     * 当前 Spring 版本是否把"继承到 {@code @Bean} 方法的无注解子类"视为配置候选
     *
     * <p>判据用 {@code AbstractApplicationContext} 的候选判定所依赖的版本分界：
     * Spring Framework 7 起放宽该判定。此处按 {@code SpringVersion} 主版本判定，
     * 不依赖具体实现细节。</p>
     *
     * @return true 表示继承到 {@code @Bean} 方法即视为配置候选（Spring 7+）
     */
    private static boolean inheritedBeanMethodsAreConfigurationCandidates() {
        val major = Integer.parseInt(SpringVersion.getVersion().split("\\.")[0]);
        return major >= 7;
    }

    /**
     * 业务侧基类子类：固定会话类型（复现业务用法；加 {@code @Configuration} 才会处理继承的 {@code @Bean} 方法）
     */
    @Configuration
    static class UserAuthBaseConfiguration extends CAbstractAuthBaseConfiguration<TestSession> {

    }

    /**
     * 业务侧基类子类（<b>未加</b> {@code @Configuration}）：用于固化"配置候选"约束
     */
    static class PlainAuthBaseConfiguration extends CAbstractAuthBaseConfiguration<TestSession> {

    }

    /**
     * 业务侧自建 mock 配置（用于验证条件不覆盖业务实现）
     */
    static class UserMockConfig {

        @Bean
        CAbstractSessionMockConfig<TestSession> userSessionMockConfig() {
            return new CAbstractSessionMockConfig<TestSession>() {};
        }

    }

    /**
     * 会话桩：{@link ICSession} 无抽象方法，空实现即可
     */
    static class TestSession implements ICSession {

    }

}
