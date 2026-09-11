package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.model.CCacheUser;
import com.c332030.ctool4j.cache.service.CCacheTestService;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CCacheAspectTests
 * </p>
 *
 * <p>
 * 是 {@link CCacheAspect} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过真实 Spring 容器（{@code @CTool4jSpringBootTest}）集成验证切面拦截生效、缓存命中与过期。</li>
 *   <li>覆盖：相同 key 缓存命中（值相等）、不同 key 不命中、过期后重新计算（值不等）、</li>
 *   <li>缓存方法抛异常时向上传播且异常不写缓存。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对切面读缓存/未命中写缓存、异常不捕获直接抛出的约定。</li>
 *   <li>依据最真实场景优先原则：走完整 Spring 容器 + 真实缓存流程，而非 mock 单点。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：命中、未命中、过期重算、异常传播（Q28 修复，异常不写缓存）。</li>
 *   <li>未覆盖：Redis 模式下的分布式锁竞争细节（由 CCacheBuilderTests/集成环境覆盖）；异步刷新并发竞争（由缓存真实环境验证）。</li>
 * </ul>
 * <h2>切面缓存行为</h2>
 * <ul>
 *   <li>1.1 本地缓存命中/未命中/过期：相同 key 命中值相等、不同 key 不等、过期后重算不等（cacheAspect）</li>
 *   <li>1.2 异常传播：缓存方法抛异常向上抛出、异常不写缓存（cacheErrorPropagates）</li>
 * </ul>
 *
 *
 * @since 2026/6/16
 * @version 1.0
 */
@CTool4jSpringBootTest
public class CCacheAspectTests {

    @Autowired
    CCacheTestService cacheTestService;

    /**
     * 测试缓存切面的生效、命中与过期
     *
     * <p>对应测试用例 1.1：本地缓存命中/未命中/过期</p>
     */
    @Test
    @SneakyThrows
    public void cacheAspect() {

        val idA = 1;
        val idB = 2;

        val a1 = cacheTestService.time(idA);
        val b1 = cacheTestService.time(idB);

        val cacheUser = CCacheUser.builder()
            .id(System.currentTimeMillis())
            .build();
        val uA1 = cacheTestService.userCache(cacheUser);

        TimeUnit.MILLISECONDS.sleep(10);
        val a2 = cacheTestService.time(idA);
        val uA2 = cacheTestService.userCache(cacheUser);

        TimeUnit.SECONDS.sleep(1);
        val a3 = cacheTestService.time(idA);
        val uA3 = cacheTestService.userCache(cacheUser);

        Assertions.assertEquals(a1, a2);
        Assertions.assertEquals(uA1, uA2);

        Assertions.assertNotEquals(a1, b1);

        Assertions.assertNotEquals(a1, a3);
        Assertions.assertNotEquals(uA1, uA3);

    }

    /**
     * 测试缓存方法抛异常时向上传播，不返回 null（Q28 修复）
     * <p>注意：缓存 key 仅由参数生成（不含方法名），需用独立 id 避免与 cacheAspect() 中 time(id) 串 key</p>
     *
     * <p>对应测试用例 1.2：异常传播，异常不写缓存</p>
     */
    @Test
    public void cacheErrorPropagates() {

        val errorId = 999;
        val ex = Assertions.assertThrowsExactly(
            IllegalStateException.class,
            () -> cacheTestService.error(errorId)
        );
        Assertions.assertTrue(ex.getMessage().contains("cache error: " + errorId));

        // 异常未写入缓存：再次调用仍抛异常（而非命中缓存返回）
        Assertions.assertThrowsExactly(
            IllegalStateException.class,
            () -> cacheTestService.error(errorId)
        );

    }

}
