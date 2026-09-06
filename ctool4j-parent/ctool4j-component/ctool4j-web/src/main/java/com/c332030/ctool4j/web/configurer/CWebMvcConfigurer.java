package com.c332030.ctool4j.web.configurer;

import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import com.c332030.ctool4j.spring.util.CSpringHttpUtils;
import com.c332030.ctool4j.web.doc.CParameterMethodArgumentResolver;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
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
        CCollUtils.forEach(icHandlerInterceptors, registry::addInterceptor);
    }

    /**
     * 注册参数解析器：支持 @CParameter 驱动 SpringMVC 绑定（required 与文档一致）
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CParameterMethodArgumentResolver());
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
