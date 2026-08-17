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

    @Test
    public void toUnmodifiableLinkedMapWithKeyAndMerge() {

        List<String> list = Arrays.asList("a", "b", "a");
        Map<String, String> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k, (v1, v2) -> v1 + v2));

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals("aa", map.get("a"));

    }

    @Test
    public void toUnmodifiableLinkedMapWithKeyValue() {

        List<String> list = Arrays.asList("a", "b");
        Map<String, String> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k, k -> k.toUpperCase()));

        Assertions.assertEquals("A", map.get("a"));
        Assertions.assertEquals("B", map.get("b"));

    }

    @Test
    public void toUnmodifiableLinkedMapWithKeyValueMerge() {

        List<Integer> list = Arrays.asList(1, 2, 3);
        Map<Integer, Integer> map = list.stream()
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k, k -> k * 10, (v1, v2) -> v1 + v2));

        Assertions.assertEquals(10, map.get(1));
        Assertions.assertEquals(20, map.get(2));
        Assertions.assertEquals(30, map.get(3));

    }

    @Test
    public void toUnmodifiableLinkedMapUnmodifiable() {

        Map<String, String> map = Stream.of("a")
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("x", "y"));

    }

    @Test
    public void toUnmodifiableLinkedMapConflictKey() {

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
                Stream.of("a", "a")
                        .collect(CCollectors.toUnmodifiableLinkedMap(
                            k -> k,
                            k -> k)));

    }

    @Test
    public void toLinkedSet() {

        java.util.Set<String> set = Stream.of("a", "b", "a", "c")
                .collect(CCollectors.toLinkedSet());

        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("a", "b", "c")), set);
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), set.stream().collect(Collectors.toList()));

    }

}
