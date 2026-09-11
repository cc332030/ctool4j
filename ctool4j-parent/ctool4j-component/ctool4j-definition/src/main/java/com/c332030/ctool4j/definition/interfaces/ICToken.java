package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICToken
 * </p>
 *
 * <p>token 字段接口：提供 token 的读取与写入能力，字段名常量见 {@link #TOKEN}。</p>
 *
 * @since 2026/9/11
 * @version 1.0
 */
public interface ICToken {

    /**
     * token 字段名常量
     */
    String TOKEN = "token";

    /**
     * 获取 token
     * @return token
     */
    String getToken();

    /**
     * 设置 token
     * @param token token
     */
    void setToken(String token);

}
