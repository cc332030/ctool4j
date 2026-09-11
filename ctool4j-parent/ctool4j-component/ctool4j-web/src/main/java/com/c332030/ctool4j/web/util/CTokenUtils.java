package com.c332030.ctool4j.auth.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.auth.config.CAuthConfig;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.util.CAuthUtils;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CTokenUtils
 * </p>
 *
 * <p>token 工具类：在请求属性中读写 token（键 {@link #TOKEN}），以及由 jwt 校验并解析 token。</p>
 *
 * <p>注意：{@link #getToken()} 依赖当前请求上下文，非请求线程调用会因 {@code CAssert.notNull(request)} 抛断言异常；
 * 常量 {@link #TOKEN} 同时用作"请求属性名"与"jwt 载荷字段名"两处语义，阅读时需注意区分；
 * {@link #getTokenByJwt(String)} 中 {@link #authConfig} 未注入时抛 NPE。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CTokenUtils {

    /**
     * 请求属性名 / jwt 载荷字段名（两种语义共用）
     */
    public final String TOKEN = "token";

    @Setter
    @CAutowired
    CAuthConfig authConfig;

    /**
     * 将 token 写入当前请求属性
     *
     * @param token token
     */
    public void setToken(String token) {
        setToken(CRequestUtils.getRequest(), token);
    }

    /**
     * 将 token 写入指定请求属性
     *
     * @param request 请求
     * @param token   token
     */
    public void setToken(HttpServletRequest request, String token) {
        request.setAttribute(TOKEN, token);
    }

    /**
     * 读取当前请求属性中的 token
     *
     * @return token；非请求环境抛断言异常
     */
    public String getToken() {

        val request = CRequestUtils.getRequest();
        CAssert.notNull(request, "非请求环境");
        return (String) request.getAttribute(TOKEN);
    }

    /**
     * 读取 token，不存在则生成新 token
     *
     * @return token
     */
    public String getTokenOrNew() {
        val token = getToken();
        if(StrUtil.isNotEmpty(token)) {
            return token;
        }
        return IdUtil.fastUUID();
    }

    /**
     * 校验 jwt 并解析其中携带的 token
     *
     * @param jwt jwt（先去除前缀）
     * @return jwt 载荷中的 token；jwt 为空或校验失败返回 null
     */
    public String getTokenByJwt(String jwt) {

        jwt = CAuthUtils.removePrefix(jwt);
        if(StrUtil.isBlank(jwt)) {
            log.debug("can't find jwt");
            return null;
        }

        try {
            CJwtUtils.verify(jwt, authConfig.getJwtSecret());
        } catch (Exception e) {
            log.debug("verify jwt error", e);
            return null;
        }

        val jwtBody = CJwtUtils.parseBody(jwt, CMapUtils.MAP_STRING_STRING_TYPE_REFERENCE);
        return MapUtil.getStr(jwtBody, TOKEN);
    }

}
