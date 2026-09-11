package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.aop.CCacheAspectTests;
import com.c332030.ctool4j.cache.model.CCacheUser;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CCacheTestService
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CCacheTestService} 为缓存测试辅助 Service（{@code @Service}），提供带 {@code @CCacheable} 注解的测试方法：</p>
 * <ul>
 *   <li>{@code time(Integer id)}：按 id 缓存，返回当前时间戳</li>
 *   <li>{@code time(CCacheUser cacheUser)}：按对象缓存，返回当前时间戳</li>
 * </ul>
 * <p>命名空间均为 {@code CCacheAspectTests.class}，expire=1，供缓存切面测试验证缓存生效与过期。</p>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>缓存切面测试（{@code CCacheAspectTests}）的辅助 Service，验证 {@code @CCacheable} 注解行为。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；缓存过期时间为 1，避免测试数据残留。</li>
 * </ul>
 *
 * @since 2026/6/16
 * @version 1.0
 */
@Service
public class CCacheTestService {

    /**
     * 测试按 id 缓存，返回当前时间戳
     *
     * @param id 缓存键 id
     * @return 当前时间戳
     */
    @CCacheable(
        namespace = CCacheAspectTests.class,
        expire = 1
    )
    /**
     * 按 id 生成当前时间戳（缓存用例的时间基准，用于验证缓存命中/过期）
     */
    public Long time(Integer id) {
        return System.currentTimeMillis();
    }

    /**
     * 测试按对象缓存，返回当前时间戳
     *
     * @param cacheUser 缓存键对象
     * @return 当前时间戳
     */
    @CCacheable(
        namespace = CCacheAspectTests.class,
        expire = 1
    )
    /**
     * 按 {@code @CCacheId} 字段缓存用户对象（验证对象缓存命中与过期）
     */
    public Long userCache(CCacheUser cacheUser) {
        return System.currentTimeMillis();
    }

    /**
     * 测试缓存方法抛异常时向上传播，不被切面吞掉
     *
     * @param id 缓存键 id
     * @return 永远抛异常
     */
    @CCacheable(
        namespace = CCacheAspectTests.class,
        expire = 1
    )
    /**
     * 固定抛出异常，用于验证缓存方法异常向上传播且不写缓存
     */
    public Long error(Integer id) {
        throw new IllegalStateException("cache error: " + id);
    }

}
