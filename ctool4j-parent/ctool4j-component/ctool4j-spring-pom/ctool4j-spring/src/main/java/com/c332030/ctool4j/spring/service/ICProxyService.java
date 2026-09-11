package com.c332030.ctool4j.spring.service;

import org.springframework.aop.framework.AopContext;

/**
 * <p>
 * Description: ICProxyService
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICProxyService}：代理服务接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>提供代理相关能力</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>代理服务</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口定义</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口定义</p>
 *
 * @since 2024/3/15
 * @version 1.0
 */
public interface ICProxyService<T> {

    /**
     * 获取当前代理对象
     * @return 当前代理对象
     */
    @SuppressWarnings("unchecked")
    default T currentProxy() {
        return (T)AopContext.currentProxy();
    }

}
