package com.c332030.ctool4j.spring.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSpringBeans
 * </p>
 *
 * @since 2025/9/10
 */
@Component
@AllArgsConstructor
public class CSpringBeans {

    @Getter
    private static ApplicationContext applicationContext;

    @EventListener(ContextRefreshedEvent.class)
    public void init(ContextRefreshedEvent event) {
        applicationContext = event.getApplicationContext();
    }

}
