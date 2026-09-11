package com.c332030.ctool4j.session.interfaces;

import com.c332030.ctool4j.definition.interfaces.ICCreateMillis;

/**
 * <p>
 * Description: ICSession
 * </p>
 *
 * <p>会话标记接口（不依赖 Spring Security），用于 base 层表达"会话"概念。
 * 会话本身不持有认证凭据（credentials）；与 Spring Security 相关的权限能力见子接口
 * {@code ICSecuritySession}（位于 ctool4j-auth-spring）。</p>
 *
 * <p>会话创建时间（毫秒时间戳）能力继承自 {@link ICCreateMillis}。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
public interface ICSession extends ICCreateMillis {

}
