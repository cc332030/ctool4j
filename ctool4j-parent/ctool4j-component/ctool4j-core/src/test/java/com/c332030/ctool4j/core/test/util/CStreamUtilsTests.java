package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CStreamUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Description: CStreamUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「去重正例 / null key 过滤 / 单元素」三个维度组织。</li>
 *   <li>去重正例：重复元素只保留首个，验证去重结果与顺序。</li>
 *   <li>null key：key 提取返回 null 的元素被全部过滤（结果为空）。</li>
 *   <li>单元素：单元素流不被误过滤。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对去重语义与 null key 过滤的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：重复元素、null key、单元素边界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：去重正例（含重复元素、保持首次出现顺序）、null key 过滤、单元素保留。</li>
 *   <li>未覆盖：并发环境下的多线程去重竞态（ConcurrentHashMap 保证，单测不构造并发场景）。</li>
 * </ul>
 * <h2>去重</h2>
 * <ul>
 *   <li>1.1 正例：{@code ["a","b","a","c","b"]} 去重 → {@code [a,b,c]}（distinctByKey）</li>
 *   <li>1.2 边界：key 均为 null 时全部过滤，结果为空（distinctByKeyNullKeyExcluded）</li>
 *   <li>1.3 边界：单元素流不被过滤（distinctByKeySingleElement）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CStreamUtilsTests {

    /**
     * 对应测试用例 1.1：正例：{@code ["a","b","a","c","b"]} 去重 → {@code [a,b,c]}
     */
    @Test
    public void distinctByKey() {

        List<String> result = Stream.of("a", "b", "a", "c", "b")
                .filter(CStreamUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), result);

    }

    /**
     * 对应测试用例 1.2：边界：key 均为 null 时全部过滤，结果为空
     */
    @Test
    public void distinctByKeyNullKeyExcluded() {

        List<String> result = Stream.of("a", "b")
                .filter(CStreamUtils.distinctByKey(s -> null))
                .collect(Collectors.toList());

        Assertions.assertTrue(result.isEmpty());

    }

    /**
     * 对应测试用例 1.3：边界：单元素流不被过滤
     */
    @Test
    public void distinctByKeySingleElement() {

        List<String> result = Stream.of("only")
                .filter(CStreamUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());

        Assertions.assertEquals(Collections.singletonList("only"), result);

    }

}
