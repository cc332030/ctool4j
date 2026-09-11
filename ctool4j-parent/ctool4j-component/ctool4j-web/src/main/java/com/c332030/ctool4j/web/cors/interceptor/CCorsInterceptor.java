package com.c332030.ctool4j.web.cors.interceptor;

import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.CustomLog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CCorsInterceptor
 * </p>
 *
 * <p>
 * CORS 备用方案：Filter 已自动生效时无需注册本拦截器；
 * 需要时由使用方手动注册到拦截器链
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsInterceptor} 为跨域的<b>备用方案</b>（{@code @Component} 已被注释，需使用方手动注册到拦截器链）， 实现 {@code ICHandlerInterceptor}（继承 {@code HandlerInterceptor}）。</p>
 * <p>核心方法 {@code preHandle(request, response, handler)}：</p>
 * <ul>
 *   <li>调用 {@code CCorsUtils.handle(request, response)} 输出 CORS 头</li>
 *   <li>返回 {@code !CCorsUtils.handleOptions(request, response)}：</li>
 *   <li>OPTIONS 预检请求返回 false（停止处理），否则返回 true（继续）</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Filter 已自动生效时无需注册本拦截器；供需要拦截器链场景手动注册。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code CCorsUtils.handle}/{@code handleOptions} 内部异常</td>
 *     <td>被 CCorsUtils 内部 try-catch 记录日志</td>
 *   </tr>
 *   <tr>
 *     <td>OPTIONS 预检</td>
 *     <td>返回 false 停止处理（以 204 结束）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>无法使用 Filter 的拦截器链场景手动注册。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>Filter 已生效时无需使用；未注册则本拦截器不生效。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>需手动注册；跨域处理统一收敛在 {@code CCorsUtils}。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>预检处理</b></p>
 * <ul>
 *   <li>OPTIONS 预检由 {@code CCorsUtils.handleOptions} 处理，preHandle 返回 false 结束。</li>
 * </ul>
 *
 * @since 2025/9/28
 */
@CustomLog
//@Component
public class CCorsInterceptor implements ICHandlerInterceptor {

    /**
     * 请求前处理：输出 CORS 头，OPTIONS 预检请求返回 false
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否继续处理
     */
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {

        CCorsUtils.handle(request, response);
        return !CCorsUtils.handleOptions(request, response);
    }

}
