package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMapTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CMapTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CMap.<String, Integer>of().isEmpty());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void ofSingle() {

        Map<String, Integer> map = CMap.of("a", 1);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, map.get("a"));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void ofTwo() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2);
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertEquals(2, map.get("b"));

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void ofTwoUnmodifiable() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("c", 3));

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void ofThree() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2, "c", 3);
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals(3, map.get("c"));

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void ofThreeUnmodifiable() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2, "c", 3);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("d", 4));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void ofMapCopyUnmodifiable() {

        Map<String, Integer> source = new HashMap<>();
        source.put("a", 1);

        Map<String, Integer> map = CMap.of(source);
        // of(Map) 与 of 系列一致返回不可变副本
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("b", 2));
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, map.get("a"));
        // 原 map 不受影响
        Assertions.assertEquals(1, source.size());

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void ofNullMapReturnsEmpty() {

        Assertions.assertTrue(CMap.of((Map<String, Integer>) null).isEmpty());
        Assertions.assertTrue(CMap.of(new HashMap<>(), LinkedHashMap::new).isEmpty());

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void ofMapWithSupplier() {

        Map<String, Integer> source = new HashMap<>();
        source.put("a", 1);

        Map<String, Integer> map = CMap.of(source, LinkedHashMap::new);
        // supplier 指定内部容器；返回不可变副本（无法直接验证内部类型），验证内容与不可变
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("b", 2));

    }

}
