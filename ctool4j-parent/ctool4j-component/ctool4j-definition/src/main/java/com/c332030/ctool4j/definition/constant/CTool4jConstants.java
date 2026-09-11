package com.c332030.ctool4j.definition.constant;

import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CTool4jConstants
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTool4jConstants} 为项目基础常量。</p>
 * <p>{@code BASE_PACKAGE}：基础包名 {@code com.c332030.ctool4j}。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>纯常量类：仅声明静态常量，不含逻辑。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>常量为编译期字面量，无运行时兜底。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>供业务代码/日志序列化/测试环境判断使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>常量值以编译期字面量内联到调用方，修改后调用方需重新编译方可生效。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 */
@UtilityClass
public class CTool4jConstants {

    /**
     * 基础包名
     */
    public final String BASE_PACKAGE = "com.c332030.ctool4j";

}
