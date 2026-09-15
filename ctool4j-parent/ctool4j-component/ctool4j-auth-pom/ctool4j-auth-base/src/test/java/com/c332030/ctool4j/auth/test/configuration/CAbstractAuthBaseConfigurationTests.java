package com.c332030.ctool4j.auth.config;

import com.c332030.ctool4j.auth.configuration.CAbstractAuthBaseConfiguration;
import com.c332030.ctool4j.session.config.CAbstractSessionMockConfig;
import com.c332030.ctool4j.session.interfaces.ICSession;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CAbstractAuthBaseConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CAbstractAuthBaseConfiguration} 的默认装配契约：未提供 mock 配置时给出关闭态默认 bean、
 * 业务子类继承后按泛型绑定会话类型、以及业务自建配置时不被覆盖。
 * </p>
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
 *   <li>依据 Spring 的配置类处理顺序：{@code @Configuration} 之外、仅有 {@code @Bean} 方法的类按"精简配置类"处理，
 *   故基类被显式注册时其 {@code @Bean} 方法同样生效。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认装配与关闭态默认值；业务子类继承装配且会话类型绑定正确；{@code @ConditionalOnMissingBean}
 *   在两种注册顺序下的表现（业务先注册被跳过 / 本类先注册两 bean 并存）。</li>
 *   <li>未覆盖：真实启动类下组件扫描发现子类的完整链路；mock 会话对认证流程的实际影响
 *   （由 {@code CAbstractBaseAuthFilterTests} 覆盖）。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>用例只断言"默认装配的产物"，不断言注解本身（如类上是否有 {@code @Configuration}）：注解是手段，
 *   bean 存在与状态才是契约。</li>
 *   <li>同一测试类内共用一个运行器常量，避免每个用例重复构造上下文配置。</li>
 * </ul>
 * <h2>默认装配</h2>
 * <ul>
 *   <li>1.1 未提供业务配置时注册关闭态默认 bean（{@code cSessionMockConfig_missingUserConfig_registersDisabledDefault}）</li>
 *   <li>1.2 业务子类继承后 bean 存在且会话类型绑定到子类指定的类型（{@code cSessionMockConfig_subclass_beanBoundToSessionType}）</li>
 *   <li>1.3 业务配置先注册时默认实现被跳过（{@code cSessionMockConfig_userConfigRegisteredFirst_skipsDefault}）</li>
 *   <li>1.4 本类先注册时条件感知不到业务配置、两个同类 bean 并存（{@code cSessionMockConfig_selfRegisteredFirst_conditionMissesUserConfig}）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
class CAbstractAuthBaseConfigurationTests {

    /**
     * 默认装配运行器：仅注册基类，等价于业务侧显式引入本类（基类自身不带 {@code @Configuration}，组件扫描发现不到）
     */
    private static final ApplicationContextRunner DEFAULT_RUNNER =
        new ApplicationContextRunner().withUserConfiguration(CAbstractAuthBaseConfiguration.class);

    /**
     * 对应测试用例 1.1：未提供业务配置时注册关闭态默认 bean
     */
    @Test
    void cSessionMockConfig_missingUserConfig_registersDisabledDefault() {
        DEFAULT_RUNNER.run(context -> {
            // 正例：精简配置类（无 @Configuration）的 @Bean 方法同样生效
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
        new ApplicationContextRunner()
            .withUserConfiguration(UserAuthBaseConfiguration.class)
            .run(context -> {
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
            .withUserConfiguration(UserMockConfig.class, CAbstractAuthBaseConfiguration.class)
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
            .withUserConfiguration(CAbstractAuthBaseConfiguration.class, UserMockConfig.class)
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
     * 业务侧基类子类：固定会话类型（复现业务用法）
     */
    @Configuration
    static class UserAuthBaseConfiguration extends CAbstractAuthBaseConfiguration<TestSession> {

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
