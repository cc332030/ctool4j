package com.c332030.ctool4j.logback.test.mdc;

import com.c332030.ctool4j.logback.mdc.CMdc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CMdcTests
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
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
     */
    @Test
    void putAndGet() {
        mdc.put("key", "value");
        Assertions.assertEquals("value", mdc.get("key"));
    }

    /**
     * 正常路径：重复 put 覆盖旧值
     */
    @Test
    void putOverwrite() {
        mdc.put("key", "v1");
        mdc.put("key", "v2");
        Assertions.assertEquals("v2", mdc.get("key"));
    }

    /**
     * 正常路径：remove 后 get 返回 null
     */
    @Test
    void remove() {
        mdc.put("key", "value");
        mdc.remove("key");
        Assertions.assertNull(mdc.get("key"));
    }

    /**
     * 边界：未 put 过的 key 返回 null
     */
    @Test
    void getNonExistentReturnsNull() {
        Assertions.assertNull(mdc.get("not-exist"));
    }

    /**
     * 边界：clear 清空所有值
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
     * 正常路径：getCopyOfContextMap 返回副本，修改副本不影响原 MDC
     */
    @Test
    void getCopyOfContextMapIsCopy() {
        mdc.put("a", "1");
        Map<String, String> copy = mdc.getCopyOfContextMap();
        copy.put("b", "2");
        Assertions.assertNull(mdc.get("b"));
        Assertions.assertEquals("1", mdc.get("a"));
    }

    /**
     * 边界：空 MDC 时 getCopyOfContextMap 返回空 map 而非 null
     */
    @Test
    void getCopyOfContextMapEmpty() {
        Assertions.assertNotNull(mdc.getCopyOfContextMap());
        Assertions.assertTrue(mdc.getCopyOfContextMap().isEmpty());
    }

    /**
     * 正常路径：setContextMap(ConcurrentMap) 直接设置并生效
     */
    @Test
    void setContextMapConcurrent() {
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put("k", "v");
        mdc.setContextMap(map);
        Assertions.assertEquals("v", mdc.get("k"));
    }

    /**
     * 正常路径：setContextMap(普通 Map) 包装后生效
     */
    @Test
    void setContextMapNormal() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        mdc.setContextMap(map);
        Assertions.assertEquals("v", mdc.get("k"));
    }

    /**
     * 边界：setContextMap(null) 相当于清空
     */
    @Test
    void setContextMapNullClears() {
        mdc.put("k", "v");
        mdc.setContextMap(null);
        Assertions.assertNull(mdc.get("k"));
    }

    /**
     * 异常路径：put(null, val) 抛空指针异常
     */
    @Test
    void putNullKeyThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> mdc.put(null, "v"));
    }

    /**
     * 异常路径：put(key, null) 抛空指针异常（ConcurrentHashMap 不允许 null 值）
     */
    @Test
    void putNullValueThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> mdc.put("k", null));
    }

    /**
     * 异常路径：get(null) 抛空指针异常
     */
    @Test
    void getNullKeyThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> mdc.get(null));
    }
}
