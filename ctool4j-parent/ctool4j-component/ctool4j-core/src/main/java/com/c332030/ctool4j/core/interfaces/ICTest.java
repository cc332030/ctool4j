package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CTestUtils;

/**
 * <p>
 * Description: ICTest
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICTest} 为测试环境判断接口，提供默认方法：</p>
 * <ul>
 *   <li>{@code isTest()}：是否处于测试环境（委托 {@code CTestUtils.isTest()}）</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要判断测试环境的接口/类，实现 {@code ICTest} 即获得统一 isTest 语义。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>判断依赖 {@code CTestUtils.isTest()}，无法细分环境类型。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>统一委托 CTestUtils，保证环境判断语义一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认实现</b></p>
 * <ul>
 *   <li>{@code isTest()} 直接委托 {@code CTestUtils.isTest()}，基于 classpath 常量判断（测试环境含 JUnit 时为 true）。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public interface ICTest {

    /**
     * 是否是测试
     * @return 结果
     */
    default boolean isTest() {
        return CTestUtils.isTest();
    }

}
