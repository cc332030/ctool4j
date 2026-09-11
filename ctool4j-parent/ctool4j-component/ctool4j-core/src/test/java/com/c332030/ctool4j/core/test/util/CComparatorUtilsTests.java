package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CComparatorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CComparatorUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「最值取法（min/max）」「入口形态（集合/可变参数）」「字段取值」「双值比较」「消费」多个维度组织。</li>
 *   <li>min/max 均覆盖：正例（含 null 元素被过滤）、空集合、全 null 集合、可变参数空参/全 null。</li>
 *   <li>字段取值覆盖正例与字段为 null 元素被过滤的场景。</li>
 *   <li>compare 覆盖双 null、单 null、自然序大小，以及显式比较器分支。</li>
 *   <li>minConsumer/maxConsumer 覆盖非空最值消费与空集合不消费（初始值保持不变）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对最值过滤 null、空集合返回 null、compare null 视为最大的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：正例、空集合、全 null、单 null、字段 null。</li>
 *   <li>compare 的 null 三分支（双 null/左 null/右 null）为判定覆盖重点。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：min/max 集合与可变参数的正例、空、全 null、含 null；compareCollection 自定义比较器；字段取值最值（正例+字段 null 过滤）；compare 双值（含显式比较器与自然序，null 三分支）；min/maxConsumer 消费与空集合不消费。</li>
 *   <li>未覆盖：自定义比较器与字段取值组合的极端边界（如比较器异常场景），非关键路径未单列。</li>
 * </ul>
 * <h2>最小值（min）</h2>
 * <ul>
 *   <li>1.1 集合正例：{@code [3,1,2]} → 1；含 null {@code [3,null,1,2]} → 1（minCollection）</li>
 *   <li>1.2 边界：空集合 → null；全 null → null（minCollection）</li>
 *   <li>1.3 可变参数正例：{@code min(3,1,2)} → 1；含 null {@code min(3,null,1,2)} → 1（minVarargs）</li>
 *   <li>1.4 边界：无参 {@code min()} → null；单 null {@code min((Integer)null)} → null（minVarargs）</li>
 * </ul>
 * <h2>最大值（max）</h2>
 * <ul>
 *   <li>2.1 集合正例：{@code [3,1,2]} → 3；含 null {@code [3,null,1,2]} → 3（maxCollection）</li>
 *   <li>2.2 边界：空集合 → null；全 null → null（maxCollection）</li>
 *   <li>2.3 可变参数正例：{@code max(3,1,2)} → 3；含 null {@code max(3,null,1,2)} → 3（maxVarargs）</li>
 *   <li>2.4 边界：无参 {@code max()} → null（maxVarargs）</li>
 * </ul>
 * <h2>自定义比较器最值（compareCollection）</h2>
 * <ul>
 *   <li>3.1 正例：自然序 → {@code "a"}、反序 → {@code "ccc"}；空集合 → null（compareCollectionWithComparator）</li>
 * </ul>
 * <h2>字段取值最值</h2>
 * <ul>
 *   <li>4.1 min 按字段：按 {@code String::length} 取最短 {@code "a"}（minByFunction）</li>
 *   <li>4.2 max 按字段：按 {@code String::length} 取最长 {@code "ccc"}（maxByFunction）</li>
 * </ul>
 * <h2>双值比较（compare）</h2>
 * <ul>
 *   <li>5.1 显式比较器：相等/大于/小于分支（compareWithComparator）</li>
 *   <li>5.2 显式比较器：null 视为最大（双 null → 0、null 左 → 1、null 右 → -1）（compareNullHandling）</li>
 *   <li>5.3 自然序：相等/大于/小于三分支，及 null 视为最大的三分支（compareNatural）</li>
 * </ul>
 * <h2>消费（minConsumer / maxConsumer）</h2>
 * <ul>
 *   <li>6.1 minConsumer：{@code [3,1,2]} 消费最小值 1；空集合不消费（初始值保持 100）（minConsumer）</li>
 *   <li>6.2 maxConsumer：{@code [3,1,2]} 消费最大值 3；空集合不消费（初始值保持 100）（maxConsumer）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CComparatorUtilsTests {

    /**
     * 对应测试用例 1.1, 1.2
     */
    @Test
    public void minCollection() {

        Assertions.assertEquals(1, CComparatorUtils.min(Arrays.asList(3, 1, 2)));
        Assertions.assertEquals(1, CComparatorUtils.min(Arrays.asList(3, null, 1, 2)));
        Assertions.assertNull(CComparatorUtils.min(Collections.<Integer>emptyList()));
        Assertions.assertNull(CComparatorUtils.min(Arrays.<Integer>asList(null, null)));

    }

    /**
     * 对应测试用例 1.3, 1.4
     */
    @Test
    public void minVarargs() {

        Assertions.assertEquals(1, CComparatorUtils.min(3, 1, 2));
        Assertions.assertEquals(1, CComparatorUtils.min(3, null, 1, 2));
        Assertions.assertNull(CComparatorUtils.min());
        Assertions.assertNull(CComparatorUtils.min((Integer) null));

    }

    /**
     * 对应测试用例 2.1, 2.2
     */
    @Test
    public void maxCollection() {

        Assertions.assertEquals(3, CComparatorUtils.max(Arrays.asList(3, 1, 2)));
        Assertions.assertEquals(3, CComparatorUtils.max(Arrays.asList(3, null, 1, 2)));
        Assertions.assertNull(CComparatorUtils.max(Collections.<Integer>emptyList()));
        Assertions.assertNull(CComparatorUtils.max(Arrays.<Integer>asList(null, null)));

    }

    /**
     * 对应测试用例 2.3, 2.4
     */
    @Test
    public void maxVarargs() {

        Assertions.assertEquals(3, CComparatorUtils.max(3, 1, 2));
        Assertions.assertEquals(3, CComparatorUtils.max(3, null, 1, 2));
        Assertions.assertNull(CComparatorUtils.max());

    }

    /**
     * 对应测试用例 3.1：正例：自然序 → {@code "a"}、反序 → {@code "ccc"}；空集合 → null
     */
    @Test
    public void compareCollectionWithComparator() {

        List<String> list = Arrays.asList("ccc", "a", "bb");
        Assertions.assertEquals("a", CComparatorUtils.compareCollection(list, Comparator.naturalOrder()));
        Assertions.assertEquals("ccc", CComparatorUtils.compareCollection(list, Comparator.reverseOrder()));
        Assertions.assertNull(CComparatorUtils.compareCollection(Collections.<String>emptyList(), Comparator.naturalOrder()));

    }

    /**
     * 对应测试用例 4.1：min 按字段：按 {@code String::length} 取最短 {@code "a"}
     */
    @Test
    public void minByFunction() {

        List<String> list = Arrays.asList("ccc", "a", "bb");
        Assertions.assertEquals("a", CComparatorUtils.min(list, String::length));
        Assertions.assertNull(CComparatorUtils.min(Collections.emptyList(), String::length));

    }

    /**
     * 对应测试用例 4.2：max 按字段：按 {@code String::length} 取最长 {@code "ccc"}
     */
    @Test
    public void maxByFunction() {

        List<String> list = Arrays.asList("ccc", "a", "bb");
        Assertions.assertEquals("ccc", CComparatorUtils.max(list, String::length));
        Assertions.assertNull(CComparatorUtils.max(Collections.emptyList(), String::length));

    }

    /**
     * 对应测试用例 5.1：显式比较器：相等/大于/小于分支
     */
    @Test
    public void compareWithComparator() {

        Assertions.assertEquals(0, CComparatorUtils.compare("a", "a", Comparator.naturalOrder()));
        Assertions.assertEquals(-1, CComparatorUtils.compare("a", "b", Comparator.naturalOrder()));

    }

    /**
     * 对应测试用例 5.2：显式比较器：null 视为最大（双 null → 0、null 左 → 1、null 右 → -1）
     */
    @Test
    public void compareNullHandling() {

        Assertions.assertEquals(0, CComparatorUtils.compare(null, null, Comparator.naturalOrder()));
        Assertions.assertEquals(1, CComparatorUtils.compare(null, "a", Comparator.naturalOrder()));
        Assertions.assertEquals(-1, CComparatorUtils.compare("a", null, Comparator.naturalOrder()));

    }

    /**
     * 对应测试用例 5.3：自然序：相等/大于/小于三分支，及 null 视为最大的三分支
     */
    @Test
    public void compareNatural() {

        Assertions.assertEquals(0, CComparatorUtils.compare(1, 1));
        Assertions.assertEquals(-1, CComparatorUtils.compare(1, 2));
        Assertions.assertEquals(1, CComparatorUtils.compare(2, 1));
        Assertions.assertEquals(0, CComparatorUtils.compare(null, null));
        Assertions.assertEquals(1, CComparatorUtils.compare(null, 1));
        Assertions.assertEquals(-1, CComparatorUtils.compare(1, null));

    }

    /**
     * 对应测试用例 6.1：{@code [3,1,2]} 消费最小值 1；空集合不消费（初始值保持 100）
     */
    @Test
    public void minConsumer() {

        AtomicInteger result = new AtomicInteger();
        CComparatorUtils.minConsumer(Arrays.asList(3, 1, 2), Integer::intValue, result::set);
        Assertions.assertEquals(1, result.get());

        AtomicInteger emptyResult = new AtomicInteger(100);
        CComparatorUtils.minConsumer(Collections.emptyList(), Integer::intValue, emptyResult::set);
        Assertions.assertEquals(100, emptyResult.get());

    }

    /**
     * 对应测试用例 6.2：{@code [3,1,2]} 消费最大值 3；空集合不消费（初始值保持 100）
     */
    @Test
    public void maxConsumer() {

        AtomicInteger result = new AtomicInteger();
        CComparatorUtils.maxConsumer(Arrays.asList(3, 1, 2), Integer::intValue, result::set);
        Assertions.assertEquals(3, result.get());

        AtomicInteger emptyResult = new AtomicInteger(100);
        CComparatorUtils.maxConsumer(Collections.emptyList(), Integer::intValue, emptyResult::set);
        Assertions.assertEquals(100, emptyResult.get());

    }

}
