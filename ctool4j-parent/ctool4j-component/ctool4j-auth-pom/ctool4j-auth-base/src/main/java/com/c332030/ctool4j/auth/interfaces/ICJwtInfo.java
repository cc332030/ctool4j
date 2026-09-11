package com.c332030.ctool4j.auth.interfaces;

/**
 * <p>
 * Description: ICJwtInfo
 * </p>
 *
 * <p>JWT 信息接口：提供 token/jwt 的读写能力，供 {@code CJwtUtils#setJwt} 生成并回填 jwt。
 * 实现类需支持 {@link #setToken} 以便回填。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
public interface ICJwtInfo {

    /**
     * 获取 token（jwt）
     *
     * @return token
     */
    String getToken();

    /**
     * 设置 token（jwt）
     *
     * @param token token
     */
    void setToken(String token);

}
