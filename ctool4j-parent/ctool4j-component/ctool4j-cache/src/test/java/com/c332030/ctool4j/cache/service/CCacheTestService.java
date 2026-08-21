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
 * @since 2026/6/16
 * @see "doc/design/cache/CCacheTestService.adoc"
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
    public Long error(Integer id) {
        throw new IllegalStateException("cache error: " + id);
    }

}
