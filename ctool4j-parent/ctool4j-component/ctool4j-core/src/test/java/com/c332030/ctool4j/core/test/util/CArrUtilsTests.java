package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CArrUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CArrUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「方法」维度组织分类（filter / get / convert / getArr / toStrArr / first），每个方法下覆盖正例、空入参边界与反例。</li>
 *   <li>每个方法都覆盖空数组/null 入参的边界，验证与设计约定一致。</li>
 *   <li>{@code get} 额外覆盖负索引、正负越界分支（Q12 修复：负索引越界返回 null 不抛异常），为该方法的分支重点。</li>
 *   <li>{@code convert} 分别覆盖两参（Object[]）与三参（类型化数组）两个入口。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各方法的空入参边界约定（filter 返回空列表、get/convert/first 返回 null、toStrArr 返回空数组）。</li>
 *   <li>依据功能设计对 {@code get} 负索引与越界返回 null 的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：典型值、空数组、null、索引 0/末位/越界、负索引。</li>
 *   <li>{@code get} 的分支（正向/负向/越界）为判定覆盖重点。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：filter 正例/无命中/空数组/null；filterNull、filterString 去空；get 正负索引/正负越界/null/空数组；</li>
 *   <li>convert 两参三参正例/null；getArr 正例/空；toStrArr 正例/null/空集合；first 正例/null/空数组。</li>
 *   <li>未覆盖：类型化数组 convert 的空数组入参（三参对空数组返回 null，与 null 行为一致，未单列）。</li>
 * </ul>
 * <h2>过滤（filter / filterNull / filterString）</h2>
 * <ul>
 *   <li>1.1 正例：按 predicate 过滤出符合元素（filter）</li>
 *   <li>1.2 反例：无元素符合 predicate 返回空列表（filter）</li>
 *   <li>1.3 边界：null 数组返回空列表（filter）</li>
 *   <li>1.4 边界：空数组返回空列表（filter）</li>
 *   <li>1.5 正例：filterNull 过滤 null 元素（filterNull）</li>
 *   <li>1.6 边界：filterNull null 数组返回空列表（filterNull）</li>
 *   <li>1.7 正例：filterString 过滤空白/null 元素（filterString）</li>
 *   <li>1.8 边界：filterString null 数组返回空列表（filterString）</li>
 * </ul>
 * <h2>取元素（get）</h2>
 * <ul>
 *   <li>2.1 正例：正索引取首/末元素（get）</li>
 *   <li>2.2 边界：负索引 -1/-2 从末尾倒数取值（get）</li>
 *   <li>2.3 边界：正索引越界返回 null（get）</li>
 *   <li>2.4 边界：null 数组返回 null（get）</li>
 *   <li>2.5 边界：空数组返回 null（get）</li>
 *   <li>2.6 边界：负索引越界（index &lt; -length）返回 null，不抛数组越界异常（getNegativeIndexOutOfRangeReturnsNull，Q12 修复）</li>
 * </ul>
 * <h2>转换（convert）</h2>
 * <ul>
 *   <li>3.1 正例：两参 convert 转 Object[]（convert）</li>
 *   <li>3.2 边界：两参 convert null 数组返回 null（convert）</li>
 *   <li>3.3 正例：三参 convert 转类型化数组 String[]（convertToTypedArray）</li>
 *   <li>3.4 边界：三参 convert null 数组返回 null（convertToTypedArray）</li>
 * </ul>
 * <h2>泛型数组（getArr）</h2>
 * <ul>
 *   <li>4.1 正例：可变参数构造数组（getArr）</li>
 *   <li>4.2 边界：无参调用返回空数组（getArr）</li>
 * </ul>
 * <h2>集合转数组（toStrArr）</h2>
 * <ul>
 *   <li>5.1 正例：字符串集合转字符串数组（toStrArr）</li>
 *   <li>5.2 边界：null 集合返回空数组（toStrArr）</li>
 *   <li>5.3 边界：空集合返回空数组（toStrArr）</li>
 * </ul>
 * <h2>首元素（first）</h2>
 * <ul>
 *   <li>6.1 正例：返回首元素（first）</li>
 *   <li>6.2 边界：null 数组返回 null（first）</li>
 *   <li>6.3 边界：空数组返回 null（first）</li>
 * </ul>
 *
 * @since 2025/9/10
 * @version 1.0
 */
public class CArrUtilsTests {

    /**
     * 对应测试用例 1.1 / 1.2 / 1.3 / 1.4
     */
    @Test
    public void filter() {

        List<Integer> result = CArrUtils.filter(new Integer[]{1, 2, 3, 4}, e -> e % 2 == 0);
        Assertions.assertEquals(Arrays.asList(2, 4), result);

        Assertions.assertEquals(0, CArrUtils.filter(new Integer[]{1, 2, 3, 4}, e -> e > 10).size());
        Assertions.assertEquals(0, CArrUtils.filter(null, e -> true).size());
        Assertions.assertEquals(0, CArrUtils.filter(new Integer[0], e -> true).size());

    }

    /**
     * 对应测试用例 1.5 / 1.6
     */
    @Test
    public void filterNull() {

        List<String> result = CArrUtils.filterNull(new String[]{"a", null, "b", null});
        Assertions.assertEquals(Arrays.asList("a", "b"), result);

        Assertions.assertEquals(0, CArrUtils.filterNull(null).size());

    }

    /**
     * 对应测试用例 1.7 / 1.8
     */
    @Test
    public void filterString() {

        List<String> result = CArrUtils.filterString(new String[]{" a ", "", null, " ", "b"});
        Assertions.assertEquals(Arrays.asList(" a ", "b"), result);

        Assertions.assertEquals(0, CArrUtils.filterString(null).size());

    }

    /**
     * 对应测试用例 2.1 / 2.2 / 2.3 / 2.4 / 2.5
     */
    @Test
    public void get() {

        String[] arr = {"a", "b", "c"};

        Assertions.assertEquals("a", CArrUtils.get(arr, 0));
        Assertions.assertEquals("c", CArrUtils.get(arr, 2));
        Assertions.assertEquals("c", CArrUtils.get(arr, -1));
        Assertions.assertEquals("b", CArrUtils.get(arr, -2));
        Assertions.assertNull(CArrUtils.get(arr, 3));
        Assertions.assertNull(CArrUtils.get(null, 0));
        Assertions.assertNull(CArrUtils.get(new String[0], 0));

    }

    /**
     * 对应测试用例 2.6：边界：负索引越界（index &lt; -length）返回 null，不抛数组越界异常（getNegativeIndexOutOfRangeReturnsNull，Q12 修复）
     */
    @Test
    public void getNegativeIndexOutOfRangeReturnsNull() {

        String[] arr = {"a", "b", "c"};

        // Q12 修复：负索引越界（index < -length）视为无值返回 null，不抛数组越界异常
        Assertions.assertNull(CArrUtils.get(arr, -4));

    }

    /**
     * 对应测试用例 3.1 / 3.2
     */
    @Test
    public void convert() {

        Object[] result = CArrUtils.convert(new Integer[]{1, 2, 3}, String::valueOf);
        Assertions.assertArrayEquals(new Object[]{"1", "2", "3"}, result);

        Assertions.assertNull(CArrUtils.convert(null, String::valueOf));

    }

    /**
     * 对应测试用例 3.3 / 3.4
     */
    @Test
    public void convertToTypedArray() {

        String[] result = CArrUtils.convert(new Integer[]{1, 2, 3}, String[]::new, String::valueOf);
        Assertions.assertArrayEquals(new String[]{"1", "2", "3"}, result);

        Assertions.assertNull(CArrUtils.convert(null, String[]::new, String::valueOf));

    }

    /**
     * 对应测试用例 4.1 / 4.2
     */
    @Test
    public void getArr() {

        Integer[] arr = CArrUtils.getArr(1, 2, 3);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, arr);

        Assertions.assertEquals(0, CArrUtils.getArr().length);

    }

    /**
     * 对应测试用例 5.1 / 5.2 / 5.3
     */
    @Test
    public void toStrArr() {

        String[] result = CArrUtils.toStrArr(Arrays.asList("a", "b"));
        Assertions.assertArrayEquals(new String[]{"a", "b"}, result);

        Assertions.assertEquals(0, CArrUtils.toStrArr(null).length);
        Assertions.assertEquals(0, CArrUtils.toStrArr(Collections.emptyList()).length);

    }

    /**
     * 对应测试用例 6.1 / 6.2 / 6.3
     */
    @Test
    public void first() {

        Assertions.assertEquals("a", CArrUtils.first(new String[]{"a", "b"}));
        Assertions.assertNull(CArrUtils.first(null));
        Assertions.assertNull(CArrUtils.first(new String[0]));

    }

}
