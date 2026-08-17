package com.c332030.ctool4j.spring.security.config;

/**
 * <p>
 * Description: ICRequestMatchersConfig
 * </p>
 *
 * @since 2026/1/24
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
