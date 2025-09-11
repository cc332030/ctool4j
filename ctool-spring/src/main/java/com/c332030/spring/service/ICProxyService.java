package com.c332030.spring.service;

import org.springframework.aop.framework.AopContext;

/**
 * <p>
 * Description: ICProxyService
 * </p>
 *
 * @since 2024/3/15
 */
public interface ICProxyService<T> {

    @SuppressWarnings("unchecked")
    default T currentProxy() {
        return (T)AopContext.currentProxy();
    }

}
