package com.c332030.ctool4j.spring.bean;

import com.c332030.ctool4j.core.util.COpt;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class CSpringBeans implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        CSpringBeans.applicationContext = applicationContext;
    }

    /**
     * 获取当前应用上下文，未初始化时可能为空
     * @return 当前应用上下文
     */
    public static COpt<ApplicationContext> getApplicationContextOpt() {
        return COpt.ofNullable(applicationContext);
    }

    /**
     * 获取当前应用上下文
     * @return 当前应用上下文
     */
    public static ApplicationContext getApplicationContext() {
        return getApplicationContextOpt()
            .orElseThrow(() -> new IllegalStateException("applicationContext is null"));
    }

}
