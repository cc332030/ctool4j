package com.c332030.ctool4j.session.service;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.auth.util.CAuthUtils;
import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.core.interfaces.IGenericType;
import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.session.config.CSessionConfig;
import com.c332030.ctool4j.session.interfaces.ICSession;
import com.c332030.ctool4j.web.util.CTokenUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CAbstractBaseSessionService
 * </p>
 *
 * <p>会话服务抽象基类，基于 Redis 提供会话的存取/删除与当前会话获取；通过 {@link IGenericType} 解析子类指定的
 * 会话类型 {@code SESSION}（子类须以具体类型直接继承，否则泛型解析可能失败）。</p>
 *
 * <p>说明：{@code sessionClass} 为实例初始化字段（{@code final}），不参与 {@code @AllArgsConstructor} 构造参数；
 * {@code get()}/{@code check()} 的当前会话来源由子类实现的 {@link #getDefaultNull()} 决定（Security 场景见
 * auth-spring 的子类），仅当前请求线程可用。</p>
 *
 * <p>继承约束：{@link #getDefaultNull()} 为 {@code public}，子类可在<b>任意包</b>直接继承本类并实现该钩子
 * （无需与本类同包）。</p>
 *
 * <p>相关测试（{@code com.c332030.ctool4j.session.service}）：{@code CAbstractBaseSessionServiceTests}。
 * 未以 {@code @see} 链接测试类：javadoc 的类路径不含测试源，{@code @see} 会报 "reference not found"
 * 并使 javadoc 退出码非 0，在 {@code failOnError=true} 下中断构建。</p>
 *
 * @author c332030
 * @since 2026/9/10
 * @version 1.3
 */
@CustomLog
public abstract class CAbstractBaseSessionService<SESSION extends ICSession> implements IGenericType<SESSION> {

    /**
     * 泛型 SESSION 的运行时 Class，由子类 {@code extends CAbstractBaseSessionService<Xxx>} 的泛型实参解析而来
     */
    final Class<SESSION> sessionClass = getGenericClass();

    @Autowired
    CSessionConfig sessionConfig;

    @Autowired
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

        log.info("save session, token: {}, session: {}, expire: {}",
            token, session, sessionConfig.getExpire()
        );
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
     * 从当前请求解出 token 并加载会话
     *
     * <p>流程：取请求 Authorization 头 token → 经 {@link CAuthUtils#getTokenByJwt} 校验并解析出业务 token
     * → 按 token 查会话 → 命中则把 token 写入请求属性（{@link CTokenUtils#setToken(HttpServletRequest, String)}）。</p>
     *
     * <p>可能收到其他系统误传的 token：解析失败（内部静默返回 null）或查不到会话时直接返回 null，
     * 不写入请求属性，由调用方决定后续处理（如保持未认证状态）。</p>
     *
     * @param request 当前请求
     * @return 会话；token 缺失/解析失败/会话不存在返回 null
     */
    public SESSION loadSession(HttpServletRequest request) {

        val jwt = CTokenUtils.getHeaderToken(request);
        val token = CAuthUtils.getTokenByJwt(jwt);
        if(CValidUtils.isNotValid(token)) {
            log.debug("no token");
            return null;
        }

        val session = get(token);
        if(session == null) {
            log.debug("can't find session, token: {}", token);
            return null;
        }

        CTokenUtils.setToken(request, token);
        return session;
    }

    /**
     * 校验当前已授权
     *
     * @throws CUnauthorizedException 未授权（由 {@link #get()} 抛出）
     */
    public void check() {
        get();
    }

    /**
     * 获取当前会话（未授权返回 null，不抛异常）
     *
     * <p>子类扩展点：由子类实现「当前会话从哪里取」（如 Spring Security 安全上下文）；{@code public} 使子类
     * 可在任意包直接继承本类，无需与本类同包，外部也可按需直接取「未授权时为 null」的会话。</p>
     *
     * @return 当前会话；未授权返回 null
     */
    public abstract SESSION getDefaultNull();

    /**
     * 获取当前会话
     *
     * <p>唯一一处「当前会话是否缺失」的判定落点：{@link #check()} 委托本方法，故两处的未授权语义与异常类型
     * 始终一致；需要自定义未授权响应时在抛出处携带信息，不在调用方另行判定。</p>
     *
     * @return 当前会话
     * @throws CUnauthorizedException 未授权（{@link #getDefaultNull()} 返回 null，当前请求无有效会话）
     */
    public SESSION get() {
        val session = getDefaultNull();
        if (null == session) {
            throw new CUnauthorizedException("未授权");
        }
        return session;
    }

}
