package com.c332030.ctool4j.spring.service;

import org.springframework.aop.framework.AopContext;

/**
 * <p>
 * Description: ICProxyService
 * </p>
 *
 * @since 2024/3/15
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
