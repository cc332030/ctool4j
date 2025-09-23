package com.c332030.ctool4j.spring.bean;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSpringBeans
 * </p>
 *
 * @since 2025/9/10
 */
@Component
public class CSpringBeans implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        CSpringBeans.applicationContext = applicationContext;
    }

}
