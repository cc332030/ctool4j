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
 * <p>认证工具类，承载依赖配置的 jwt 能力（密钥取自 {@code CAuthConfig#jwtSecret}）。</p>
 *
 *
 * <h2>设计思路总述</h2>
 * <ul>
 *   <li>本类只负责「读取配置 + 组装业务语义」：纯编解码能力（创建/校验/解析）由 ctool4j-web 的
 *   {@code CJwtUtils} 提供，token 前缀与请求头读写由 {@code CTokenUtils} 提供。</li>
 *   <li>失败策略按方向区分：<b>解析方向静默失败</b>（任意来源的 jwt 解析失败不构成业务错误，
 *   统一吞掉异常返回 null，由调用方按「无 token」处理）；<b>签发方向快速失败</b>
 *   （本系统主动签发，配置缺失属编码/配置错误，异常直接向上抛）。</li>
 * </ul>
 * <p>详细设计、详细步骤、兜底与已知限制见各方法 javadoc。</p>
 *
 * @since 2026/9/11
 * @version 1.0
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
     * <p><b>详细设计</b>：密钥取自配置 {@code CAuthConfig#jwtSecret}；载荷中的 token 字段名为
     * {@link com.c332030.ctool4j.definition.interfaces.ICToken#TOKEN}。本方法服务于接口权限控制，
     * 传入的可能是任意来源的 jwt（非本系统签发、被篡改、已过期），因此<b>静默失败为刻意设计</b>：
     * 解析失败不构成业务错误，返回 null 交调用方按「无 token」处理，请求仍为未授权状态、
     * 后续照常走鉴权，不因外部系统凭据导致接口报错。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>经 {@link CTokenUtils#removePrefix(String)} 去除 {@code Bearer } 等前缀；</li>
     *   <li>去前缀后为空/空白 → 记 debug 日志并返回 null；</li>
     *   <li>以配置密钥校验（{@code CJwtUtils.verify}）：校验阶段的任何异常
     *   （authConfig 未注入的 NPE、密钥未配置的 IllegalArgumentException、格式非法的 JWTException）
     *   统一捕获、记 debug 日志并返回 null；</li>
     *   <li>校验通过后解析载荷（{@code CJwtUtils.parseBody}）并取 {@link ICToken#TOKEN} 字段值，
     *   经 {@code StrUtil.toStringOrNull} 转字符串返回；该段同样捕获异常并返回 null；</li>
     *   <li>最终返回 token 或 null。</li>
     * </ol>
     *
     * <p><b>边界与已知限制</b>：不区分失败原因（空白/非本系统 jwt/被篡改/配置未注入/载荷无 token 字段）
     * 一律返回 null，排障需依赖 debug 日志；配置未注入时静默返回 null，可能掩盖配置缺失问题。</p>
     * <ul>
     *   <li>{@link #getTokenByJwt(String)}：校验 jwt 并解析其中携带的 token（静默失败）。</li>
     * </ul>
     *
     * @param jwt jwt（先去除前缀）
     * @return jwt 载荷中的 token；jwt 为空、校验失败或载荷无 token 字段时返回 null
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
     * <p><b>详细设计</b>：属本系统主动签发路径，配置缺失或密钥空白属编码/配置错误，
     * 因此<b>快速失败</b>（异常向上抛）而非静默兜底，与
     * {@link #getTokenByJwt(String)} 的静默失败策略相反。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>取配置密钥 {@code CAuthConfig#jwtSecret}（authConfig 未注入时在此抛 NPE）；</li>
     *   <li>以 {@code jwtInfo} 为载荷生成 jwt（{@link CJwtUtils#create(Object, String)}）；
     *   密钥空白（未配置）时由 {@code CJwtUtils.create} 抛 {@link IllegalArgumentException}；</li>
     *   <li>经 {@code CTokenUtils.setHeaderToken} 写入当前响应的 Authorization 头
     *   （依赖请求上下文，非请求线程调用会因 {@code CRequestUtils.getResponse()} 抛
     *   {@link IllegalArgumentException}）。</li>
     * </ol>
     * <ul>
     *   <li>{@link #setJwt(ICJwtInfo)}：为 jwt body 生成 jwt 并写入响应 Authorization 头（快速失败）。</li>
     * </ul>
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
