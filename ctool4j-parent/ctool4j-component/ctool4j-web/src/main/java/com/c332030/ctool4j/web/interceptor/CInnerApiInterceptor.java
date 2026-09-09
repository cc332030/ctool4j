package com.c332030.ctool4j.web.interceptor;

import com.c332030.ctool4j.definition.annotation.CInnerApi;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CIpUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.config.CInnerApiConfig;
import com.c332030.ctool4j.web.util.CServletUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * <p>
 * Description: 内部接口 IP 白名单拦截器
 * </p>
 *
 * <p>对标注 {@code @CInnerApi} 的 Controller 类/方法做 IP 白名单校验：
 * 取客户端 IP，若命中配置类 {@code CInnerApiConfig#allowedIps} 中任一 IP/IP 段(CIDR)则放行，
 * 否则拒绝访问（HTTP 403）。白名单为空时全部放行。</p>
 *
 * @author c332030
 * @since 2026/9/9
 * @see "doc/design/web/CInnerApiInterceptor.adoc"
 */
@CustomLog
@Component
@AllArgsConstructor
public class CInnerApiInterceptor implements ICHandlerInterceptor {

    private final CInnerApiConfig config;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        if (!isInternalInterface(handler)) {
            return true;
        }
        Set<String> allowedIps = config.getAllowedIps();
        if (allowedIps == null || allowedIps.isEmpty()) {
            return true;
        }
        String clientIp = CRequestUtils.getIp(request);
        if (CIpUtils.contains(clientIp, allowedIps)) {
            return true;
        }
        log.warn("内部接口 IP 白名单校验不通过，拒绝访问，clientIp: {}", clientIp);
        CServletUtils.writeJson(
            response,
            HttpStatus.FORBIDDEN,
            CStrResult.error(HttpStatus.FORBIDDEN, "非内部 IP 白名单地址，禁止访问")
        );
        return false;
    }

    /**
     * 当前请求是否属于内部接口（标注 {@code @CInnerApi}）
     *
     * @param handler 处理器
     * @return 是否为内部接口
     */
    private boolean isInternalInterface(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.getMethodAnnotation(CInnerApi.class) != null) {
            return true;
        }
        // @Inherited：标注在（抽象）父类类级时子类继承，也覆盖具体 Controller 类级标注
        return handlerMethod.getBeanType().isAnnotationPresent(CInnerApi.class);
    }

}
