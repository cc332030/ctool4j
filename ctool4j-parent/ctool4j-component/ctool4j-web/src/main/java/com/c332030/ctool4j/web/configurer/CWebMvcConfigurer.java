package com.c332030.ctool4j.web.configurer;

import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import com.c332030.ctool4j.spring.util.CSpringHttpUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
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
 * @see "doc/design/web/CWebMvcConfigurer.adoc"
 */
@CustomLog
@Configuration
@AllArgsConstructor
public class CWebMvcConfigurer implements WebMvcConfigurer {

    Collection<ICHandlerInterceptor> icHandlerInterceptors;

    CSpringJacksonConfig jacksonConfig;

    /**
     * 注册所有处理器拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        val count = icHandlerInterceptors.size();
        log.debug("注册 {} 个 ctool4j 处理器拦截器", count);
        CCollUtils.forEach(icHandlerInterceptors, registry::addInterceptor);
    }

    /**
     * 扩展消息转换器：配置 Jackson 后统一转换器
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        CSpringHttpUtils.setJacksonConfig(jacksonConfig);
        CSpringHttpUtils.configureMessageConverters(converters);
    }

}
