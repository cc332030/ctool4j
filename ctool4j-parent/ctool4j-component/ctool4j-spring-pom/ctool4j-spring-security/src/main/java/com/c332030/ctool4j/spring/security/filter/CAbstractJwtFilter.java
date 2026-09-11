package com.c332030.ctool4j.spring.security.filter;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * <p>
 * Description: CAbstractJwtFilter jwt 过滤器
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAbstractJwtFilter}：JWT 过滤器基类。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>JWT 认证过滤器，解析令牌并设置安全上下文</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无令牌/无效时未认证</p>
 * <h2>适用范围</h2>
 * <p>JWT 认证</p>
 * <h2>不适用与边界场景</h2>
 * <p>抽象基类</p>
 * <h2>已知限制与取舍</h2>
 * <p>抽象基类</p>
 *
 * @since 2026/3/19
 * @version 1.0
 */
public abstract class CAbstractJwtFilter extends OncePerRequestFilter {

}
