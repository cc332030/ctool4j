package com.c332030.ctool4j.spring.security.config;

/**
 * <p>
 * Description: ICRequestMatchersConfig
 * </p>
 *
 * @since 2026/1/24
 */
public interface ICRequestMatchersConfig {

    String[] getPermits();

    String[] getDenies();

}
