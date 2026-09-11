package com.c332030.ctool4j.definition.constant;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CTool4jTestConstants
 * </p>
 *
 * <p>
 * 本类保留 slf4j 原生 @Slf4j 而非 @CustomLog（原因详见设计文档）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTool4jTestConstants} 为测试环境常量。</p>
 * <p>{@code JUNIT_TEST_CLASS_NAME}：JUnit 测试类名；{@code IS_TEST}：通过能否加载 JUnit 判断是否测试环境。</p>
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
 *   <li>本类使用 slf4j 原生 {@code @Slf4j} 而非 {@code @CustomLog}：本类位于 {@code ctool4j-definition} 基础模块，</li>
 *   <li>{@code ctool4j-core} 依赖本模块，{@code @CustomLog} 生成的 {@code CLog} 位于 core 会形成反向依赖（循环依赖），</li>
 *   <li>故保留 {@code @Slf4j}。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class CTool4jTestConstants {

    /**
     * JUnit 测试类名
     */
    public final String JUNIT_TEST_CLASS_NAME = "org.junit.jupiter.api.Test";

    /**
     * 是否为测试环境（通过能否加载 JUnit 判断）
     */
    public final boolean IS_TEST = ((Supplier<Boolean>) () -> {
        try {

            Class.forName(JUNIT_TEST_CLASS_NAME);
            return true;
        } catch (Throwable e) {
            log.debug("check junit result exception", e);
        }
        return false;
    }).get();

}
