package com.c332030.ctool4j.cache.aop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDefaultCacheIdConverterTests
 * </p>
 *
 * @since 2026/8/14
 */
class CDefaultCacheIdConverterTests {

    private final CDefaultCacheIdConverter converter = new CDefaultCacheIdConverter();

    @Test
    void testApplyThrowable_bothNull() throws Throwable {
        Assertions.assertNull(converter.applyThrowable(null, null));
    }

    @Test
    void testApplyThrowable_keyNull_objectPresent() throws Throwable {
        Assertions.assertEquals("hello", converter.applyThrowable(null, "hello"));
    }

    @Test
    void testApplyThrowable_keyNull_objectEmpty() throws Throwable {
        Assertions.assertEquals("", converter.applyThrowable(null, ""));
    }

    @Test
    void testApplyThrowable_keyNull_objectNonString() throws Throwable {
        Assertions.assertEquals("123", converter.applyThrowable(null, 123));
    }

    @Test
    void testApplyThrowable_keyPresent_objectNull() throws Throwable {
        Assertions.assertEquals("id1", converter.applyThrowable("id1", null));
    }

    @Test
    void testApplyThrowable_bothPresent_usesKey() throws Throwable {
        Assertions.assertEquals("id1", converter.applyThrowable("id1", "object"));
    }

    @Test
    void testApplyThrowable_keySpecialChars() throws Throwable {
        Assertions.assertEquals("a:b", converter.applyThrowable("a:b", "obj"));
    }

    @Test
    void testApplyThrowable_keyWhitespace() throws Throwable {
        Assertions.assertEquals("  ", converter.applyThrowable("  ", "obj"));
    }
}
