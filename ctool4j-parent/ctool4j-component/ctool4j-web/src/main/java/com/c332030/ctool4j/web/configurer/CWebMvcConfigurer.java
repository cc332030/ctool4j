package com.c332030.ctool4j.web.configurer;

import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.spring.util.CSpringHttpUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: CWebMvcConfigurer
 * </p>
 *
 * @since 2025/9/28
 */
@Configuration
@AllArgsConstructor
public class CWebMvcConfigurer implements WebMvcConfigurer {

    Collection<ICHandlerInterceptor> icHandlerInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        CCollUtils.forEach(icHandlerInterceptors, registry::addInterceptor);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        CSpringHttpUtils.configureMessageConverters(converters);
    }

}
