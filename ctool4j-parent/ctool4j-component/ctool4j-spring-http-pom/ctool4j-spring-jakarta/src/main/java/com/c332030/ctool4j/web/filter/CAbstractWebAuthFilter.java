package com.c332030.ctool4j.web.filter;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * <p>
 * Description: CAbstractWebAuthFilter
 * </p>
 *
 * <p>认证过滤器继承链的最顶层基类：抽象类，不含任何逻辑，仅承载"认证过滤器"的类型契约。位于最底层的 ctool4j-web，
 * 使不依赖 Spring Security 的 ctool4j-auth-base 与需要按该类型注入认证过滤器的 ctool4j-spring-security
 * 都能看到同一类型。</p>
 *
 * <p>继承链自下而上：本类（web：类型契约）→
 * {@code CAbstractBaseAuthFilter}（auth-base：会话加载与过滤器骨架等非 Security 公共逻辑）→
 * {@code CAbstractAuthFilter}（auth-spring：Security 认证信息构造，业务直接继承）。</p>
 *
 * <p>本类由 ctool4j-spring-security 的 {@code CAbstractJwtFilter} 改名并下沉到 web：安全过滤器链装配时需按
 * "认证过滤器"这一确定类型注入，若直接按 {@link OncePerRequestFilter} 注入，会与 Spring Boot 内置的同类型 bean
 * （字符编码过滤器、请求体表单内容过滤器等）产生注入歧义。</p>
 *
 * <h2>能力目录</h2>
 * <p>无方法，仅类型契约。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>只提供类型契约，不实现任何过滤逻辑；能力由 auth-base / auth-spring 的继承者提供。</li>
 *   <li>继承 Spring 的 {@link OncePerRequestFilter}（过滤器链装配依赖其 doFilter / doFilterInternal 模板），
 *   故不实现项目内的 {@link ICFilter}。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>ctool4j 认证过滤器继承链（auth-base、auth-spring）的顶层基类。</li>
 *   <li>安全过滤器链装配时，认证过滤器的注入类型契约。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>非请求认证用途的过滤器不应继承本类，否则会命中认证过滤器的注入契约。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本类不含逻辑，仅用于统一继承链与类型契约，实际能力由继承者提供。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/3/19
 * @version 1.2
 */
public abstract class CAbstractWebAuthFilter extends OncePerRequestFilter {

}
