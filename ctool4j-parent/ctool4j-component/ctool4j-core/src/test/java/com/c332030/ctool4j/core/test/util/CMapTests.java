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

    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CMap.<String, Integer>of().isEmpty());

    }

    @Test
    public void ofSingle() {

        Map<String, Integer> map = CMap.of("a", 1);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, map.get("a"));

    }

    @Test
    public void ofTwo() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2);
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertEquals(2, map.get("b"));

    }

    @Test
    public void ofTwoUnmodifiable() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("c", 3));

    }

    @Test
    public void ofThree() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2, "c", 3);
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals(3, map.get("c"));

    }

    @Test
    public void ofThreeUnmodifiable() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2, "c", 3);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("d", 4));

    }

    @Test
    public void ofMapCopyModifiable() {

        Map<String, Integer> source = new HashMap<>();
        source.put("a", 1);

        Map<String, Integer> map = CMap.of(source);
        map.put("b", 2);

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(2, map.get("b"));
        // 原 map 不受影响
        Assertions.assertEquals(1, source.size());

    }

    @Test
    public void ofNullMapReturnsEmpty() {

        Assertions.assertTrue(CMap.of((Map<String, Integer>) null).isEmpty());
        Assertions.assertTrue(CMap.of(new HashMap<>(), LinkedHashMap::new).isEmpty());

    }

    @Test
    public void ofMapWithSupplier() {

        Map<String, Integer> source = new HashMap<>();
        source.put("a", 1);

        Map<String, Integer> map = CMap.of(source, LinkedHashMap::new);
        Assertions.assertInstanceOf(LinkedHashMap.class, map);
        Assertions.assertEquals(1, map.get("a"));

    }

}
