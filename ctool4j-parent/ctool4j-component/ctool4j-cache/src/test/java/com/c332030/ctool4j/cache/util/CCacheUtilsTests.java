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
 * 是 {@link CCacheUtils} 的测试用例（对应测试文档
 * <code>doc/design/cache/CCacheUtilsTests.adoc</code>）。
 * </p>
 */
public class CCacheUtilsTests {

    private CCacheService cacheService;

    @BeforeEach
    public void setUp() {
        // @CAutowired 注入器可能在其他 Spring 测试（如 CCacheAspectTests）启动时写入静态字段，
        // 这里先清空，保证本测试从 null 起点开始、不依赖测试执行顺序
        CCacheUtils.setCacheService(null);
        cacheService = new CCacheService(
            Mockito.mock(CLockService.class),
            Mockito.mock(CStringStringRedisService.class));
    }

    @AfterEach
    public void tearDown() {
        CCacheUtils.setCacheService(null);
    }

    /** 对应测试用例 1.1：未注入时 cacheBuilder 抛 NPE */
    @Test
    public void cacheBuilder_notSet_throwsNullPointerException() {
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CCacheUtils.cacheBuilder("key", String.class));
    }

    /** 对应测试用例 1.2：注入后转发到 CCacheService */
    @Test
    public void cacheBuilder_set_delegatesToCacheService() {
        CCacheUtils.setCacheService(cacheService);

        CCacheService.CCacheBuilder<String> builder = CCacheUtils.cacheBuilder("key", String.class);

        Assertions.assertNotNull(builder);
    }

}
