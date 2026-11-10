package com.c332030.ctool4j.spring.security.config;

/**
 * <p>
 * Description: ICRequestMatchersConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICRequestMatchersConfig}：放行路径配置接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>定义 getPermitAllPaths 等</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>放行路径扩展</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口定义</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口定义</p>
 *
 * @since 2026/1/24
 * @version 1.0
 */
public interface ICRequestMatchersConfig {

    /**
     * 获取放行路径
     * @return 放行路径
     */
    String[] getPermits();

    /**
     * 获取拒绝路径
     * @return 拒绝路径
     */
    String[] getDenies();

}
