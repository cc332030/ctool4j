package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CListTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「空 List / 单元素 / 可变参数」三个维度组织。</li>
 *   <li>单元素：正例、单元素 null 返回空。</li>
 *   <li>可变参数：正例、null 过滤、全 null 返回空、空参、不可变断言。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 null 过滤、不可变返回的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：单元素、可变参数、null 过滤、全 null、不可变。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：of() 空；of(t) 单元素与单元素 null；of(T...) 正例、null 过滤、全 null、空参、不可变。</li>
 *   <li>未覆盖：无（覆盖了全部入口与边界）。</li>
 * </ul>
 * <h2>空 List</h2>
 * <ul>
 *   <li>1.1 of()：返回空 List（ofEmpty）</li>
 * </ul>
 * <h2>单元素 List</h2>
 * <ul>
 *   <li>2.1 正例：{@code of("a")} → {@code [a]}；单元素 null → 空（ofSingle）</li>
 * </ul>
 * <h2>可变参数 List</h2>
 * <ul>
 *   <li>3.1 正例：{@code of("a","b")} → {@code [a,b]}；{@code of(null,"b",null)} → {@code [b]}；全 null → 空；空参 → 空（ofVarargs）</li>
 *   <li>3.2 不可变：add 抛 UnsupportedOperationException（ofVarargsUnmodifiable）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CListTests {

    /**
     * 对应测试用例 1.1：of()：返回空 List
     */
    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CList.<String>of().isEmpty());

    }

    /**
     * 对应测试用例 2.1：正例：{@code of("a")} → {@code [a]}；单元素 null → 空
     */
    @Test
    public void ofSingle() {

        Assertions.assertEquals(Collections.singletonList("a"), CList.of("a"));
        Assertions.assertTrue(CList.of((String) null).isEmpty());

    }

    /**
     * 对应测试用例 3.1：正例：{@code of("a","b")} → {@code [a,b]}；{@code of(null,"b",null)} → {@code [b]}；全 null → 空；空参 → 空
     */
    @Test
    public void ofVarargs() {

        Assertions.assertEquals(Arrays.asList("a", "b"), CList.of("a", "b"));
        Assertions.assertEquals(Collections.singletonList("b"), CList.of(null, "b", null));
        Assertions.assertTrue(CList.of((String) null, null).isEmpty());
        Assertions.assertTrue(CList.of().isEmpty());

    }

    /**
     * 对应测试用例 3.2：不可变：add 抛 UnsupportedOperationException
     */
    @Test
    public void ofVarargsUnmodifiable() {

        List<String> list = CList.of("a", "b");
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> list.add("c"));

    }

}
