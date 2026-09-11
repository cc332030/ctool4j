package com.c332030.ctool4j.auth.interfaces;

import com.c332030.ctool4j.definition.interfaces.ICCreateMillis;
import com.c332030.ctool4j.definition.interfaces.ICToken;

/**
 * <p>
 * Description: ICJwtInfo
 * </p>
 *
 * <p>JWT 信息接口：作为 jwt body（载荷）的基类接口，实现类自身字段即为 jwt 载荷内容，
 * 由 {@code CJwtUtils#create} 转换为载荷后生成 jwt，便捷入口见 {@code CAuthUtils#setJwt}。</p>
 *
 * <p>继承 {@link ICToken}，{@code getToken}/{@code setToken} 对应 jwt 载荷中的 token 字段（字段名见
 * {@link ICToken#TOKEN}），供 {@code CAuthUtils#getTokenByJwt} 解析回填使用；
 * 创建时间（毫秒时间戳）能力继承自 {@link ICCreateMillis}。</p>
 *
 * @author c332030
 * @since 2026/9/11
 * @version 1.0
 */
public interface ICJwtInfo extends ICToken, ICCreateMillis {

}
