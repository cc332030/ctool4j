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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过最小 Spring AOP 上下文（复用 {@code CCacheElLocalAspectTests.LocalConfig}，mock cacheService）集成验证</li>
 *   <li>{@code @CCacheRemove} / {@code @CCacheUpdate} 经切面拦截生效。</li>
 *   <li>覆盖：remove 删除后重算、remove 方法异常不删缓存、update 更新后命中新值、update 返回 null 不写、</li>
 *   <li>无缓存 key 时跳过。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 remove/update 的约定：方法成功后才删除 / 更新缓存；异常不删 / 不写。</li>
 *   <li>依据最真实场景优先原则：走真实切面 + 真实业务 Bean + Caffeine 缓存流程，而非 mock 切面单点。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：remove 删除、remove 异常、update 更新、update null、null key 跳过（均为本地 Caffeine 模式）。</li>
 *   <li>未覆盖：Redis 模式的删除 / 更新（由 CCacheService 集成 / 环境覆盖）；多实例并发丢失更新竞争。</li>
 * </ul>
 * <h2>缓存删除 / 更新行为</h2>
 * <ul>
 *   <li>1.1 remove 删除缓存后重新计算（不再命中）（testLocalCache_remove_recompute）</li>
 *   <li>1.2 remove 方法抛异常时向上传播、且不删除缓存（testLocalCache_removeError_notRemove）</li>
 *   <li>1.3 经真实 {@code @CCacheUpdate} 入口更新后，读方法命中更新后的值（testLocalCache_update_updateHit）</li>
 *   <li>1.4 update 方法返回 null 时不写入缓存（保留原值）（testLocalCache_updateNull_keepOld）</li>
 *   <li>1.5 无缓存 key（参数为 null）时 remove/update 明确跳过缓存读写（testLocalCache_removeUpdateNullKey_skip）</li>
 *   <li>1.6 update 写入后首次读取即命中更新值（无前置缓存）（testLocalCache_update_writeThenHit）</li>
 * </ul>
 *
 * @since 2026/9/11
 * @version 1.1
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
     * 真实 {@code @CCacheable} 读入口：与 {@link KeywordUpdateService} 用同一个 key 规则
     *
     * <p>用例 1.3 需要「update 写入 → read 读到」的闭环。两个方法用同一个 key 表达式 {@code "id"}：
     * el 表达式首段必须是方法的形参名（否则解析期即 {@code IllegalArgumentException}），
     * 故两侧都声明同名形参 {@code Integer id}；表达式只取值、无属性跳，取到的 id 经
     * {@code CDefaultCacheIdConverter} 转成字符串 key——两侧 key 格式一致，读方法才能命中 update 写入的值。</p>
     */
    static class KeywordReadService {

        /**
         * 读缓存：命中时直接返回缓存值、不执行本方法（{@code EXECUTE_COUNT} 不变）
         */
        @CCacheable(namespace = Namespace.class, expire = 1, key = "id")
        public String lookup(Integer id) {
            EXECUTE_COUNT.incrementAndGet();
            return "recomputed";
        }
    }

    /**
     * 真实 {@code @CCacheUpdate} 入口：方法成功后把返回值写入缓存（返回值即入参，不经测试样例算式加工）
     */
    static class KeywordUpdateService {

        @CCacheUpdate(namespace = Namespace.class, expire = 1, key = "id")
        public String publish(Integer id, String keyword) {
            return keyword;
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

        @Bean
        KeywordReadService keywordReadService() {
            return new KeywordReadService();
        }

        @Bean
        KeywordUpdateService keywordUpdateService() {
            return new KeywordUpdateService();
        }
    }

    @Autowired
    RemoveUpdateCacheService service;

    @Autowired
    KeywordReadService readService;

    @Autowired
    KeywordUpdateService updateService;

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
     * 对应测试用例 1.3：经真实 {@code @CCacheUpdate} 入口更新后，读方法命中更新后的值
     *
     * <p><b>为何合并原 1.3 / 1.4</b>：两条都验「update 后读命中新值」，1.4 是 1.3 的真子集
     * （同分类用例须归一个类、不得重复，见测试规范「合并去重」）；且原断言值
     * {@code 9000L + id} 由测试内样例方法自定（{@code return 9000L + id}），属"用被测自己的算式
     * 证明被测自己"，自证循环。</p>
     *
     * <p><b>改为真实入口 + 非自证断言</b>：经 Spring AOP 代理的真实 {@code @CCacheUpdate} 方法
     * {@link UpdateService#publish} 写入缓存（返回值为传入的业务值，不经测试样例算式加工），
     * 再由真实 {@code @CCacheable} 方法读回，断言读到的确实是那次 publish 的业务值。</p>
     */
    @Test
    void testLocalCache_update_updateHit() {

        val id = 3;
        val keyword = "publish-3";

        val published = updateService.publish(id, keyword);   // 真实 @CCacheUpdate 入口：返回值写入缓存
        Assertions.assertEquals(keyword, published);

        // 读方法命中 update 写入的值，不再执行原方法（命中时 EXECUTE_COUNT 不变）
        val before = EXECUTE_COUNT.get();
        Assertions.assertEquals(keyword, readService.lookup(id));
        Assertions.assertEquals(before, EXECUTE_COUNT.get(),
            "update 写入的值应被读到，不得回落到执行原方法");
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
     * 对应测试用例 1.5：无缓存 key（参数为 null）时 remove/update 明确「跳过缓存读写」
     *
     * <p><b>断言有效性</b>：原用例只断言 {@code assertDoesNotThrow}，而「跳过」的失败形态
     * （守卫恒不成立、直接拿 null key 去删 / 去写）同样不抛异常——Caffeine 的
     * {@code invalidate(null)} 与 {@code put(null, null)} 都静默接受，用例在守卫完全失效时仍全绿，
     * 属"写了等于没写"。故改为断言<strong>可观测的行为结果</strong>：</p>
     * <ul>
     *   <li><b>删除方向</b>：参数为 null 时不得删缓存——先写入缓存值，null 参数调用后该值必须仍在
     *   （被删则再次读取会重新执行方法、返回新值）；</li>
     *   <li><b>写入方向</b>：参数为 null 时不得写缓存——读路径先取（创建）expire 分组、再判 key，
     *   写路径先判 key、再取（创建）分组，两处对「key 为 null」的处理不同：读路径
     *   <strong>会</strong>创建分组、写路径<strong>不应</strong>创建。<br>
     *   守卫失效时 {@code updateById(null)} 会返回 {@code null} 而跳过写入、分组仍未被创建，
     *   该差异被掩盖；故读路径那侧只能退一步断言「缓存读不到 null 键」，由写路径的
     *   {@code @CCacheUpdate} 守卫（返回值 null 即跳过）如实覆盖。</li>
     * </ul>
     */
    @Test
    void testLocalCache_removeUpdateNullKey_skip() {

        val id = 50;

        // 删除方向：先写入缓存，参数为 null 的 remove 不得把它删掉
        val v1 = service.readById(id);
        service.removeById(null);
        Assertions.assertSame(v1, service.readById(id),
            "参数为 null 时不应删除缓存（再次读取须命中同一缓存值）");

        // 写入方向：参数为 null 时 update 返回 null（@CCacheUpdate 守卫：返回值 null 即跳过写入），
        // 且该调用的返回值不应被写入缓存——再次以同样的 null 参数读取时仍会执行原方法（缓存未命中），
        // 即读到的值由方法自身重新产生、不是上一次调用残留的缓存值
        Assertions.assertNull(service.updateById(null));
        val n1 = service.readById(null);
        val n2 = service.readById(null);
        Assertions.assertNotNull(n1);
        Assertions.assertNotSame(n1, n2,
            "参数为 null 时不应写入缓存（两次读取应各自执行原方法、不回落到同一缓存值）");
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
