package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheRemove;
import com.c332030.ctool4j.cache.annotation.CCacheUpdate;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.service.CCacheService;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Description: CCacheRemoveUpdateAspectTests
 * </p>
 * <p>
 * 最小本地（Caffeine）集成用例：验证 {@link CCacheRemove}（删除缓存）与 {@link CCacheUpdate}
 * （更新缓存）两个注解经 {@link CCacheAspect} 的语义——方法执行成功后删除 / 更新缓存，
 * 以及方法异常时的行为。
 * </p>
 * <p>
 * 复用 {@link CCacheElLocalAspectTests.LocalConfig}（最小 Spring AOP 上下文 + mock cacheService），
 * 本地缓存模式不依赖真实 Redis，避免加载 ctool4j-spring 的 CSpringConfiguration。
 * </p>
 *
 * @since 2026/9/11
 * @see "doc/design/cache/CCacheRemoveUpdateAspectTests.adoc"
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    CCacheElLocalAspectTests.LocalConfig.class,
    CCacheRemoveUpdateAspectTests.RemoveUpdateConfig.class
})
class CCacheRemoveUpdateAspectTests {

    /**
     * 读方法真正执行（缓存未命中）时自增；命中时直接返回缓存值，计数不变
     */
    static final AtomicLong EXECUTE_COUNT = new AtomicLong();

    /**
     * namespace 标记类
     */
    static class Namespace {
    }

    /**
     * 缓存读写 + 更新 + 删除业务方法：共用同一 namespace + 同一 key 规则
     */
    static class RemoveUpdateCacheService {

        /**
         * 读缓存：首次执行返回递增序号（缓存写入），命中返回缓存值（计数不变）
         */
        @CCacheable(namespace = Namespace.class, expire = 1)
        public Long readById(Integer id) {
            return EXECUTE_COUNT.incrementAndGet();
        }

        /**
         * 删除缓存：方法成功后删除对应 key 的缓存
         */
        @CCacheRemove(namespace = Namespace.class)
        public void removeById(Integer id) {
        }

        /**
         * 删除缓存 + 抛异常：验证方法异常时向上传播、不删除缓存
         */
        @CCacheRemove(namespace = Namespace.class)
        public void removeError(Integer id) {
            throw new IllegalStateException("remove error: " + id);
        }

        /**
         * 更新缓存：方法成功后把确定值写入缓存
         */
        @CCacheUpdate(namespace = Namespace.class, expire = 1)
        public Long updateById(Integer id) {
            if (null == id) {
                return null;
            }
            return 9000L + id;
        }

        /**
         * 更新缓存 + 返回 null：验证不写入空值
         */
        @CCacheUpdate(namespace = Namespace.class, expire = 1)
        public Long updateNull(Integer id) {
            return null;
        }
    }

    /**
     * 仅注册本地业务 Bean（cacheService 复用 {@link CCacheElLocalAspectTests.LocalConfig}）
     */
    @Configuration
    static class RemoveUpdateConfig {

        @Bean
        RemoveUpdateCacheService removeUpdateCacheService() {
            return new RemoveUpdateCacheService();
        }
    }

    @Autowired
    RemoveUpdateCacheService service;

    /**
     * 对应测试用例 1.1：remove 删除缓存后重新计算（不再命中）
     */
    @Test
    void testLocalCache_remove_recompute() {

        val id = 1;

        val v1 = service.readById(id);   // 未命中，执行 count 自增
        val v2 = service.readById(id);   // 命中，返回缓存值，count 不变

        Assertions.assertEquals(v1, v2);

        // remove 删除缓存
        service.removeById(id);

        val v3 = service.readById(id);   // 缓存已删，重新执行，count 自增
        Assertions.assertNotEquals(v2, v3);
    }

    /**
     * 对应测试用例 1.2：remove 方法抛异常时向上传播、且不删除缓存
     */
    @Test
    void testLocalCache_removeError_notRemove() {

        val id = 2;

        val v1 = service.readById(id);   // 写入缓存

        // remove 方法抛异常，异常向上传播
        Assertions.assertThrowsExactly(
            IllegalStateException.class,
            () -> service.removeError(id));

        // 缓存未被删除：再次读取仍命中旧值
        val v2 = service.readById(id);
        Assertions.assertEquals(v1, v2);
    }

    /**
     * 对应测试用例 1.3：update 更新缓存后，读方法命中更新后的值
     */
    @Test
    void testLocalCache_update_updateHit() {

        val id = 3;

        service.readById(id);            // 未命中，写入缓存

        val updateValue = service.updateById(id);   // 方法成功，返回值写入缓存

        val hit = service.readById(id);   // 命中 update 写入的新值
        Assertions.assertEquals(updateValue, hit);
        Assertions.assertEquals(Long.valueOf(9000 + id), hit);
    }

    /**
     * 对应测试用例 1.4：update 方法返回 null 时不写入缓存（保留原值）
     */
    @Test
    void testLocalCache_updateNull_keepOld() {

        val id = 4;

        val v1 = service.readById(id);   // 写入缓存

        // update 方法返回 null，不写入
        Assertions.assertNull(service.updateNull(id));

        // 缓存仍保留原值
        val v2 = service.readById(id);
        Assertions.assertEquals(v1, v2);
    }

    /**
     * 对应测试用例 1.5：无缓存 key（参数为 null）时 remove/update 跳过，不抛错
     */
    @Test
    void testLocalCache_removeUpdateNullKey_skip() {

        // 参数为 null → resolveCacheKey 返回 null → 跳过缓存删除
        Assertions.assertDoesNotThrow(() -> service.removeById(null));

        // update 参数为 null → key 为 null，跳过缓存写入（不抛错）
        Assertions.assertDoesNotThrow(() -> service.updateById(null));
    }

    /**
     * 对应测试用例 1.6：update 写入后首次读取即命中更新值（无前置缓存）
     */
    @Test
    void testLocalCache_update_writeThenHit() {

        val id = 5;

        // 无前置缓存时直接 update：返回值写入缓存
        val updateValue = service.updateById(id);

        // 首次 read 即命中 update 写入的新值，不再执行原方法
        val hit = service.readById(id);
        Assertions.assertEquals(updateValue, hit);
        Assertions.assertEquals(Long.valueOf(9000 + id), hit);
    }

}
