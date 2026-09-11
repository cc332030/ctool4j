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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「LinkedHashMap 收集（多个重载）/ 不可变语义 / 键冲突 / 有序 Set」多个维度组织。</li>
 *   <li>LinkedHashMap 各重载覆盖：仅键、键+冲突合并、键值、键值+冲突合并。</li>
 *   <li>不可变语义通过 {@code assertThrowsExactly(UnsupportedOperationException, () -&gt; map.put(...))} 精确断言。</li>
 *   <li>键冲突分支覆盖默认抛 IllegalStateException 与显式合并函数两条路径。</li>
 *   <li>toLinkedSet 覆盖去重与保持插入顺序。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对不可变收集（add/put 抛 UnsupportedOperationException）与键冲突（默认抛 IllegalStateException、显式 merge）的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：各重载正例、冲突分支、不可变断言、顺序保持。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：toUnmodifiableLinkedMap 四个重载正例、冲突默认抛异常、冲突 merge、不可变 put 抛异常；</li>
 *   <li>toLinkedSet 去重且保持顺序。</li>
 *   <li>未覆盖：toUnmodifiableList、toUnmodifiableSet 的收集（本批测试类未单列，属可选扩展）。</li>
 * </ul>
 * <h2>LinkedHashMap 收集（toUnmodifiableLinkedMap）</h2>
 * <ul>
 *   <li>1.1 仅键：{@code ["a","b","c"]} 收集，键值即元素、保持顺序（toUnmodifiableLinkedMapWithKey）</li>
 *   <li>1.2 键+冲突合并：{@code ["a","b","a"]} 收集，重复键合并为 {@code "aa"}（toUnmodifiableLinkedMapWithKeyAndMerge）</li>
 *   <li>1.3 键值：{@code ["a","b"]} 收集，值为 {@code key.toUpperCase()}（toUnmodifiableLinkedMapWithKeyValue）</li>
 *   <li>1.4 键值+冲突合并：{@code [1,2,3]} 收集，值为 {@code k*10}（toUnmodifiableLinkedMapWithKeyValueMerge）</li>
 * </ul>
 * <h2>不可变语义</h2>
 * <ul>
 *   <li>2.1 不可变：收集后 {@code put} 抛 UnsupportedOperationException（toUnmodifiableLinkedMapUnmodifiable）</li>
 * </ul>
 * <h2>键冲突</h2>
 * <ul>
 *   <li>3.1 默认冲突：重复键未提供 merge 抛 IllegalStateException（toUnmodifiableLinkedMapConflictKey）</li>
 * </ul>
 * <h2>有序 Set（toLinkedSet）</h2>
 * <ul>
 *   <li>4.1 去重且保持插入顺序：{@code ["a","b","a","c"]} → {@code [a,b,c]}（toLinkedSet）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CCollectorsTests {

    /**
     * 对应测试用例 1.1：仅键：{@code ["a","b","c"]} 收集，键值即元素、保持顺序
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
     * 对应测试用例 1.2：键+冲突合并：{@code ["a","b","a"]} 收集，重复键合并为 {@code "aa"}
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
     * 对应测试用例 1.3：键值：{@code ["a","b"]} 收集，值为 {@code key.toUpperCase()}
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
     * 对应测试用例 1.4：键值+冲突合并：{@code [1,2,3]} 收集，值为 {@code k*10}
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
     * 对应测试用例 2.1：不可变：收集后 {@code put} 抛 UnsupportedOperationException
     */
    @Test
    public void toUnmodifiableLinkedMapUnmodifiable() {

        Map<String, String> map = Stream.of("a")
                .collect(CCollectors.toUnmodifiableLinkedMap(k -> k));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("x", "y"));

    }

    /**
     * 对应测试用例 3.1：默认冲突：重复键未提供 merge 抛 IllegalStateException
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
     * 对应测试用例 4.1：去重且保持插入顺序：{@code ["a","b","a","c"]} → {@code [a,b,c]}
     */
    @Test
    public void toLinkedSet() {

        java.util.Set<String> set = Stream.of("a", "b", "a", "c")
                .collect(CCollectors.toLinkedSet());

        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("a", "b", "c")), set);
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), set.stream().collect(Collectors.toList()));

    }

}
