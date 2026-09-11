package com.c332030.ctool4j.mybatisplus.injector;

/**
 * <p>
 * Description: ICMpSqlMethod
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICMpSqlMethod}：SQL 方法接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>定义方法名/描述/SQL 获取</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>SQL 方法</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @author c332030
 * @since 2024/5/7
 * @version 1.0
 */
public interface ICMpSqlMethod {

    /**
     * 获取方法名
     * @return 方法名
     */
    String getMethod();

    /**
     * 获取方法描述
     * @return 方法描述
     */
    String getDesc();

    /**
     * 获取 SQL 语句
     * @return SQL 语句
     */
    String getSql();

}
