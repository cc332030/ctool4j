package com.c332030.ctool4j.spring.lifecycle;

import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * <p>
 * Description: ICSpringInit
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICSpringInit}：Spring 生命周期初始化接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>定义 onInit 回调</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>Spring 初始化扩展</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现类需提供 onInit</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现类需提供 onInit</p>
 *
 * @since 2025/11/28
 * @version 1.0
 */
public interface ICSpringInit extends SmartInitializingSingleton {

    /**
     * 单例 Bean 实例化后触发初始化
     */
    @Override
    default void afterSingletonsInstantiated() {
        onInit();
    }

    /**
     * 初始化
     */
    void onInit();

}
