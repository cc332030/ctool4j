package com.c332030.ctool4j.mybatisplus.injector;

/**
 * <p>
 * Description: ICMpMethod
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICMpMethod}：注入方法接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>定义获取 SQL 方法名</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>注入方法</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2026/1/6
 * @version 1.0
 */
public interface ICMpMethod {

    /**
     * 获取 SQL 方法名
     * @param sqlMethod SQL 方法
     * @return 方法名
     */
    default String getMethod(ICMpSqlMethod sqlMethod) {
        return sqlMethod.getMethod();
    }

}
