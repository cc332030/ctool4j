package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.auth.filter.CAbstractAuthFilter;
import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.session.service.CAbstractSessionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * Description: CAbstractAuthConfiguration
 * </p>
 *
 * <p>认证模块装配基类（auth-spring）：在 auth-base 的 {@link CAbstractAuthBaseConfiguration}（mock 会话配置落点）
 * 之上，追加 Spring Security 认证过滤器的默认装配；业务侧继承本类、（按需）覆写 {@link #isAuthAnonymous}，
 * 即可同时获得二者。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code cAuthFilter()}：业务未自建 {@link CAbstractAuthFilter} 时，提供一个默认（匿名子类）实现，
 *   其匿名判定委派给 {@link #isAuthAnonymous}</li>
 *   <li>{@code cSessionService()}：业务未自建 {@link CAbstractSessionService} 时，提供一个默认（匿名子类）实现；
 *   会话类型从<b>业务配置子类</b>的泛型实参解析（{@code sessionClass()}，配置基类提供的懒解析入口），显式传入服务——
 *   匿名子类自身的类型实参仍是类型变量，若依赖其自身解析会抛 {@code ClassCastException}（TypeVariable 不能强转 Class）</li>
 *   <li>{@link #isAuthAnonymous}：可覆写的判定方法（"会话是否匿名"），<b>不覆写时按默认值（非匿名＝已认证）处理</b></li>
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
 *   故收口为可覆写方法：业务子类覆写 {@link #isAuthAnonymous}，默认过滤器负责把判定接进过滤器。</li>
 *   <li>该方法为 {@code protected} 而非包私有：业务子类与本类<b>不同包</b>，包私有方法跨包不构成覆写——子类即使写了
 *   同签名方法也不会被调用，而是静默走默认实现（契约失效且不报错），故必须以 {@code protected} 暴露给子类
 *   （委派用例可捕捉该回归）。</li>
 *   <li>默认实现返回 {@code false}（非匿名＝已认证），即"有会话即视为已登录"：本类<b>不做</b>兜底拒绝，
 *   是否匿名由业务语义决定（如访客会话应覆写为 {@code true}）。这是显式取舍而非"安全默认"，
 *   漏覆写的后果见「已知限制与取舍」。</li>
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
 *   <tr><td>业务子类未覆写 {@link #isAuthAnonymous}</td>
 *   <td>使用默认实现：按"非匿名"处理（会话被视为已认证），不报错</td></tr>
 *   <tr><td>业务未提供 {@link CAbstractSessionService} bean</td>
 *   <td>注册默认会话服务：匿名子类 {@code new CAbstractSessionService<T>(sessionClass()) {}}（会话类型由业务配置子类的泛型实参解析后显式传入），
 *   会话读写行为由基类提供</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>引入 Spring Security 的业务装配：继承本类 + 加 {@code @Configuration} +（按需）覆写
 *   {@link #isAuthAnonymous}，即同时获得"mock 会话配置落点"与"认证过滤器"。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不引入 Spring Security 的场景应继承 {@link CAbstractAuthBaseConfiguration}，不要继承本类
 *   （本类装配的 {@link CAbstractAuthFilter} 需要 Spring Security 在类路径上），二者择一。</li>
 *   <li>需自定义过滤器行为（如解析失败即拒绝、额外埋点）时，应自建 {@link CAbstractAuthFilter} bean，
 *   本类的默认实现会自动让位。</li>
 *   <li>会话可能是"未登录访客"（如仅持有匿名会话）时<b>必须</b>覆写 {@link #isAuthAnonymous} 返回 {@code true}，
 *   否则访客会话会被当作已认证。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认"非匿名"意味着<b>漏覆写不会报错</b>：会话一旦存在即被构造为已认证
 *   （{@code UsernamePasswordAuthenticationToken}，而非 {@code AnonymousAuthenticationToken}），
 *   {@code authenticated()} 级别的授权规则会放行；此时权限仅为会话默认权限
 *   （{@code ICSecuritySession#getAuthorities()} 默认 {@code ROLE_ANONYMOUS}），按角色/权限的规则仍会拦截。</li>
 *   <li>{@code @ConditionalOnMissingBean} 在本类（非自动配置类）上按注册顺序评估，与基类一致：业务过滤器
 *   <b>先于本类注册</b>时才被感知并跳过默认实现；本类先注册时两个过滤器 bean 并存（按类型注入出现歧义，
 *   需 {@code @Primary} 或按名注入）。自动配置天然最后加载，业务侧显式注册时需自行保证顺序。</li>
 *   <li>默认过滤器只提供"匿名判定"这一个接缝：其余过滤器行为（会话加载、mock 分支等）仍由
 *   {@link CAbstractAuthFilter} 及其基类实现，本类不做二次封装。</li>
 *   <li>{@code @Bean} 方法名遵循项目规范以 {@code c} 前缀命名，bean 名为 {@code cAuthFilter}、{@code cSessionService}。</li>
 * </ul>
 *
 * @param <SESSION> 会话类型（Security 相关，下界 {@link ICSecuritySession}）
 *
 * @author c332030
 * @since 2026/9/14
 * @version 1.3
 * @see CAbstractAuthBaseConfiguration
 * @see CAbstractAuthFilter
 */
//@Configuration
public abstract class CAbstractAuthConfiguration<SESSION extends ICSecuritySession> extends CAbstractAuthBaseConfiguration<SESSION> {

    /**
     * 判断会话是否为匿名（未认证）
     *
     * <p>可覆写方法：默认返回 {@code false}（非匿名＝已认证，即"有会话即视为已登录"）；业务按自身会话语义覆写
     * （如访客会话返回 {@code true}）。默认过滤器 {@code cAuthFilter} 把判定结果接到
     * {@link CAbstractAuthFilter#isAnonymous} 上。</p>
     *
     * @param session 会话
     * @return true 表示匿名/未认证，将构造匿名认证信息；false 表示已认证（默认实现）
     */
    protected boolean isAuthAnonymous(SESSION session) {
        return false;
    }

    /**
     * 提供默认的认证过滤器 bean（业务已提供同类型 bean 时跳过）
     *
     * <p>返回绑定到本类会话类型 {@code T} 的匿名子类实例，其匿名判定委派给 {@link #isAuthAnonymous}。</p>
     *
     * @return 默认认证过滤器
     */
    @Bean
    @ConditionalOnMissingBean(CAbstractAuthFilter.class)
    public CAbstractAuthFilter<SESSION> cAuthFilter() {
        return new CAbstractAuthFilter<SESSION>() {
            @Override
            public boolean isAnonymous(SESSION session) {
                return isAuthAnonymous(session);
            }
        };
    }

    /**
     * 提供默认的会话服务 bean（业务已提供同类型 bean 时跳过）
     *
     * <p>返回绑定到本类会话类型 {@code SESSION} 的匿名子类实例：{@link CAbstractSessionService} 只差泛型绑定，
     * 会话读写与当前会话获取等行为由基类（Redis）提供；业务需要自定义会话来源时自建同类型 bean，
     * 本默认实现由 {@code @ConditionalOnMissingBean} 让位。</p>
     *
     * <p><b>会话类型解析</b>：匿名子类自身的类型实参仍是类型变量 {@code SESSION}，基类若依赖其自身解析会得到
     * {@code TypeVariable} 并在强转时抛 {@code ClassCastException}；故按 {@code sessionClass()}（配置基类的懒解析入口）
     * 从<b>业务配置子类</b>（{@code extends CAbstractAuthConfiguration<XxxSession>}）解析出具体 Class 后经构造显式传入。</p>
     *
     * @return 默认会话服务
     */
    @Bean
    @ConditionalOnMissingBean(CAbstractSessionService.class)
    public CAbstractSessionService<SESSION> cSessionService() {
        return new CAbstractSessionService<SESSION>(sessionClass()) {};
    }

}
