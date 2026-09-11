package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTestUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「isTest / isNotTest」两个维度组织。</li>
 *   <li>测试环境 classpath 包含 JUnit，{@code CTool4jTestConstants.IS_TEST} 恒为 true，故断言 isTest() 为 true、</li>
 *   <li>isNotTest() 为 false。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对判断依据（IS_TEST 常量）的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：isTest() 为 true、isNotTest() 为 false（测试环境下）。</li>
 *   <li>未覆盖：生产环境（非测试 classpath）下 isTest() 为 false 的分支（依赖运行环境，单测无法覆盖）。</li>
 * </ul>
 * <h2>环境判断</h2>
 * <ul>
 *   <li>1.1 isTest：测试环境为 true（isTest）</li>
 *   <li>1.2 isNotTest：测试环境为 false（isNotTest）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CTestUtilsTests {

    /**
     * 对应测试用例 1.1：测试环境为 true
     */
    @Test
    public void isTest() {

        // 测试环境 classpath 包含 JUnit，IS_TEST 恒为 true
        Assertions.assertTrue(CTestUtils.isTest());

    }

    /**
     * 对应测试用例 1.2：测试环境为 false
     */
    @Test
    public void isNotTest() {

        Assertions.assertFalse(CTestUtils.isNotTest());

    }

}
