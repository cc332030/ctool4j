package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.constant.CTool4jTestConstants;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CTestUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTestUtils} 为测试环境判断工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>测试环境</td>
 *     <td>isTest() 为 true，isNotTest() 为 false</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要区分测试/生产环境行为的代码路径（如日志级别、Mock 数据注入）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>基于 classpath 常量判断，无法区分单元测试/集成测试等细分环境。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>判断依赖编译期/classpath 常量，简单直接；复杂环境区分需结合 Spring Profile 等。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>判断依据</b></p>
 * <ul>
 *   <li>基于 {@code CTool4jTestConstants.IS_TEST} 常量判断（测试环境 classpath 包含 JUnit 时该常量恒为 true）。</li>
 *   <li>{@code isNotTest()} = {@code !isTest()}，语义自洽。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CTestUtils {

    /**
     * 是否是测试
     * <ul>
     *   <li>{@code isTest()}：是否处于测试环境</li>
     * </ul>
     *
     * @return 结果
     */
    public boolean isTest() {
        return CTool4jTestConstants.IS_TEST;
    }

    /**
     * 是否不是测试
     * <ul>
     *   <li>{@code isNotTest()}：是否不处于测试环境</li>
     * </ul>
     *
     * @return 结果
     */
    public boolean isNotTest() {
        return !isTest();
    }

}
