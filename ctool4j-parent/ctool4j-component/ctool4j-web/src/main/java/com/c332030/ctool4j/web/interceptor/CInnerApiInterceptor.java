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
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>非内部接口（未标注 {@code CInnerApi}）</td>
 *     <td>直接放行</td>
 *   </tr>
 *   <tr>
 *     <td>白名单为空（未配置 {@code allowed-ips}）</td>
 *     <td>全部放行</td>
 *   </tr>
 *   <tr>
 *     <td>客户端 IP 非法/为空</td>
 *     <td>{@code CIpUtils} 判定不命中，拒绝（安全优先）</td>
 *   </tr>
 *   <tr>
 *     <td>白名单规则非法</td>
 *     <td>{@code CIpUtils} 记录日志并跳过该规则，不影响其它规则</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <p>内部管理接口、继承 {@code CMpController} 的具体 Controller（在类或方法上标注 {@code CInnerApi}），限制仅内网 IP 调用。</p>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>非 web MVC（无 {@code HandlerMethod}）调用不适用；非 {@code HandlerMethod}（如静态资源）一律放行，不影响静态资源访问。</li>
 *   <li>对外公开接口不应标注，避免误拦截。</li>
 *   <li>客户端 IP 非法/为空时按"不命中"拒绝（安全优先），避免空 IP 绕过。</li>
 *   <li>拒绝响应：HTTP 403 + {@code CStrResult.error(FORBIDDEN)}，经 {@code CServletUtils.writeJson} 写 JSON（不抛异常进异常处理器）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li><b>IP 来源可伪造</b>：依赖 {@code CRequestUtils.getIp}，其当前无条件信任 {@code X-Forwarded-For} 首段，客户端直连时可伪造该请求头绕过 IP 白名单（与 {@code CRequestUtils} 既有已知限制一致）。<b>生产对外场景必须配合可信代理，并在代理处清理/覆盖 {@code X-Forwarded-For}，否则本拦截器可能被绕过</b>。</li>
 *   <li><b>标注≠生效</b>：仅在标注 {@code CInnerApi} 且 {@code CInnerApiConfig.allowed-ips} 非空时校验才真正收紧；忘配白名单则仍全部放行（见 {@code CInnerApiConfig}）。</li>
 *   <li><b>{@code @Inherited} 仅作用于类级</b>：标在父类类级时子类 Controller 继承识别；方法级标注不随继承传播，需标在具体方法上。</li>
 * </ul>
 * <h2>波及影响</h2>
 * <ul>
 *   <li>作为 {@code @Component implements ICHandlerInterceptor}，凡使用 ctool4j-web 且启用了拦截器收集机制的应用均会注册本拦截器；其行为（识别 {@code CInnerApi}、拒绝 403）变化会影响所有启用方，需评估兼容。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>生效链路</b></p>
 * <p>请求 → {@code CWebMvcConfigurer.addInterceptors} 注册的全部 {@code ICHandlerInterceptor} → {@code preHandle}：</p>
 * <ul>
 *   <li>{@code handler} 非 {@code HandlerMethod}（如静态资源）直接放行；</li>
 *   <li>方法标注 {@code CInnerApi}，或类标注 {@code CInnerApi}（含 {@code @Inherited} 从抽象父类继承）则视为内部接口；</li>
 *   <li>内部接口取客户端 IP（{@code CRequestUtils.getIp}）→ {@code CIpUtils.contains} 校验白名单 → 命中放行，否则拒绝。</li>
 * </ul>
 * <p><b>注解识别</b></p>
 * <ul>
 *   <li>方法级优先：{@code handlerMethod.getMethodAnnotation(CInnerApi.class)}。</li>
 *   <li>类级兜底：{@code handlerMethod.getBeanType().isAnnotationPresent(CInnerApi.class)}。</li>
 *   <li>注解 {@code @Inherited} 使标注在抽象父类类级时，子类 Controller 自动识别。</li>
 * </ul>
 * <p><b>拒绝响应</b></p>
 * <ul>
 *   <li>未命中白名单时：HTTP 403 FORBIDDEN + {@code CStrResult.error(HttpStatus.FORBIDDEN, ...)}，经 {@code CServletUtils.writeJson} 输出 JSON。</li>
 * </ul>
 * <p><b>性能</b></p>
 * <ul>
 *   <li>拦截器全路径注册（由 {@code CWebMvcConfigurer} 统一收集），对<b>每个</b>请求都会进入 {@code preHandle}；内部仅做一次注解判定 + 命中校验，开销低。注解判定用 {@code getMethodAnnotation}/{@code isAnnotationPresent}，非反射调用，可忽略。</li>
 * </ul>
 * <p><b>测试</b></p>
 * <ul>
 *   <li>已提供 {@code CInnerApiInterceptorTests}，覆盖非 HandlerMethod、类/方法级注解识别、{@code @Inherited} 父类类级继承、白名单空、命中、未命中拒绝(403)、X-Forwarded-For 首段命中/未命中等路径。</li>
 *   <li>未覆盖：真实 Spring MVC 容器集成场景、{@code @Inherited} 在真实代理(如 CGLIB)下的场景。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/9
 * @version 1.0
 */
@CustomLog
@Component
@AllArgsConstructor
public class CInnerApiInterceptor implements ICHandlerInterceptor {

    private final CInnerApiConfig config;

    /**
     * 对标注 {@code @CInnerApi} 的内部接口做 IP 白名单校验。
     *
     * <p>非内部接口（handler 非 {@code HandlerMethod} 或未标注注解）、白名单为空、客户端 IP 命中白名单时直接放行；
     * 未命中时写 403 JSON 响应并中断请求。客户端 IP 取自 {@code CRequestUtils.getIp}（无条件信任
     * {@code X-Forwarded-For} 首段，属已知安全取舍，见类级说明）。</p>
     *
     * @param request  当前请求，用于取客户端 IP
     * @param response 当前响应，校验不通过时写入 403 JSON
     * @param handler  被调用的处理器，用于判定是否内部接口
     * @return true 放行；false 中断请求（已写 403 响应）
     */
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
        // 已知取舍：getIp 无条件信任 X-Forwarded-For 首段，客户端直连时可伪造该头绕过白名单（安全缺陷）。
        // 生产对外场景须配合可信代理清理/覆盖 X-Forwarded-For（见 CRequestUtils.adoc 既有已知限制）；暂按此取舍保留。
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
