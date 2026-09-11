package com.c332030.ctool4j.cache.aop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDefaultCacheIdConverterTests
 * </p>
 *
 * <p>
 * 是 {@link CDefaultCacheIdConverter} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>围绕 {@code applyThrowable(key, object)} 的 key/object 组合覆盖：都 null、key null（object 为字符串/空串/非字符串）、</li>
 *   <li>key 非 null（object null / 同时存在）、key 特殊字符、key 空白。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对生成规则的约定：key 优先、key 为 null 退 object、都 null 返回 null。</li>
 *   <li>依据白盒原则：覆盖 key/object 为 null 与否的全部组合，以及 key/object 的典型形态（字符串、非字符串、空、特殊字符、空白）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：双 null、key null + object 各种形态、key 非 null + object null、双非 null、key 特殊字符/空白。</li>
 *   <li>未覆盖：object 为 null 且 key 为特殊对象（如非字符串对象）——key 优先语义下不涉及；复合 key 生成（本类不提供）。</li>
 * </ul>
 * <h2>生成规则</h2>
 * <ul>
 *   <li>1.1 双 null：返回 null（testApplyThrowable_bothNull）</li>
 *   <li>1.2 key null + object 字符串：返回 object（testApplyThrowable_keyNull_objectPresent）</li>
 *   <li>1.3 key null + object 空串：返回空串（testApplyThrowable_keyNull_objectEmpty）</li>
 *   <li>1.4 key null + object 非字符串：返回字符串形式（testApplyThrowable_keyNull_objectNonString）</li>
 *   <li>1.5 key 非 null + object null：返回 key（testApplyThrowable_keyPresent_objectNull）</li>
 *   <li>1.6 双非 null：优先返回 key（testApplyThrowable_bothPresent_usesKey）</li>
 *   <li>1.7 key 含特殊字符：原样返回（testApplyThrowable_keySpecialChars）</li>
 *   <li>1.8 key 为空白：原样返回（testApplyThrowable_keyWhitespace）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CDefaultCacheIdConverterTests {

    private final CDefaultCacheIdConverter converter = new CDefaultCacheIdConverter();

    /**
     * 对应测试用例 1.1：key 与 object 均为 null
     */
    @Test
    void testApplyThrowable_bothNull() throws Throwable {
        Assertions.assertNull(converter.applyThrowable(null, null));
    }

    /**
     * 对应测试用例 1.2：key null + object 字符串
     */
    @Test
    void testApplyThrowable_keyNull_objectPresent() throws Throwable {
        Assertions.assertEquals("hello", converter.applyThrowable(null, "hello"));
    }

    /**
     * 对应测试用例 1.3：key null + object 空串
     */
    @Test
    void testApplyThrowable_keyNull_objectEmpty() throws Throwable {
        Assertions.assertEquals("", converter.applyThrowable(null, ""));
    }

    /**
     * 对应测试用例 1.4：key null + object 非字符串
     */
    @Test
    void testApplyThrowable_keyNull_objectNonString() throws Throwable {
        Assertions.assertEquals("123", converter.applyThrowable(null, 123));
    }

    /**
     * 对应测试用例 1.5：key 非 null + object null
     */
    @Test
    void testApplyThrowable_keyPresent_objectNull() throws Throwable {
        Assertions.assertEquals("id1", converter.applyThrowable("id1", null));
    }

    /**
     * 对应测试用例 1.6：双非 null，优先返回 key
     */
    @Test
    void testApplyThrowable_bothPresent_usesKey() throws Throwable {
        Assertions.assertEquals("id1", converter.applyThrowable("id1", "object"));
    }

    /**
     * 对应测试用例 1.7：key 含特殊字符
     */
    @Test
    void testApplyThrowable_keySpecialChars() throws Throwable {
        Assertions.assertEquals("a:b", converter.applyThrowable("a:b", "obj"));
    }

    /**
     * 对应测试用例 1.8：key 为空白
     */
    @Test
    void testApplyThrowable_keyWhitespace() throws Throwable {
        Assertions.assertEquals("  ", converter.applyThrowable("  ", "obj"));
    }
}
