package com.c332030.ctool4j.log.test.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CMdcTests
 * </p>
 *
 * <p>
 * 是 {@link CMdc} 的测试用例（对应测试文档
 * <code>doc/design/log/CMdcTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/31
 */
class CMdcTests {

    private CMdc mdc;

    @BeforeEach
    void setUp() {
        mdc = new CMdc();
        mdc.clear();
    }

    /**
     * 正常路径：put 后可 get 到对应值
 * <p>
 * 对应测试用例 1.1
 */
    @Test
    void putAndGet() {
        mdc.put("key", "value");
        Assertions.assertEquals("value", mdc.get("key"));
    }

    /**
     * 正常路径：重复 put 覆盖旧值
 * <p>
 * 对应测试用例 1.2
 */
    @Test
    void putOverwrite() {
        mdc.put("key", "v1");
        mdc.put("key", "v2");
        Assertions.assertEquals("v2", mdc.get("key"));
    }

    /**
     * 正常路径：remove 后 get 返回 null
 * <p>
 * 对应测试用例 1.3
 */
    @Test
    void remove() {
        mdc.put("key", "value");
        mdc.remove("key");
        Assertions.assertNull(mdc.get("key"));
    }

    /**
     * 边界：未 put 过的 key 返回 null
 * <p>
 * 对应测试用例 1.4
 */
    @Test
    void getNonExistentReturnsNull() {
        Assertions.assertNull(mdc.get("not-exist"));
    }

    /**
     * 边界：clear 清空所有值
 * <p>
 * 对应测试用例 1.5
 */
    @Test
    void clearEmpties() {
        mdc.put("a", "1");
        mdc.put("b", "2");
        mdc.clear();
        Assertions.assertNull(mdc.get("a"));
        Assertions.assertNull(mdc.get("b"));
    }

    /**
     * 正常路径：containsKey 判断键是否存在
 * <p>
 * 对应测试用例 2.1
 */
    @Test
    void containsKey() {
        mdc.put("a", "1");
        Assertions.assertTrue(mdc.containsKey("a"));
        Assertions.assertFalse(mdc.containsKey("not-exist"));
    }

    /**
     * 边界：isEmpty 判断是否为空
 * <p>
 * 对应测试用例 2.2
 */
    @Test
    void isEmpty() {
        Assertions.assertTrue(mdc.isEmpty());
        mdc.put("a", "1");
        Assertions.assertFalse(mdc.isEmpty());
    }

    /**
     * 异常路径：put(null, val) 抛空指针异常
 * <p>
 * 对应测试用例 3.1
 */
    @Test
    void putNullKeyThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put(null, "v"));
    }

    /**
     * 异常路径：put(key, null) 抛空指针异常（ConcurrentHashMap 不允许 null 值）
 * <p>
 * 对应测试用例 3.2
 */
    @Test
    void putNullValueThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put("k", null));
    }
}
