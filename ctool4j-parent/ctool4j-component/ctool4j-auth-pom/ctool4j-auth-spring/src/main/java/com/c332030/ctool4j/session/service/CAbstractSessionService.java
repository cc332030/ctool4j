package com.c332030.ctool4j.session.service;

import com.c332030.ctool4j.session.interfaces.ICSession;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;

/**
 * <p>
 * Description: CAbstractSessionService
 * </p>
 *
 * <p>Spring Security 会话服务抽象基类：继承 auth-base 的 {@link CAbstractBaseSessionService}（会话存取/删除、
 * 按 jwt 取会话、按请求加载会话），只需把「当前会话」的来源绑定到安全上下文——
 * {@link CSpringSecurityUtils#getPrincipal()}。</p>
 *
 * <p>说明：{@code get()}/{@code check()} 依赖 Spring Security 上下文，仅请求线程可用。</p>
 *
 * @author c332030
 * @since 2026/9/10
 * @version 1.0
 */
@CustomLog
public abstract class CAbstractSessionService<SESSION extends ICSession> extends CAbstractBaseSessionService<SESSION> {

    /**
     * 获取当前会话（未授权返回 null，不抛异常）
     *
     * @return 当前会话；未授权返回 null
     */
    @Override
    protected SESSION getDefaultNull() {
        return CSpringSecurityUtils.getPrincipal();
    }

}
