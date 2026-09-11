package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CIteratorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CIteratorUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「遍历消费 / null 处理 / 异常忽略 / 空集合」多个维度组织。</li>
 *   <li>遍历消费：正常逐元素收集。</li>
 *   <li>null 处理：null iterable 不执行、集合中 null 元素被跳过。</li>
 *   <li>异常忽略：consumer 抛异常被忽略，其他元素继续处理。</li>
 *   <li>空集合：无元素收集为空。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对忽略异常、null 跳过的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：正常遍历、null 输入、null 元素、异常、空集合。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：Iterable 正常遍历；null iterable 不执行；null 元素跳过；consumer 异常忽略且继续；</li>
 *   <li>空集合无消费。</li>
 *   <li>未覆盖：Iterator 入口（Iterable 重载内部委托，行为一致）、异常被 debug 日志记录的具体断言</li>
 *   <li>（日志不易断言，未单列）。</li>
 * </ul>
 * <h2>遍历消费</h2>
 * <ul>
 *   <li>1.1 正常遍历：{@code [a,b,c]} 全部收集（forEachIgnoreExceptionByIterable）</li>
 *   <li>1.2 空集合：无元素收集（forEachIgnoreExceptionEmpty）</li>
 * </ul>
 * <h2>null 处理</h2>
 * <ul>
 *   <li>2.1 null iterable：直接返回，consumer 不执行（forEachIgnoreExceptionNullIterable）</li>
 *   <li>2.2 null 元素：{@code [a,null,b]} → 收集 {@code [a,b]}（forEachIgnoreExceptionNullValueSkipped）</li>
 * </ul>
 * <h2>异常忽略</h2>
 * <ul>
 *   <li>3.1 consumer 抛异常：{@code "b"} 抛异常被忽略，{@code a}/{@code c} 继续处理（forEachIgnoreExceptionConsumerExceptionSwallowed）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CIteratorUtilsTests {

    /**
     * 对应测试用例 1.1：正常遍历：{@code [a,b,c]} 全部收集
     */
    @Test
    public void forEachIgnoreExceptionByIterable() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Arrays.asList("a", "b", "c"), collected::add);

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), collected);

    }

    /**
     * 对应测试用例 2.1：null iterable：直接返回，consumer 不执行
     */
    @Test
    public void forEachIgnoreExceptionNullIterable() {

        CIteratorUtils.forEachIgnoreException((Iterable<String>) null, s -> {
            throw new AssertionError("不应执行");
        });
        // 不抛异常即通过

    }

    /**
     * 对应测试用例 2.2：null 元素：{@code [a,null,b]} → 收集 {@code [a,b]}
     */
    @Test
    public void forEachIgnoreExceptionNullValueSkipped() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Arrays.asList("a", null, "b"), collected::add);

        Assertions.assertEquals(Arrays.asList("a", "b"), collected);

    }

    /**
     * 对应测试用例 3.1：consumer 抛异常：{@code "b"} 抛异常被忽略，{@code a}/{@code c} 继续处理
     */
    @Test
    public void forEachIgnoreExceptionConsumerExceptionSwallowed() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Arrays.asList("a", "b", "c"), s -> {
            if ("b".equals(s)) {
                throw new IllegalStateException("boom");
            }
            collected.add(s);
        });

        // b 抛异常被忽略，a/c 继续处理
        Assertions.assertEquals(Arrays.asList("a", "c"), collected);

    }

    /**
     * 对应测试用例 1.2：空集合：无元素收集
     */
    @Test
    public void forEachIgnoreExceptionEmpty() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Collections.<String>emptyList(), collected::add);

        Assertions.assertTrue(collected.isEmpty());

    }

}
