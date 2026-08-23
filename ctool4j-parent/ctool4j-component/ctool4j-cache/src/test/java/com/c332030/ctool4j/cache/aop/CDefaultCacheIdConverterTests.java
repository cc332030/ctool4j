package com.c332030.ctool4j.cache.aop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDefaultCacheIdConverterTests
 * </p>
 *
 * <p>
 * 是 {@link CDefaultCacheIdConverter} 的测试用例（对应测试文档
 * <code>doc/design/cache/CDefaultCacheIdConverterTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
 */
class CDefaultCacheIdConverterTests {

    private final CDefaultCacheIdConverter converter = new CDefaultCacheIdConverter();

    /** 对应测试用例 1.1：key 与 object 均为 null */
    @Test
    void testApplyThrowable_bothNull() throws Throwable {
        Assertions.assertNull(converter.applyThrowable(null, null));
    }

    /** 对应测试用例 1.2：key null + object 字符串 */
    @Test
    void testApplyThrowable_keyNull_objectPresent() throws Throwable {
        Assertions.assertEquals("hello", converter.applyThrowable(null, "hello"));
    }

    /** 对应测试用例 1.3：key null + object 空串 */
    @Test
    void testApplyThrowable_keyNull_objectEmpty() throws Throwable {
        Assertions.assertEquals("", converter.applyThrowable(null, ""));
    }

    /** 对应测试用例 1.4：key null + object 非字符串 */
    @Test
    void testApplyThrowable_keyNull_objectNonString() throws Throwable {
        Assertions.assertEquals("123", converter.applyThrowable(null, 123));
    }

    /** 对应测试用例 1.5：key 非 null + object null */
    @Test
    void testApplyThrowable_keyPresent_objectNull() throws Throwable {
        Assertions.assertEquals("id1", converter.applyThrowable("id1", null));
    }

    /** 对应测试用例 1.6：双非 null，优先返回 key */
    @Test
    void testApplyThrowable_bothPresent_usesKey() throws Throwable {
        Assertions.assertEquals("id1", converter.applyThrowable("id1", "object"));
    }

    /** 对应测试用例 1.7：key 含特殊字符 */
    @Test
    void testApplyThrowable_keySpecialChars() throws Throwable {
        Assertions.assertEquals("a:b", converter.applyThrowable("a:b", "obj"));
    }

    /** 对应测试用例 1.8：key 为空白 */
    @Test
    void testApplyThrowable_keyWhitespace() throws Throwable {
        Assertions.assertEquals("  ", converter.applyThrowable("  ", "obj"));
    }
}
