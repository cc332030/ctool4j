package com.c332030.ctool4j.session.interfaces;

/**
 * <p>
 * Description: ICSession
 * </p>
 *
 * <p>会话标记接口（不依赖 Spring Security），用于 base 层表达"会话"概念。
 * 会话本身不持有认证凭据（credentials）；与 Spring Security 相关的权限能力见子接口
 * {@code ICSecuritySession}（位于 ctool4j-auth-spring）。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
public interface ICSession {

}
