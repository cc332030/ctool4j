package com.c332030.ctool4j.auth.interfaces;

/**
 * <p>
 * Description: ICJwtInfo
 * </p>
 *
 * <p>JWT 信息接口：作为 jwt body（载荷）的基类接口，实现类自身字段即为 jwt 载荷内容，
 * 由 {@code CJwtUtils#create} 转换为载荷后生成 jwt，便捷入口见 {@code CAuthUtils#setJwt}。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
public interface ICJwtInfo {

}
