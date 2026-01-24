package com.c332030.ctool4j.spring.security.config;

import java.util.List;

/**
 * <p>
 * Description: IRequestMatchersConfig
 * </p>
 *
 * @since 2026/1/24
 */
public interface IRequestMatchersConfig {

    List<String> getPermits();

    List<String> getDenies();

}
