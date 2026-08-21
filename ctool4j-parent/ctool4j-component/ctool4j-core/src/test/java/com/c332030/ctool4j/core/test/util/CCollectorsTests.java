package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CCollectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Description: CCollectorsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CCollectorsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void toUnmodifiableLinkedMapWithKey() {

        List<String> list = Arrays.asList("a", "b", "c");
        Map<String, String> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k));

        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals("a", map.get("a"));
        Assertions.assertEquals("b", map.get("b"));
        Assertions.assertEquals("c", map.get("c"));
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), map.keySet().stream().collect(Collectors.toList()));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void toUnmodifiableLinkedMapWithKeyAndMerge() {

        List<String> list = Arrays.asList("a", "b", "a");
        Map<String, String> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k, (v1, v2) -> v1 + v2));

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals("aa", map.get("a"));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void toUnmodifiableLinkedMapWithKeyValue() {

        List<String> list = Arrays.asList("a", "b");
        Map<String, String> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k, k -> k.toUpperCase()));

        Assertions.assertEquals("A", map.get("a"));
        Assertions.assertEquals("B", map.get("b"));

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void toUnmodifiableLinkedMapWithKeyValueMerge() {

        List<Integer> list = Arrays.asList(1, 2, 3);
        Map<Integer, Integer> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k, k -> k * 10, (v1, v2) -> v1 + v2));

        Assertions.assertEquals(10, map.get(1));
        Assertions.assertEquals(20, map.get(2));
        Assertions.assertEquals(30, map.get(3));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toUnmodifiableLinkedMapUnmodifiable() {

        Map<String, String> map = Stream.of("a")
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("x", "y"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void toUnmodifiableLinkedMapConflictKey() {

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
                Stream.of("a", "a")
                        .collect(CCollectors.toUnmodifiableLinkedMap(
                            k -> k,
                            k -> k)));

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void toLinkedSet() {

        java.util.Set<String> set = Stream.of("a", "b", "a", "c")
                .collect(CCollectors.toLinkedSet());

        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("a", "b", "c")), set);
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), set.stream().collect(Collectors.toList()));

    }

}
