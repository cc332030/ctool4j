package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.session.config.CAbstractSessionMockConfig;
import com.c332030.ctool4j.session.interfaces.ICSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * Description: CAbstractAuthBaseConfiguration
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAbstractAuthBaseConfiguration} 为认证模块的装配基类，向业务模块提供默认的 mock 会话配置 bean：</p>
 * <ul>
 *   <li>{@code cSessionMockConfig()}：业务未自建 {@link CAbstractSessionMockConfig} 时，提供一个默认（禁用态）实现</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <p><b>为什么是泛型基类 + 匿名子类</b></p>
 * <ul>
 *   <li>本类不确定会话类型，由业务子类继承时固定（{@code CAbstractAuthBaseConfiguration<UserSession>}）；
 *   基类的 {@code @Bean} 方法再以匿名子类 {@code new CAbstractSessionMockConfig<T>() {}} 把该类型绑定到 bean 上。</li>
 *   <li>Spring 会处理配置类<b>声明及继承</b>的 {@code @Bean} 方法，故业务子类加 {@code @Configuration} 即可获得默认装配，
 *   无需重复定义方法体。</li>
 * </ul>
 * <p><b>本类自身不加 {@code @Configuration}（当前为注释状态）</b></p>
 * <ul>
 *   <li>本类不带 {@code @Component}/{@code @Configuration}，不会被组件扫描发现——避免"基类"被当成配置重复注册；
 *   生效方式是由业务子类加 {@code @Configuration}，或显式 {@code @Import} 本类。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr><th>场景</th><th>兜底行为</th></tr>
 *   <tr><td>业务未提供 {@link CAbstractSessionMockConfig} bean</td>
 *   <td>注册默认实现：{@code enable=false}、{@code session=null}，即不注入 mock 会话</td></tr>
 *   <tr><td>业务已提供该类型 bean</td>
 *   <td>{@code @ConditionalOnMissingBean} 跳过默认实现，不覆盖业务配置</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>认证模块的默认装配：为业务提供"免登录 mock 会话配置"的落点，业务侧继承并加 {@code @Configuration} 即可。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不承载业务装配：认证过滤器、会话服务等由各子模块（如 auth-spring）自行装配。</li>
 *   <li>默认 bean 为"关闭态"：仅提供配置落点，不启用 mock 会话；启用仍需业务显式设置
 *   {@code enable=true} 与 {@code session}。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code @ConditionalOnMissingBean} 在<b>非自动配置类</b>上按注册顺序评估（条件在 bean 定义注册阶段判断）：
 *   业务配置<b>先于本类注册</b>时才被感知，默认实现被跳过；若本类先注册，则默认实现与业务实现<b>同时存在</b>，
 *   按类型注入出现歧义（需 {@code @Primary} 或按名注入）。自动配置天然最后加载，业务侧显式注册时需自行保证顺序
 *   （两条顺序均有测试固化）。</li>
 *   <li>按类型判断且泛型擦除：应用中已有<b>任意</b> {@link CAbstractSessionMockConfig} bean 即视为"已提供"，
 *   不支持多个会话类型各自保留默认配置。</li>
 *   <li>{@code @Bean} 方法名遵循项目规范以 {@code c} 前缀命名，bean 名为 {@code cSessionMockConfig}。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/14
 * @version 1.0
 */
//@Configuration
public abstract class CAbstractAuthBaseConfiguration<T extends ICSession> {

    /**
     * 提供默认的 mock 会话配置 bean（业务已提供同类型 bean 时跳过）
     *
     * <p>返回的是绑定到本类会话类型 {@code T} 的匿名子类实例，默认关闭（{@code enable=false}、{@code session=null}），
     * 不会对认证流程产生任何影响。</p>
     *
     * @return 默认 mock 会话配置
     */
    @Bean
    @ConditionalOnMissingBean(CAbstractSessionMockConfig.class)
    public CAbstractSessionMockConfig<T> cSessionMockConfig() {
        return new CAbstractSessionMockConfig<T>() {};
    }

}
