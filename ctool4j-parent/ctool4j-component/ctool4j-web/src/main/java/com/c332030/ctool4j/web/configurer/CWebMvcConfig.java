package com.c332030.ctool4j.web.configurer;

import com.c332030.ctool4j.web.interceptor.CRequestLogHandlerInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 * Description: CWebMvcConfig
 * </p>
 *
 * @since 2025/9/28
 */
@Configuration
@AllArgsConstructor
public class CWebMvcConfig implements WebMvcConfigurer {

    CRequestLogHandlerInterceptor requestLogHandlerInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(requestLogHandlerInterceptor);
    }

}
