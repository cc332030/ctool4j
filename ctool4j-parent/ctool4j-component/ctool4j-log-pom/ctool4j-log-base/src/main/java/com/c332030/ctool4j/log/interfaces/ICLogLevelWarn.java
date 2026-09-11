package com.c332030.ctool4j.log.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICLogLevelWarn
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICLogLevelWarn}（extends {@code ICLogLevel}）以 default 方法固定返回日志级别 {@code Level.WARN}。</p>
 * <h2>兜底设计</h2>
 * <p>无（default 方法固定返回值）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>声明某组件的日志级别为 WARN。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不用于动态级别调整。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>级别固定，如需动态级别不应使用本接口。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>固定级别</b></p>
 * <ul>
 *   <li>重写 {@code getLevel()} 默认返回 {@code Level.WARN}。</li>
 * </ul>
 *
 * @since 2026/3/20
 * @version 1.0
 */
public interface ICLogLevelWarn extends ICLogLevel {

    /**
     * 获取日志级别
     * @return 日志级别
     */
    @Override
    default Level getLevel() {
        return Level.WARN;
    }

}
