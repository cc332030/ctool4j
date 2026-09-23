package com.c332030.ctool4j.spring.security.jackson;

import com.c332030.ctool4j.spring.security.jackson.deserializer.CGrantedAuthorityDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * <p>
 * Description: CSecurityJacksonModule
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSecurityJacksonModule} 为 Spring Security 相关类型的 Jackson 模块：</p>
 * <ul>
 *   <li>为 {@link GrantedAuthority}（接口）与 {@link SimpleGrantedAuthority}（具体实现类）注册同一反序列化器
 *   （{@link CGrantedAuthorityDeserializer}）</li>
 *   <li>由 {@code META-INF/services/com.fasterxml.jackson.databind.Module} 声明为 Jackson SPI，自动装配</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <p><b>存在的原因</b></p>
 * <ul>
 *   <li>{@link GrantedAuthority} 是接口，序列化后 JSON 不含类型信息，反序列化时 Jackson 无法推断实现类，
 *   会抛 {@code InvalidDefinitionException}；会话类含 {@code authorities} 字段并持久化到 Redis 再读回时必然触发。</li>
 * </ul>
 * <p><b>自动发现（替代手工注册）</b></p>
 * <ul>
 *   <li>本模块经 {@code META-INF/services/com.fasterxml.jackson.databind.Module} 声明为 Jackson SPI，
 *   {@code ObjectMapper#findAndRegisterModules()} 会自动装配，无需任何 Spring 启动回调或手工注册代码。</li>
 *   <li>{@code CJacksonUtils} 在构建预置 mapper 时已调用 {@code findAndRegisterModules()}，
 *   故其全部预置 mapper（含由 {@code copy()} 派生的 mapper）均自动具备该能力。</li>
 *   <li>core 模块不反向依赖具体框架，框架相关类型体系的反序列化能力由各框架模块以 SPI 方式自注册。</li>
 * </ul>
 * <p><b>接口与具体实现类同注册</b></p>
 * <ul>
 *   <li>Jackson 按<b>声明类型</b>查找反序列化器：只注册接口时，字段/集合声明为 {@link SimpleGrantedAuthority}
 *   仍会退化为默认推导（该类型无默认构造、且 JSON 属性名 {@code authority} 与构造参数名不匹配），必然失败。</li>
 *   <li>故接口与唯一实现类注册同一反序列化器；常量池只会产出 {@link SimpleGrantedAuthority}，
 *   以声明类型为 {@code GrantedAuthority} 的反序列化器注册到实现类上是类型安全的。</li>
 * </ul>
 * <p><b>与 Spring 官方模块的关系（为何不复用）</b></p>
 * <ul>
 *   <li>spring-security-core 自带 {@code org.springframework.security.jackson2.CoreJackson2Module}（内含
 *   {@code SimpleGrantedAuthorityMixin}），但：① 它不是 Jackson SPI（{@code findAndRegisterModules()} 发现不到，
 *   必须手工注册）；② 它依赖 default typing，序列化结果会带 {@code @class}，与既有数据形态
 *   {@code {"authority":"ROLE_XXX"}} 不兼容（历史数据仍无法读回）；③ default typing 需配合
 *   {@code SecurityJackson2Modules} 白名单使用，安全面更大。故不采用，仅借鉴其「按 authority 字段构造」的语义。</li>
 *   <li>序列化无需自研：Jackson 默认按 {@code getAuthority()} 输出 {@code {"authority":"ROLE_XXX"}}，
 *   与本模块及线上数据的形态一致（有测试固化）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无（SPI 声明缺失时该能力即不存在，不静默降级）</p>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>本模块（ctool4j-spring-security）自身提供的 Jackson 反序列化能力集合。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不承载序列化能力：{@link GrantedAuthority} 序列化仍由 Jackson 默认机制处理。</li>
 *   <li>不启用多态类型信息（default typing）：JSON 不写 {@code @class}，自定义权限实现类无法还原。</li>
 *   <li>只覆盖经 {@code findAndRegisterModules} 装配的 mapper（{@code CJacksonUtils} 的预置 mapper 及其
 *   {@code configure} 过的 mapper）；Spring 容器内的 {@code ObjectMapper} 默认不覆盖——Spring Boot 只安装
 *   容器内的 {@code Module} Bean、不开启 SPI 模块发现，如需覆盖由使用方自行声明 {@code Module} Bean。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 SPI 配置文件被正确打包（resources 随 classpath 输出）；若外部打包工具裁剪或未合并
 *   {@code META-INF/services}（如 shade 未使用 {@code ServicesResourceTransformer}），自动发现会失效。</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
public class CSecurityJacksonModule extends SimpleModule {

    /**
     * 注册 Spring Security 相关类型的序列化/反序列化能力
     */
    @SuppressWarnings("unchecked")
    public CSecurityJacksonModule() {

        // Jackson 按「声明类型」精确查找反序列化器（SimpleDeserializers 以 Class 为键），不会从已注册的接口/父类型向下适配到子类：
        // 只注册接口时，字段/集合元素声明为 SimpleGrantedAuthority 会因认不到可用 creator 而失败（MismatchedInputException）。
        // 故接口与唯一实现类注册同一反序列化器实例：两条路径都进常量池，「同值唯一」不因声明类型而失效。
        addDeserializer(GrantedAuthority.class, CGrantedAuthorityDeserializer.INSTANCE);

        // 泛型为何要绕一层：addDeserializer(Class<T>, JsonDeserializer<? extends T>) 要求两端是同一个 T，
        // 而这里反序列化器的泛型是父接口、声明类型是子类；Class 又是 final 类（Class<A> 与 Class<B> 被编译器
        // 判定为 provably distinct），单层强转 (Class<GrantedAuthority>) SimpleGrantedAuthority.class
        // 会直接编译报错「不兼容的类型」。故先加宽为 Class<?>（合法转换），再窄化为 Class<GrantedAuthority>
        // （未检查转换，由 @SuppressWarnings 抑制）。运行时泛型已擦除、Class 对象不变（仍是 SimpleGrantedAuthority.class），
        // 仅借用父类型的泛型视角，无类型风险（常量池只产出 SimpleGrantedAuthority）
        addDeserializer(
            (Class<GrantedAuthority>) (Class<?>) SimpleGrantedAuthority.class,
            CGrantedAuthorityDeserializer.INSTANCE
        );
    }

}
