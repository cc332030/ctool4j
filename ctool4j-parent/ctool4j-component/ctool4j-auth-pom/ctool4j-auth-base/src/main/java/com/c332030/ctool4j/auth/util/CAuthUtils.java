package com.c332030.ctool4j.auth.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.auth.config.CAuthConfig;
import com.c332030.ctool4j.auth.interfaces.ICJwtInfo;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.definition.interfaces.ICToken;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.web.util.CJwtUtils;
import com.c332030.ctool4j.web.util.CTokenUtils;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CAuthUtils
 * </p>
 *
 * <p>认证工具类，承载依赖配置的 jwt 能力（密钥取自 {@code CAuthConfig#jwtSecret}）：</p>
 * <ul>
 *   <li>{@link #getTokenByJwt(String)}：校验 jwt 并解析其中携带的 token；</li>
 *   <li>{@link #setJwt(ICJwtInfo)}：为 jwt body 生成 jwt 并写入响应 Authorization 头。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>{@link #getTokenByJwt(String)}：jwt 先经 {@code CTokenUtils.removePrefix} 去除前缀；空白返回 null；
 *   以配置密钥校验，校验异常记 debug 日志并返回 null（不向上抛）；校验通过后解析载荷，取字段
 *   {@link ICToken#TOKEN} 对应值。
 *   <p><b>静默失败为刻意设计</b>：本方法服务于接口权限控制，传入的可能是任意来源的 jwt
 *   （非本系统签发、被篡改、已过期等）。解析失败不构成业务错误，返回 null 交由调用方按「无 token」处理即可，
 *   因此统一吞掉异常不向上抛，避免非本系统凭据导致接口报错。</p></li>
 *   <li>{@link #setJwt(ICJwtInfo)}：以配置密钥与 {@code jwtInfo} 为载荷生成 jwt（{@code CJwtUtils.create}），
 *   再经 {@code CTokenUtils.setHeaderToken} 写入当前响应头。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr><th>场景</th><th>兜底行为</th></tr>
 *   <tr><td>getTokenByJwt jwt 空白</td><td>返回 null</td></tr>
 *   <tr><td>getTokenByJwt 校验失败</td><td>记 debug 日志，返回 null</td></tr>
 *   <tr><td>getTokenByJwt authConfig 未注入或密钥未配置</td><td>NPE/IllegalArgumentException 被捕获，记 debug 日志，返回 null</td></tr>
 *   <tr><td>setJwt 密钥未配置</td><td>{@code CJwtUtils.create} 抛 IllegalArgumentException（不捕获，向上抛出）</td></tr>
 *   <tr><td>setJwt 非请求上下文</td><td>{@code CRequestUtils.getResponse()} 抛 IllegalArgumentException</td></tr>
 *   <tr><td>setJwt authConfig 未注入</td><td>抛 NPE（不捕获，向上抛出）</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>适用：需要以配置密钥校验 jwt 并取出业务 token；需要签发 jwt 并写入响应头。</li>
 *   <li>不适用：token 前缀处理、请求头/响应头 token 读写、请求属性 token 读写等不依赖配置的能力见
 *   ctool4j-web 的 {@code CTokenUtils}。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@link #getTokenByJwt(String)} 静默失败有意为之：不区分失败原因（空白/非本系统 jwt/被篡改/配置未注入）
 *   一律返回 null，调用方按「无 token」处理；排障需依赖 debug 日志。</li>
 *   <li>依赖 {@code CAuthConfig} 注入；{@link #setJwt(ICJwtInfo)} 属主动签发，失败直接抛出不做兜底。</li>
 * </ul>
 *
 * <p>注意：{@link #authConfig} 未注入时，{@link #getTokenByJwt(String)} 的 NPE 会被内部捕获并静默返回 null（便于排障需关注 debug 日志），
 * {@link #setJwt(ICJwtInfo)} 则直接抛 NPE；
 * {@link #setJwt(ICJwtInfo)} 写入当前响应依赖请求上下文，非请求线程调用会抛 {@link IllegalArgumentException}。</p>
 *
 * @since 2026/9/11
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CAuthUtils {

    @Setter
    @CAutowired
    CAuthConfig authConfig;

    /**
     * 校验 jwt 并解析其中携带的 token
     *
     * <p>密钥取自配置 {@code CAuthConfig#jwtSecret}；jwt 先经 {@link CTokenUtils#removePrefix(String)}
     * 去除前缀，载荷中的 token 字段名为 {@link CTokenUtils#TOKEN}。</p>
     *
     * <p>校验阶段的异常（含 authConfig 未注入的 NPE、密钥未配置的 IllegalArgumentException、格式非法的
     * JWTException）统一被捕获并返回 null；但校验通过后的载荷解析不在 try 内，解析异常会向上抛出。</p>
     *
     * <p><b>静默失败为刻意设计</b>：本方法用于接口权限控制，传入的可能是非本系统签发的 jwt，
     * 解析失败不属业务错误，返回 null 由调用方按「无 token」处理，不应因此导致接口报错。</p>
     *
     * @param jwt jwt（先去除前缀）
     * @return jwt 载荷中的 token；jwt 为空或校验失败返回 null
     */
    public String getTokenByJwt(String jwt) {

        jwt = CTokenUtils.removePrefix(jwt);
        if(StrUtil.isBlank(jwt)) {
            log.debug("can't find jwt");
            return null;
        }

        // 其他系统的 token 可能误传到本服务（非本系统签发 / 被篡改 / 已过期），校验失败属正常情况而非业务错误：
        // 此处静默处理（debug 日志 + 返回 null），由调用方按「无 token」处理——请求仍为未授权状态、后续照常
        // 走鉴权，不影响接口安全，也不会因外部系统的凭据导致接口报错。
        try {
            CJwtUtils.verify(jwt, authConfig.getJwtSecret());
        } catch (Exception e) {
            log.debug("verify jwt error", e);
            return null;
        }

        try {
            val jwtBody = CJwtUtils.parseBody(jwt, CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
            val token = CMapUtils.get(jwtBody, ICToken.TOKEN);
            return StrUtil.toStringOrNull(token);
        } catch (Exception e) {
            log.debug("parse jwt error", e);
            return null;
        }

    }

    /**
     * 为 jwt 信息对象生成 jwt 并写入当前响应头（{@code Authorization: Bearer {jwt}}）
     *
     * <p>密钥取自配置 {@code CAuthConfig#jwtSecret}；写入当前响应依赖请求上下文，
     * 非请求线程调用会因 {@code CRequestUtils.getResponse()} 抛 {@link IllegalArgumentException}；
     * jwtSecret 未配置时由 {@link CJwtUtils#create} 抛 {@link IllegalArgumentException}。</p>
     *
     * @param jwtInfo jwt 信息对象（以其内容为载荷）
     * @param <S>     jwt 信息类型
     * @throws IllegalArgumentException secret 为空白或非请求上下文时抛出
     */
    public <S extends ICJwtInfo> void setJwt(S jwtInfo) {
        val jwt = CJwtUtils.create(jwtInfo, authConfig.getJwtSecret());
        CTokenUtils.setHeaderToken(jwt);
    }

}
