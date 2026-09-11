package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.interfaces.ICTest;
import com.c332030.ctool4j.core.util.CTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: ICTestTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>用一个匿名实现验证 {@code isTest()} 委托 {@code CTestUtils.isTest()}，结果一致。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对委托 CTestUtils 的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：isTest() 与 CTestUtils.isTest() 一致（测试环境为 true）。</li>
 *   <li>未覆盖：生产环境（非测试 classpath）分支（依赖运行环境）。</li>
 * </ul>
 * <h2>测试环境判断</h2>
 * <ul>
 *   <li>1.1 isTest：实现类 isTest() 与 CTestUtils.isTest() 一致（isTest）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class ICTestTests {

    /**
     * 对应测试用例 1.1：实现类 isTest() 与 CTestUtils.isTest() 一致
     */
    @Test
    public void isTest() {

        ICTest impl = new ICTest() {
        };
        Assertions.assertEquals(CTestUtils.isTest(), impl.isTest());

    }

}
