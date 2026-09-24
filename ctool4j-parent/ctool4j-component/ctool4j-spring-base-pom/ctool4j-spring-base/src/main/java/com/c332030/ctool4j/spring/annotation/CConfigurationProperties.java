package com.c332030.ctool4j.spring.annotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CConfigurationProperties
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CConfigurationProperties}：配置属性绑定注解，{@code @ConfigurationProperties} 的组合注解。
 * 注解只承载一项能力——前缀（{@code value}，透传给 {@code @ConfigurationProperties} 的 {@code prefix}）；
 * 两个绑定开关都在元注解上给定：{@code ignoreUnknownFields} 沿用元注解默认值 {@code true}，
 * {@code ignoreInvalidFields} 由本注解的元注解显式钉为 {@code true}。
 * 故默认行为是<b>既忽略配置里多出的未知键、也忽略取值类型不匹配的属性，两者都不报错、不中断启动</b>。
 * 属性 Bean 的注册由 ctool4j-spring 的
 * {@code @ConfigurationPropertiesScan}（扫描 {@code com.c332030.ctool4j}，{@code @CSpringBootApplication} 亦已开启）
 * 承担，故属性类只需声明本注解、无需再声明 {@code @EnableConfigurationProperties}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只承载前缀，开关不在本注解上复述</b>：两个开关的默认行为都已在元注解上给定
 *   （{@code ignoreUnknownFields} 取元注解默认的 {@code true}，{@code ignoreInvalidFields} 由元注解使用点写成 {@code true}），
 *   在本注解上再声明一遍属性元素不改变行为、只多出一处要同步维护的说明，故本注解<b>只有一个属性元素</b> {@code value}。</li>
 *   <li><b>{@code ignoreInvalidFields = true} 必须落在元注解的使用点上</b>：元注解同名属性默认是 {@code false}，
 *   「默认忽略非法值」只能在那里改到 {@code true}；{@code @AliasFor} 只用于本注解<b>自身的</b>属性元素，
 *   标注不到元注解的使用点，故该值就是字面量 {@code true}、不能由使用点改写。</li>
 *   <li><b>别名只指前缀</b>：{@code value} 经 {@code @AliasFor} 透传到元注解的 {@code prefix}。
 *   元注解的 {@code value}/{@code prefix} 默认同为 {@code ""}，互为别名成立。</li>
 *   <li><b>无逻辑</b>：纯声明式组合注解，属性只做透传，不含任何条件判断与取值加工（项目规范：配置类不写逻辑）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>配置里出现属性类未声明的键</td>
 *     <td>忽略该键、不报错、不中断启动（沿用元注解 {@code ignoreUnknownFields} 的默认 {@code true}）</td>
 *   </tr>
 *   <tr>
 *     <td>配置值与属性类型不匹配</td>
 *     <td>忽略该属性、不中断启动（元注解使用点上的 {@code ignoreInvalidFields = true}）</td>
 *   </tr>
 *   <tr>
 *     <td>未指定前缀</td>
 *     <td>由使用点显式声明前缀（{@code value}）为准</td>
 *   </tr>
 *   <tr>
 *     <td>属性类未声明任何绑定注解</td>
 *     <td>本注解不参与，{@code @ConfigurationPropertiesScan} 不注册该类型</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>ctool4j 与引用本库的 Spring Boot 应用的配置属性类（{@code @ConfigurationProperties} 的替代写法）。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要「非法值即启动失败」的场景：本注解<b>表达不了</b>该开关（它只有一个属性元素），属性类改用元注解
 *   {@code @ConfigurationProperties(prefix = "order", ignoreInvalidFields = false)}。</li>
 *   <li>需要「未知键即启动失败」的场景：同上，属性类改用元注解
 *   {@code @ConfigurationProperties(prefix = "order", ignoreUnknownFields = false)}。</li>
 *   <li>非 Spring 环境不适用；缺少属性绑定基础设施时该注解不产生效果。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认忽略意味着<b>配置键拼写错误（静默取默认值）与配置值类型写错（该属性被跳过）都不会在启动期暴露</b>；
 *   需要暴露时改用元注解按上节逐项关闭。</li>
 *   <li>只透传前缀：Spring Boot 侧为 {@code @ConfigurationProperties} 新增的其余属性
 *   不会出现在本注解上，需要时直接使用元注解。</li>
 *   <li>只可标注类型：元注解 {@code @ConfigurationProperties} 可标注类型与方法（{@code @Target({TYPE, METHOD})}），
 *   本注解<b>只保留类型</b>，方法级绑定不支持，需要时直接使用元注解。</li>
 * </ul>
 *
 * @since 2026/9/21
 * @version 1.0
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@ConfigurationProperties(ignoreInvalidFields = true)
public @interface CConfigurationProperties {

    /**
     * 配置前缀（透传给 {@code @ConfigurationProperties} 的 {@code prefix}，与 {@code @ConfigurationProperties#value} 等价）
     *
     * @return 配置前缀；未声明时为空串
     */
    @AliasFor(annotation = ConfigurationProperties.class, attribute = "prefix")
    String value() default "";

}
