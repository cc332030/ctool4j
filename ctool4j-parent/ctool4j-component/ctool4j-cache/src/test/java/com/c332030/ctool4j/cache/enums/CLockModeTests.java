package com.c332030.ctool4j.cache.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * <p>
 * Description: CLockModeTests
 * </p>
 *
 * <p>
 * 是 {@link CLockMode} 的测试用例（对应测试文档
 * <code>doc/design/cache/CLockModeTests.adoc</code>）。
 * </p>
 */
public class CLockModeTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        CLockMode[] values = CLockMode.values();

        Assertions.assertEquals(2, values.length);
        Assertions.assertTrue(Arrays.asList(values).contains(CLockMode.LOCAL));
        Assertions.assertTrue(Arrays.asList(values).contains(CLockMode.DISTRIBUTED));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void text() {

        Assertions.assertEquals("本地锁", CLockMode.LOCAL.getText());
        Assertions.assertEquals("分布式锁", CLockMode.DISTRIBUTED.getText());

    }

}
