package com.c332030.ctool4j.session.service;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.auth.util.CAuthUtils;
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
import org.springframework.lang.NonNull;

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

    /**
     * 泛型 SESSION 的运行时 Class，由子类 {@code extends CAbstractSessionService<Xxx>} 的泛型实参解析而来
     */
    final Class<SESSION> sessionClass = getGenericClass();

    CSessionConfig sessionConfig;

    CStringStringRedisService redisService;

    /**
     * 生成会话在 Redis 中的 key（基于会话类型 {@code sessionClass} 与 token）
     *
     * @param token token
     * @return Redis key
     */
    private String getKey(String token) {
        return CRedisUtils.getKey(sessionClass, token);
    }

    /**
     * 按 token 读取会话
     *
     * @param token token
     * @return 会话；不存在返回 null
     */
    public SESSION get(@NonNull String token) {
        return redisService.getValue(getKey(token), sessionClass);
    }

    /**
     * 按 token 写入会话（过期时间取配置 {@link CSessionConfig#expire}）
     *
     * @param token   token
     * @param session 会话
     */
    public void save(@NonNull String token, SESSION session) {

        log.info("save session, token: {}, session: {}", token, session);
        redisService.setValue(getKey(token), session, sessionConfig.getExpire());

    }

    /**
     * 按 token 删除会话
     *
     * @param token token
     */
    public void remove(@NonNull String token) {

        log.info("remove session, token: {}", token);
        redisService.delete(getKey(token));

    }

    /**
     * 由 jwt 解析 token 后读取会话
     *
     * @param jwt jwt
     * @return 会话；token 为空或会话不存在返回 null
     */
    public SESSION getSessionByJwt(String jwt) {

        val token = CAuthUtils.getTokenByJwt(jwt);
        if(StrUtil.isBlank(token)) {
            return null;
        }

        return get(token);
    }

    /**
     * 校验当前已授权
     *
     * @throws IllegalArgumentException 未授权
     */
    public void check() {
        get();
    }

    /**
     * 获取当前会话（未授权返回 null，不抛异常）
     *
     * @return 当前会话；未授权返回 null
     */
    public SESSION getDefaultNull() {
        return CSpringSecurityUtils.getPrincipal();
    }

    /**
     * 获取当前会话
     *
     * @return 当前会话
     * @throws IllegalArgumentException 未授权
     */
    public SESSION get() {
        val session = getDefaultNull();
        CAssert.notNull(session, "未授权");
        return session;
    }

}
