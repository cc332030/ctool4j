package com.c332030.ctool4j.cache.util;

import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CCacheUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCacheUtils}（{@code @UtilityClass} + {@code @CAutowiredScan}）为缓存构建器的静态访问入口， 持有 {@code CCacheService}（{@code @CAutowired} 注入），暴露 {@code cacheBuilder(key, tClass)} 静态方法， 转发给 {@code CCacheService.cacheBuilder}。</p>
 * <p>核心方法：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code cacheService} 未注入</td>
 *     <td>{@code cacheBuilder} 调用将 NPE（依赖 Spring 注入，需先初始化容器）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>非 Spring Bean 的静态代码需要获取缓存构建器时。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code cacheService} 已注入；独立使用（无 Spring 上下文）时不适用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>静态注入依赖 Spring 容器初始化，静态方法调用前需确保注入完成。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>通过 {@code @CAutowiredScan}/{@code @CAutowired} 将 {@code CCacheService} 注入为静态可访问字段，</li>
 *   <li>使无 Spring 注入的静态代码也能便捷获取缓存构建器。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>静态字段 {@code cacheService} 由 {@code @CAutowired} 注入，{@code cacheBuilder} 直接转发。</li>
 * </ul>
 *
 * @since 2025/9/27
 * @version 1.0
 */
@UtilityClass
@CAutowiredScan
public class CCacheUtils {

    @Setter
    @CAutowired
    CCacheService cacheService;

    /**
     * 获取缓存构建器
     * <ul>
     *   <li>{@code cacheBuilder(String key, Class&lt;T&gt; tClass)}：返回 {@code CCacheService.CCacheBuilder&lt;T&gt;} 缓存构建器。</li>
     * </ul>
     *
     * @param key    缓存 key
     * @param tClass 缓存值类型
     * @param <T>    缓存值类型
     * @return 缓存构建器
     */
    public <T> CCacheService.CCacheBuilder<T> cacheBuilder(String key, Class<T> tClass) {
        return cacheService.cacheBuilder(key, tClass);
    }

}
