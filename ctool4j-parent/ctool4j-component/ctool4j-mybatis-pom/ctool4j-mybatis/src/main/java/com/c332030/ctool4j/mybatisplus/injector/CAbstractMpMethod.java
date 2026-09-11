package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;

/**
 * <p>
 * Description: CAbstractMpMethod
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAbstractMpMethod}：MyBatis-Plus 注入方法抽象基类。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>基于 ICMpSqlMethod 定义注入的 SQL 方法与语句，作为自定义注入方法的基类</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>子类实现具体注入逻辑</p>
 * <h2>适用范围</h2>
 * <p>SQL 注入方法扩展</p>
 * <h2>不适用与边界场景</h2>
 * <p>继承 AbstractMethod</p>
 * <h2>已知限制与取舍</h2>
 * <p>继承 AbstractMethod</p>
 *
 * @author c332030
 * @since 2024/5/7
 * @version 1.0
 */
public abstract class CAbstractMpMethod extends AbstractMethod implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    /**
     * 注入的 SQL 方法枚举
     */
    protected final ICMpSqlMethod sqlMethod;

    /**
     * 构造方法
     *
     * @param sqlMethodEnum 注入的 SQL 方法枚举
     */
    public CAbstractMpMethod(ICMpSqlMethod sqlMethodEnum) {
        super(sqlMethodEnum.getMethod());
        sqlMethod = sqlMethodEnum;
    }

}
