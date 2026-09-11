package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Description: CSetTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「空 Set / 普通元素 / 枚举」三个维度组织。</li>
 *   <li>空 Set：of() 为空。</li>
 *   <li>普通元素：正例去重、null 过滤（含全 null 返回空）、不可变（add 抛异常）。</li>
 *   <li>枚举：正例元素齐全、不可变（add 抛异常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 null 过滤、不可变返回、枚举构造的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：去重、null 过滤、全 null、不可变断言。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：of() 空；普通元素去重、null 过滤、不可变；枚举正例与不可变。</li>
 *   <li>未覆盖：HashSet 无序性验证（非语义要点，未断言顺序）。</li>
 * </ul>
 * <h2>空 Set</h2>
 * <ul>
 *   <li>1.1 of()：返回空 Set（ofEmpty）</li>
 * </ul>
 * <h2>普通元素 Set</h2>
 * <ul>
 *   <li>2.1 正例：{@code ["a","b","a"]} 去重 → {@code {a,b}}（ofGeneric）</li>
 *   <li>2.2 null 过滤：{@code [null,"b",null]} → {@code {b}}；全 null → 空（ofGenericNullFiltered）</li>
 *   <li>2.3 不可变：add 抛 UnsupportedOperationException（ofGenericUnmodifiable）</li>
 * </ul>
 * <h2>枚举 Set</h2>
 * <ul>
 *   <li>3.1 正例：{@code {INSERT, DELETE}} 大小为 2 且包含两元素（ofEnum）</li>
 *   <li>3.2 不可变：add 抛 UnsupportedOperationException（ofEnumUnmodifiable）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CSetTests {

    /**
     * 对应测试用例 1.1：of()：返回空 Set
     */
    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CSet.<String>of().isEmpty());

    }

    /**
     * 对应测试用例 2.1：正例：{@code ["a","b","a"]} 去重 → {@code {a,b}}
     */
    @Test
    public void ofGeneric() {

        Set<String> set = CSet.of("a", "b", "a");
        Assertions.assertEquals(new HashSet<>(Arrays.asList("a", "b")), set);

    }

    /**
     * 对应测试用例 2.2：null 过滤：{@code [null,"b",null]} → {@code {b}}；全 null → 空
     */
    @Test
    public void ofGenericNullFiltered() {

        Set<String> set = CSet.of(null, "b", null);
        Assertions.assertEquals(new HashSet<>(Collections.singletonList("b")), set);

        Assertions.assertTrue(CSet.of((String) null, null).isEmpty());

    }

    /**
     * 对应测试用例 2.3：不可变：add 抛 UnsupportedOperationException
     */
    @Test
    public void ofGenericUnmodifiable() {

        Set<String> set = CSet.of("a", "b");
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> set.add("c"));

    }

    /**
     * 对应测试用例 3.1：正例：{@code {INSERT, DELETE}} 大小为 2 且包含两元素
     */
    @Test
    public void ofEnum() {

        Set<CDbOperateEnum> set = CSet.of(CDbOperateEnum.INSERT, CDbOperateEnum.DELETE);
        Assertions.assertEquals(2, set.size());
        Assertions.assertTrue(set.contains(CDbOperateEnum.INSERT));
        Assertions.assertTrue(set.contains(CDbOperateEnum.DELETE));

    }

    /**
     * 对应测试用例 3.2：不可变：add 抛 UnsupportedOperationException
     */
    @Test
    public void ofEnumUnmodifiable() {

        Set<CDbOperateEnum> set = CSet.of(CDbOperateEnum.INSERT, CDbOperateEnum.DELETE);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> set.add(CDbOperateEnum.UPDATE));

    }

}
