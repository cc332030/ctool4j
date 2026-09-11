package com.c332030.ctool4j.web.constant;

import com.c332030.ctool4j.core.util.CSet;
import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * <p>
 * Description: CResourceUrlConstants
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CResourceUrlConstants}（{@code @UtilityClass}）为静态资源 URL 常量类。</p>
 * <p>常量：</p>
 * <ul>
 *   <li>{@code FAVICON_ICO_URL = "/favicon.ico"}：favicon 图标地址</li>
 *   <li>{@code IGNORE_RESOURCE_URLS}：需要忽略的静态资源地址集合，默认含 {@code FAVICON_ICO_URL}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无</td>
 *     <td>纯常量类，无行为</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要判断/忽略 favicon 等静态资源的过滤器场景。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>集合为不可变快照（{@code CSet.of}），新增忽略资源需修改常量。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>常量收敛</b></p>
 * <ul>
 *   <li>静态资源 URL 集中管理，供 {@code CResourceFilter} 等判断忽略的资源。</li>
 * </ul>
 *
 * @since 2026/1/28
 * @version 1.0
 */
@UtilityClass
public class CResourceUrlConstants {

    /**
     * favicon 图标地址
     */
    public final String FAVICON_ICO_URL = "/favicon.ico";

    /**
     * 需要忽略的静态资源地址集合
     */
    public final Set<String> IGNORE_RESOURCE_URLS = CSet.of(
        FAVICON_ICO_URL
    );

}
