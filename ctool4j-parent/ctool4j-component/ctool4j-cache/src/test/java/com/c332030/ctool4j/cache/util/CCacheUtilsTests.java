package com.c332030.ctool4j.cache.util;

import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * <p>
 * Description: CCacheUtilsTests
 * </p>
 *
 * <p>
 * 是 {@link CCacheUtils} 的测试用例。
 * </p>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证静态入口在 {@code cacheService} 注入前（null）调用 {@code cacheBuilder} 抛 NPE（未注入兜底）；</li>
 *   <li>注入后正确转发到 {@code CCacheService.cacheBuilder} 返回构建器。</li>
 *   <li>使用 Mockito mock {@code CLockService}/{@code CStringStringRedisService} 构造真实 {@code CCacheService}。</li>
 *   <li>测试前后 setCacheService(null) 清空静态字段，避免依赖测试执行顺序。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code cacheService} 依赖注入与转发的约定。</li>
 *   <li>依据黑盒原则：覆盖"未注入抛异常"与"注入后正常转发"两条路径。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：未注入抛 NPE、注入后转发返回构建器。</li>
 *   <li>未覆盖：注入后构建器实际的读-算-写行为（由 {@code CCacheBuilderTests} 覆盖）。</li>
 * </ul>
 * <h2>静态入口转发</h2>
 * <ul>
 *   <li>1.1 未注入：{@code cacheBuilder} 抛 {@code NullPointerException}（cacheBuilder_notSet_throwsNullPointerException）</li>
 *   <li>1.2 已注入：转发到 {@code CCacheService.cacheBuilder} 返回非空构建器（cacheBuilder_set_delegatesToCacheService）</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
 */
public class CCacheUtilsTests {

    private CCacheService cacheService;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        // @CAutowired 注入器可能在其他 Spring 测试（如 CCacheAspectTests）启动时写入静态字段，
        // 这里先清空，保证本测试从 null 起点开始、不依赖测试执行顺序
        CCacheUtils.setCacheService(null);
        cacheService = new CCacheService(
            Mockito.mock(CLockService.class),
            Mockito.mock(CStringStringRedisService.class));
    }

    /**
     * 每个用例执行后的清理
     */
    @AfterEach
    public void tearDown() {
        CCacheUtils.setCacheService(null);
    }

    /**
     * 对应测试用例 1.1：未注入时 cacheBuilder 抛 NPE
     */
    @Test
    public void cacheBuilder_notSet_throwsNullPointerException() {
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCacheUtils.cacheBuilder("key", String.class));
    }

    /**
     * 对应测试用例 1.2：注入后转发到 CCacheService
     */
    @Test
    public void cacheBuilder_set_delegatesToCacheService() {
        CCacheUtils.setCacheService(cacheService);

        CCacheService.CCacheBuilder<String> builder = CCacheUtils.cacheBuilder("key", String.class);

        Assertions.assertNotNull(builder);
    }

}
