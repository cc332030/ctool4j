package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.auth.filter.CAbstractAuthFilter;
import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * Description: CAbstractAuthConfiguration
 * </p>
 *
 * <p>认证模块装配基类（auth-spring）：在 auth-base 的 {@link CAbstractAuthBaseConfiguration}（mock 会话配置落点）
 * 之上，追加 Spring Security 认证过滤器的默认装配；业务侧继承本类、实现 {@link #isAuthAnonymous}，即可同时获得二者。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code cAuthFilter()}：业务未自建 {@link CAbstractAuthFilter} 时，提供一个默认（匿名子类）实现，
 *   其匿名判定委派给 {@link #isAuthAnonymous}</li>
 *   <li>{@link #isAuthAnonymous}：模板方法，业务子类实现"会话是否匿名"的判定</li>
 *   <li>继承 {@link CAbstractAuthBaseConfiguration}：同时获得默认 mock 会话配置 bean（{@code cSessionMockConfig}）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <p><b>为什么是默认 bean + 匿名子类</b></p>
 * <ul>
 *   <li>过滤器类型 {@code CAbstractAuthFilter<T>} 需与业务会话类型绑定，而基类不确定该类型，故在 {@code @Bean}
 *   方法内以匿名子类 {@code new CAbstractAuthFilter<T>() {}} 绑定，业务子类只需固定泛型。</li>
 *   <li>过滤器本可由业务自行定义为 {@code @Bean}；本类只解决"业务没写时认证链路也能跑通"，业务一旦自建同类型 bean
 *   即被 {@code @ConditionalOnMissingBean} 跳过（业务实现优先）。</li>
 * </ul>
 * <p><b>为什么再包一层 {@link #isAuthAnonymous}</b></p>
 * <ul>
 *   <li>{@link CAbstractAuthFilter#isAnonymous} 是过滤器契约上的公开方法，而业务在本类只需回答"会话是否匿名"这一个问题，
 *   故收口为模板方法：业务子类实现 {@link #isAuthAnonymous}，默认过滤器负责把判定接进过滤器。</li>
 *   <li>该方法为 {@code protected} 而非包私有：业务子类与本类<b>不同包</b>，包私有的抽象方法跨包无法被实现
 *   （子类即使声明同签名方法也不构成覆写），会导致子类编译失败——跨包可继承是本类的硬约束（有测试固化）。</li>
 * </ul>
 * <p><b>本类自身不加 {@code @Configuration}（当前为注释状态）</b></p>
 * <ul>
 *   <li>与基类同口径：不带 {@code @Component}/{@code @Configuration} 即不会被组件扫描发现，避免"基类"被当成配置
 *   重复注册；生效方式是由业务子类加 {@code @Configuration}，或显式 {@code @Import} 本类。</li>
 *   <li>业务子类<b>必须</b>加 {@code @Configuration}（或在子类中声明自己的 {@code @Bean} 方法）：继承来的
 *   {@code @Bean} 方法只有在子类自身是配置候选时才会被处理；子类无注解、无 {@code @Bean} 时不会注册任何默认 bean，
 *   且<i>不报错</i>（静默无 bean）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr><th>场景</th><th>兜底行为</th></tr>
 *   <tr><td>业务未提供 {@link CAbstractAuthFilter} bean</td>
 *   <td>注册默认过滤器：匿名判定委派给子类的 {@link #isAuthAnonymous}</td></tr>
 *   <tr><td>业务已提供同类型 bean</td>
 *   <td>{@code @ConditionalOnMissingBean} 跳过默认实现，不覆盖业务过滤器</td></tr>
 *   <tr><td>业务子类未实现 {@link #isAuthAnonymous}</td>
 *   <td>编译期报错（抽象方法），不会在运行期静默按"非匿名"处理</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>引入 Spring Security 的业务装配：继承本类 + 加 {@code @Configuration} + 实现 {@link #isAuthAnonymous}，
 *   即同时获得"mock 会话配置落点"与"认证过滤器"。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不引入 Spring Security 的场景应继承 {@link CAbstractAuthBaseConfiguration}，不要继承本类
 *   （本类装配的 {@link CAbstractAuthFilter} 需要 Spring Security 在类路径上），二者择一。</li>
 *   <li>需自定义过滤器行为（如解析失败即拒绝、额外埋点）时，应自建 {@link CAbstractAuthFilter} bean，
 *   本类的默认实现会自动让位。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code @ConditionalOnMissingBean} 在本类（非自动配置类）上按注册顺序评估，与基类一致：业务过滤器
 *   <b>先于本类注册</b>时才被感知并跳过默认实现；本类先注册时两个过滤器 bean 并存（按类型注入出现歧义，
 *   需 {@code @Primary} 或按名注入）。自动配置天然最后加载，业务侧显式注册时需自行保证顺序。</li>
 *   <li>默认过滤器只提供"匿名判定"这一个接缝：其余过滤器行为（会话加载、mock 分支等）仍由
 *   {@link CAbstractAuthFilter} 及其基类实现，本类不做二次封装。</li>
 *   <li>{@code @Bean} 方法名遵循项目规范以 {@code c} 前缀命名，bean 名为 {@code cAuthFilter}。</li>
 * </ul>
 *
 * @param <T> 会话类型（Security 相关，下界 {@link ICSecuritySession}）
 *
 * @author c332030
 * @since 2026/9/14
 * @version 1.0
 * @see CAbstractAuthBaseConfiguration
 * @see CAbstractAuthFilter
 */
//@Configuration
public abstract class CAbstractAuthConfiguration<T extends ICSecuritySession> extends CAbstractAuthBaseConfiguration<T> {

    /**
     * 判断会话是否为匿名（未认证）
     *
     * <p>模板方法：业务子类实现本方法，默认过滤器 {@code cAuthFilter} 把判定结果接到
     * {@link CAbstractAuthFilter#isAnonymous} 上。</p>
     *
     * @param session 会话
     * @return true 表示匿名/未认证，将构造匿名认证信息；false 表示已认证
     */
    protected abstract boolean isAuthAnonymous(T session);

    /**
     * 提供默认的认证过滤器 bean（业务已提供同类型 bean 时跳过）
     *
     * <p>返回绑定到本类会话类型 {@code T} 的匿名子类实例，其匿名判定委派给 {@link #isAuthAnonymous}；
     * 子类未实现 {@link #isAuthAnonymous} 时本类无法被继承（编译期报错）。</p>
     *
     * @return 默认认证过滤器
     */
    @Bean
    @ConditionalOnMissingBean(CAbstractAuthFilter.class)
    public CAbstractAuthFilter<T> cAuthFilter() {
        return new CAbstractAuthFilter<T>() {
            @Override
            public boolean isAnonymous(T session) {
                return isAuthAnonymous(session);
            }
        };
    }

}
