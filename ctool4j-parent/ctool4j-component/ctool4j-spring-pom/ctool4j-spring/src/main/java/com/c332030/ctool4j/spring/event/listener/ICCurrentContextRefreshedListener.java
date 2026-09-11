package com.c332030.ctool4j.spring.event.listener;

import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <p>
 * Description: ICCurrentContextRefreshedListener
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>监听 ContextRefreshedEvent 且为当前上下文</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>上下文刷新处理</p>
 * <h2>不适用与边界场景</h2>
 * <p>继承 ApplicationListener</p>
 * <h2>已知限制与取舍</h2>
 * <p>继承 ApplicationListener</p>
 *
 * @since 2025/11/17
 * @version 1.0
 */
public interface ICCurrentContextRefreshedListener extends ICSpringSourceApplicationListener<ContextRefreshedEvent> {

}
