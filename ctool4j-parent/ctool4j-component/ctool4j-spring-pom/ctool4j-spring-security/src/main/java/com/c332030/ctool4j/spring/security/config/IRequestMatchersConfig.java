package com.c332030.ctool4j.spring.security.config;

/**
 * <p>
 * Description: IRequestMatchersConfig
 * </p>
 *
 * @since 2026/1/24
 */
public interface IRequestMatchersConfig {

    String[] getPermits();

    String[] getDenies();

}
