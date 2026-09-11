package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CPageUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CPageUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「默认常量 / pageThenDo / pageThenEach」三个维度组织。</li>
 *   <li>默认常量断言三个分页大小值。</li>
 *   <li>pageThenDo 覆盖两个终止条件分支：queryFunction 返回 null、doSth 返回 false。</li>
 *   <li>pageThenEach 覆盖正常逐页处理（含空集合作为末页终止）与首页即空的情况。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对终止条件（null 结果 / doSth false / 空集合）的约定。</li>
 *   <li>依据测试方法（分支覆盖/边界值）：两种终止分支、末页空集合、首页空。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认常量；pageThenDo null 结果终止、doSth false 终止；pageThenEach 正常分页消费、</li>
 *   <li>首页空即终止。</li>
 *   <li>未覆盖：doSth 返回 null 导致结束的分支（与 false 语义一致，未单列）。</li>
 * </ul>
 * <h2>默认分页常量</h2>
 * <ul>
 *   <li>1.1 默认值：10 / 100 / 1000（defaultPageSizes）</li>
 * </ul>
 * <h2>分页执行（pageThenDo）</h2>
 * <ul>
 *   <li>2.1 终止分支：queryFunction 返回 null 结束（页号 1,2,3）（pageThenDoStopsWhenResultNull）</li>
 *   <li>2.2 终止分支：doSth 返回 false 结束（页号 1,2）（pageThenDoStopsWhenDoSthFalse）</li>
 * </ul>
 * <h2>分页逐元素执行（pageThenEach）</h2>
 * <ul>
 *   <li>3.1 正常分页：逐页消费 {@code [a,b]}、{@code [c]}，空集合作为末页终止（页号 1,2,3）（pageThenEach）</li>
 *   <li>3.2 首页空：首页返回空集合即终止，无消费（pageThenEachFirstPageEmpty）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CPageUtilsTests {

    /**
     * 对应测试用例 1.1：默认值：10 / 100 / 1000
     */
    @Test
    public void defaultPageSizes() {

        Assertions.assertEquals(10, CPageUtils.DEFAULT_PAGE_SIZE);
        Assertions.assertEquals(100, CPageUtils.DEFAULT_JOB_PAGE_SIZE);
        Assertions.assertEquals(1000, CPageUtils.DEFAULT_EXPORT_PAGE_SIZE);

    }

    /**
     * 对应测试用例 2.1：终止分支：queryFunction 返回 null 结束（页号 1,2,3）
     */
    @Test
    public void pageThenDoStopsWhenResultNull() {

        List<Integer> pages = new ArrayList<>();
        CPageUtils.pageThenDo(
                page -> {
                    pages.add(page);
                    return page < 3 ? "data" : null;
                },
                data -> true);

        Assertions.assertEquals(Arrays.asList(1, 2, 3), pages);

    }

    /**
     * 对应测试用例 2.2：终止分支：doSth 返回 false 结束（页号 1,2）
     */
    @Test
    public void pageThenDoStopsWhenDoSthFalse() {

        List<Integer> pages = new ArrayList<>();
        CPageUtils.pageThenDo(
                page -> {
                    pages.add(page);
                    return page == 2 ? "stop" : "go";
                },
                data -> !"stop".equals(data));

        Assertions.assertEquals(Arrays.asList(1, 2), pages);

    }

    /**
     * 对应测试用例 3.1：正常分页：逐页消费 {@code [a,b]}、{@code [c]}，空集合作为末页终止（页号 1,2,3）
     */
    @Test
    public void pageThenEach() {

        List<String> consumed = new ArrayList<>();
        List<Integer> pages = new ArrayList<>();
        CPageUtils.pageThenEach(
                page -> {
                    pages.add(page);
                    if (page == 1) {
                        return Arrays.asList("a", "b");
                    }
                    if (page == 2) {
                        return Collections.singletonList("c");
                    }
                    return Collections.emptyList();
                },
                consumed::add);

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), consumed);
        Assertions.assertEquals(Arrays.asList(1, 2, 3), pages);

    }

    /**
     * 对应测试用例 3.2：首页空：首页返回空集合即终止，无消费
     */
    @Test
    public void pageThenEachFirstPageEmpty() {

        List<String> consumed = new ArrayList<>();
        CPageUtils.pageThenEach(
                page -> Collections.<String>emptyList(),
                consumed::add);

        Assertions.assertTrue(consumed.isEmpty());

    }

}
