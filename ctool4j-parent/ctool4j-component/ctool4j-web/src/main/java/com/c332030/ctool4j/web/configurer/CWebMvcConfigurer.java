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
 * <h2>能力目录</h2>
 * <p>{@code CWebMvcConfigurer} 为 web MVC 配置，{@code @Configuration} + {@code @AllArgsConstructor} + 实现 {@code WebMvcConfigurer}， 构造注入 {@code Collection&lt;ICHandlerInterceptor&gt;} 与 {@code CSpringJacksonConfig}。</p>
 * <p>核心方法：</p>
 * <ul>
 *   <li>{@code CSpringHttpUtils.setJacksonConfig(jacksonConfig)} 配置 Jackson，</li>
 *   <li>{@code CSpringHttpUtils.configureMessageConverters(converters)} 统一消息转换器</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无 {@code ICHandlerInterceptor}</td>
 *     <td>{@code CCollUtils.forEach} 空集合不执行，无拦截器注册</td>
 *   </tr>
 *   <tr>
 *     <td>{@code converters} 为空</td>
 *     <td>{@code configureMessageConverters} 处理空列表</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要自动注册项目拦截器与统一 Jackson 序列化的 web 应用。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>业务方自定义 {@code WebMvcConfigurer} 时需注意与框架约定的配置合并语义。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>拦截器与 Jackson 配置集中在框架层，业务方无需各自实现。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>拦截器注册</b></p>
 * <ul>
 *   <li>收集容器中所有 {@code ICHandlerInterceptor} bean，统一注册。</li>
 * </ul>
 * <p><b>Jackson 消息转换器</b></p>
 * <ul>
 *   <li>通过 {@code CSpringHttpUtils} 配置 Jackson 后统一消息转换器，保证序列化行为一致。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
@CustomLog
@Configuration
@AllArgsConstructor
public class CWebMvcConfigurer implements WebMvcConfigurer {

    Collection<ICHandlerInterceptor> icHandlerInterceptors;

    CSpringJacksonConfig jacksonConfig;

    /**
     * 注册所有处理器拦截器
     * <ul>
     *   <li>{@code addInterceptors(InterceptorRegistry registry)}：将全部 {@code ICHandlerInterceptor} 注册到拦截器链</li>
     * </ul>
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
     * <ul>
     *   <li>{@code extendMessageConverters(List&lt;HttpMessageConverter&lt;?&gt;&gt; converters)}：</li>
     * </ul>
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        CSpringHttpUtils.setJacksonConfig(jacksonConfig);
        CSpringHttpUtils.configureMessageConverters(converters);
    }

}
