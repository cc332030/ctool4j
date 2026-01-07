package com.c332030.ctool4j.web.configurer;

import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;

/**
 * <p>
 * Description: CWebMvcConfig
 * </p>
 *
 * @since 2025/9/28
 */
@Configuration
@AllArgsConstructor(onConstructor_ = @Autowired(required = false))
public class CWebMvcConfig implements WebMvcConfigurer {

    Collection<ICHandlerInterceptor> icHandlerInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        CCollUtils.forEach(icHandlerInterceptors, registry::addInterceptor);

    }

}
