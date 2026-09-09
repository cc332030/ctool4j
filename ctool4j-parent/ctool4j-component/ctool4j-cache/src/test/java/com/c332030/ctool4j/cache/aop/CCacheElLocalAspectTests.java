package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Description: CCacheElLocalAspectTests
 * </p>
 * <p>
 * 最小本地（Caffeine）集成用例：验证带 {@code key()} 的 {@code @CCacheable} 方法经
 * {@link CCacheAspect} 实际缓存生效——首次执行写缓存、二次同 key 命中不重算、不同 key 不共享。
 * </p>
 * <p>
 * 使用最小 Spring AOP 上下文（非完整 Spring Boot / 非 {@code CTool4jSpringBootTest}），
 * 仅加载切面 + 本地业务 Bean + mock 的 {@link CCacheService}（local 模式不使用 cacheService），
 * 不依赖真实 Redis，也避免加载 ctool4j-spring 的 {@code CSpringConfiguration}（其 RestTemplate
 * 依赖 httpclient）。
 * </p>
 *
 * @since 2026/9/8
 * @see "doc/design/cache/CCacheElLocalAspectTests.adoc"
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CCacheElLocalAspectTests.LocalConfig.class)
class CCacheElLocalAspectTests {

    /**
     * 命中计数：缓存未命中（真正执行方法）时自增；命中时直接返回缓存值，计数不变
     */
    static final AtomicLong EXECUTE_COUNT = new AtomicLong();

    @Data
    @AllArgsConstructor
    static class User {

        private Long id;

        private String name;
    }

    /**
     * namespace 标记类
     */
    static class Namespace {
    }

    /**
     * 本地缓存业务方法：key 走 el 表达式，返回执行序号
     */
    static class ElCacheService {

        @CCacheable(namespace = Namespace.class, key = "user.id")
        public Long elById(User user) {
            return EXECUTE_COUNT.incrementAndGet();
        }

        @CCacheable(namespace = Namespace.class, key = "user.id")
        public Long elNullParam(User user) {
            return EXECUTE_COUNT.incrementAndGet();
        }
    }

    /**
     * 最小本地上下文：仅注册切面、mock cacheService 与本地业务 Bean
     */
    @Configuration
    @EnableAspectJAutoProxy
    @Import(CCacheAspect.class)
    static class LocalConfig {

        @Bean
        CCacheService cacheService() {
            return new CCacheService(
                Mockito.mock(CLockService.class),
                Mockito.mock(CStringStringRedisService.class));
        }

        @Bean
        ElCacheService elCacheService() {
            return new ElCacheService();
        }
    }

    @Autowired
    ElCacheService elCacheService;

    /**
     * 对应测试用例 1.1：el key 缓存命中（同 key 二次调用不重算）
     */
    @Test
    void testLocalCache_elKey_hit() {

        val user = new User(1L, "a");

        val c1 = elCacheService.elById(user); // 未命中，执行 count=1
        val c2 = elCacheService.elById(user); // 命中，返回缓存值，count 不变

        Assertions.assertEquals(c1, c2);
    }

    /**
     * 对应测试用例 1.2：不同 el key（不同对象 id）不共享缓存，分别执行
     */
    @Test
    void testLocalCache_elKey_distinctIsolation() {

        val u1 = new User(10L, "a");
        val u2 = new User(20L, "b");

        val c1 = elCacheService.elById(u1);
        val c2 = elCacheService.elById(u2);

        Assertions.assertNotEquals(c1, c2);
    }

    /**
     * 对应测试用例 1.3：参数为 null（el 取不到 key）不写缓存，每次执行
     */
    @Test
    void testLocalCache_elNullParam_noCache() {

        // 参数为 null → resolveCacheKey 返回 null → 不入缓存，每次都执行
        val n1 = elCacheService.elNullParam(null);
        val n2 = elCacheService.elNullParam(null);

        Assertions.assertNotEquals(n1, n2);
    }

}
