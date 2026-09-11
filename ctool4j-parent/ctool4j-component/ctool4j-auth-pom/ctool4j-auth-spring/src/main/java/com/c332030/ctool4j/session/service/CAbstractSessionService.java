package com.c332030.ctool4j.session.service;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.auth.util.CTokenUtils;
import com.c332030.ctool4j.core.interfaces.IGenericType;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.session.config.CSessionConfig;
import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;

/**
 * <p>
 * Description: CAbstractSessionService
 * </p>
 *
 * <p>会话服务抽象基类，基于 Redis 提供会话的存取/删除与当前会话获取；通过 {@link IGenericType} 解析子类指定的
 * 会话类型 {@code SESSION}（子类须以具体类型直接继承，否则泛型解析可能失败）。</p>
 *
 * <p>说明：{@code sessionClass} 为实例初始化字段（{@code final}），不参与 {@code @AllArgsConstructor} 构造参数；
 * {@code get()}/{@code check()} 依赖 Spring Security 上下文，仅请求线程可用。</p>
 *
 * @author c332030
 * @since 2026/9/10
 */
@CustomLog
@AllArgsConstructor
public abstract class CAbstractSessionService<SESSION extends ICSecuritySession> implements IGenericType<SESSION> {

    CSessionConfig sessionConfig;

    CStringStringRedisService redisService;

    /**
     * 泛型 SESSION 的运行时 Class，由子类 {@code extends CAbstractSessionService<Xxx>} 的泛型实参解析而来
     */
    final Class<SESSION> sessionClass = getGenericClass();

    public String getKey(String token) {
        return CRedisUtils.getKey(sessionClass, token);
    }

    public SESSION get(String token) {
        return redisService.getValue(getKey(token), sessionClass);
    }

    public void save(String token, SESSION session) {

        log.info("save session, token: {}, session: {}", token, session);
        redisService.setValue(getKey(token), session, sessionConfig.getExpire());

    }

    public void remove(String token) {

        log.info("remove session, token: {}", token);
        redisService.delete(getKey(token));

    }

    public SESSION getSessionByJwt(String jwt) {

        val token = CTokenUtils.getTokenByJwt(jwt);
        if(StrUtil.isBlank(token)) {
            return null;
        }

        return get(token);
    }

    public void check() {
        get();
    }

    public SESSION getDefaultNull() {
        return CSpringSecurityUtils.getPrincipal();
    }

    public SESSION get() {
        val session = getDefaultNull();
        CAssert.notNull(session, "未授权");
        return session;
    }

}
