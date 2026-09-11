package com.c332030.ctool4j.log.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICLogLevel
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICLogLevel} 定义日志级别提供能力：</p>
 * <ul>
 *   <li>{@code Level getLevel()}：返回日志级别（SLF4J {@code org.slf4j.event.Level}）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无（接口方法由实现决定）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>声明某组件/类的日志级别，供日志配置、过滤等使用。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不直接提供级别，需子接口或实现指定。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>级别枚举依赖 SLF4J {@code Level}，不自定义级别集合。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>接口语义</b></p>
 * <ul>
 *   <li>作为日志级别的公共父接口，由实现方/子接口提供具体级别。</li>
 *   <li>各子接口（{@code ICLogLevelTrace}/{@code ICLogLevelDebug}/{@code ICLogLevelInfo}/{@code ICLogLevelWarn}/{@code ICLogLevelError}）以 default 方法固定返回对应级别。</li>
 * </ul>
 *
 * @since 2026/3/20
 * @version 1.0
 */
public interface ICLogLevel {

    /**
     * 获取日志级别
     * @return 日志级别
     */
    Level getLevel();

}
