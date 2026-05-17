package com.c332030.ctool4j.web.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CAuthUtils
 * </p>
 *
 * @since 2026/3/16
 */
@UtilityClass
public class CAuthUtils {

    /**
     * token 前缀
     */
    public String TOKEN_PREFIX = "Bearer";

    /**
     * 移除前缀
     * @param token token
     * @return token
     */
    public String removePrefix(String token) {

        if(StrUtil.isBlank(token)) {
            return null;
        }

        if(token.length() <= TOKEN_PREFIX.length() ||
            !token.startsWith(TOKEN_PREFIX)) {
            return token;
        }

        return token.substring(TOKEN_PREFIX.length() + 1);
    }

    /**
     * 获取 token
     * @return token
     */
    public String getToken() {
        return getToken(TOKEN_PREFIX);
    }

    /**
     * 获取 token
     * @param prefix 前缀
     * @return token
     */
    public String getToken(String prefix) {
        return getToken(CRequestUtils.getRequest(), prefix);
    }

    /**
     * 获取 token
     * @param request 请求
     * @return token
     */
    public String getToken(HttpServletRequest request) {
        return getToken(request, TOKEN_PREFIX);
    }

    /**
     * 获取 token
     * @param request 请求
     * @param prefix 前缀
     * @return token
     */
    public String getToken(HttpServletRequest request, String prefix) {

        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StrUtil.isEmpty(authorization)
            || !authorization.startsWith(prefix)
        ) {
            return null;
        }

        return authorization.substring(prefix.length() + 1);

    }

    /**
     * 设置 token
     * @param token token
     */
    public void setToken(String token) {
        setToken(token, TOKEN_PREFIX);
    }

    /**
     * 设置 token
     * @param token token
     * @param prefix 前缀
     */
    public void setToken(String token, String prefix) {
        setToken(token, prefix, CRequestUtils.getResponse());
    }

    /**
     * 设置 token
     * @param token token
     * @param response 响应
     */
    public void setToken(String token, HttpServletResponse response) {
        setToken(token, TOKEN_PREFIX, response);
    }

    /**
     * 设置 token
     * @param token token
     * @param prefix 前缀
     * @param response 响应
     */
    public void setToken(String token, String prefix, HttpServletResponse response) {
        response.setHeader(HttpHeaders.AUTHORIZATION, prefix + " " + token);
    }

}
