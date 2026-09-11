package com.c332030.ctool4j.web.cors.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.web.cors.CCorsConfig;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * Description: CCorsUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsUtils}（{@code @UtilityClass} + {@code @CAutowiredScan}）为跨域处理的静态工具类， 持有 {@code @CAutowired} 注入的 {@code CCorsConfig config}，供 {@code CCorsFilter}/{@code CCorsInterceptor}/ {@code CCorsResponseBodyAdvice} 复用。</p>
 * <p>核心方法：</p>
 * <ul>
 *   <li>结束预检并返回 true；否则返回 false。预检的 CORS 响应头由 {@code handle}/{@code handleDo} 在处理链中先行设置。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>配置未注入（config 为 null）</td>
 *     <td>NPE 被外层 try-catch 捕获，记录日志，跨域头不输出</td>
 *   </tr>
 *   <tr>
 *     <td>{@code handleDo} 校验链不通过（非同源/不在白名单/方法不允许）</td>
 *     <td>不设置任何跨域头，直接返回</td>
 *   </tr>
 *   <tr>
 *     <td>请求无 {@code Origin} 头</td>
 *     <td>视为非跨域请求，直接返回</td>
 *   </tr>
 *   <tr>
 *     <td>{@code allowedHeaders}/{@code exposedHeaders} 集合为 null 或空</td>
 *     <td>经 {@code setHeaderIfNotEmpty} 不设置对应响应头（避免空指针，空即不声明）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>跨域请求需要动态回显 Origin、按配置白名单放行来源与方法的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code enable=false} 时不做处理；{@code Origin} 为 null（非浏览器跨域）不处理。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>采用"回显 Origin + 白名单校验"而非 {@code *} 通配，因此支持 {@code Allow-Credentials: true}（带凭据跨域）。</li>
 *   <li>{@code CUrlUtils.getHostWithPort} 归一化 Origin 后再与 HOST 比较判断是否同源。</li>
 *   <li>预检请求的 CORS 头依赖 {@code handle}/{@code handleDo} 在处理链中先行设置，{@code handleOptions} 本身不再设置（有意设计）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>开启开关</b></p>
 * <ul>
 *   <li>由 {@code CBoolUtils.isTrue(config.getEnable())} 控制；未开启时不输出任何跨域头。</li>
 * </ul>
 * <p><b>异常兜底</b></p>
 * <ul>
 *   <li>{@code handle}/{@code handleOptions} 外层 try-catch 捕获 Throwable 记录 error 日志，避免跨域处理异常影响主流程。</li>
 * </ul>
 *
 * @since 2026/1/9
 * @version 1.0
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CCorsUtils {

    @Setter
    @CAutowired
    CCorsConfig config;

    /**
     * 处理 OPTIONS 预检请求，直接返回 204
     *
     * <p>预检请求的 CORS 响应头由 {@link #handle} / {@link #handleDo} 在处理链中先行设置，
     * 此处仅负责以 204 状态码结束预检请求，不再重复设置响应头（有意设计）</p>
     * <ul>
     *   <li>{@code handleOptions(request, response)}：{@code enable=true} 且为 OPTIONS 预检请求时，以 204 状态码</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     * @return true 表示本次为预检请求且已处理
     */
    public boolean handleOptions(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (CBoolUtils.isTrue(config.getEnable())) {
                if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                    log.debug("deal OPTIONS request");
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return true;
                }
            }
        } catch (Throwable e) {
            log.error("deal OPTIONS failure", e);
        }

        return false;
    }

    /**
     * 处理跨域请求，开启跨域时设置响应头
     * <ul>
     *   <li>{@code handle(request, response)}：{@code enable=true} 时调用 {@code handleDo} 设置跨域响应头。</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (CBoolUtils.isTrue(config.getEnable())) {
                handleDo(request, response);
            }
        } catch (Throwable e) {
            log.error("cors handle failure", e);
        }

    }

    /**
     * 按配置校验并设置跨域响应头
     *
     * <h2>handleDo 校验链</h2>
     * <ul>
     *   <li>校验请求来源 header {@code Origin} 是否为空 → 是否同源 → 是否在 {@code allowedOrigins} 中 →</li>
     *   <li>请求方法是否在 {@code allowedMethods}（含 {@code *} 通配）中。</li>
     *   <li>全部通过后设置响应头：</li>
     *   <li>{@code Access-Control-Allow-Origin} 回显请求 Origin</li>
     *   <li>{@code Access-Control-Allow-Headers} / {@code Access-Control-Expose-Headers}：头集合经统一私有方法</li>
     *   <li>{@code setHeaderIfNotEmpty}（集合为 null/空则不设置）拼接 {@code joinHeaders}（含 {@code *} 用 {@code *}，否则逗号连接）</li>
     *   <li>{@code Access-Control-Allow-Credentials}：固定 {@code true}</li>
     *   <li>{@code Access-Control-Allow-Methods}：回显当前请求方法</li>
     *   <li>{@code Access-Control-Expose-Headers}：默认仅暴露 {@code Authorization}（浏览器脚本默认仅可读简单响应头，</li>
     *   <li>需显式暴露）；集合为空则不设置该头</li>
     * </ul>
     * <ul>
     *   <li>{@code handleDo(request, response)}：按配置校验并设置跨域响应头。</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应*/
    public void handleDo(HttpServletRequest request, HttpServletResponse response) {

        val origin = request.getHeader(HttpHeaders.ORIGIN);
        if (StrUtil.isEmpty(origin)) {
            log.debug("Not cors");
            return;
        }

        val newOrigin = CUrlUtils.getHostWithPort(origin);
        // 同源，避免打印日志误导
        if (Objects.equals(request.getHeader(HttpHeaders.HOST), newOrigin)) {
            return;
        }

        if (!config.getAllowedOrigins().contains(newOrigin)) {
            log.debug("Not allow origin: {}", origin);
            return;
        }

        val allowedMethods = config.getAllowedMethods();
        val method = request.getMethod();
        if (!CCollUtils.containsAny(allowedMethods, CCorsConfig.ALL, method)) {
            log.info("Not allow origin with method: {} {}", method, origin);
            return;
        }

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        // 允许当前请求方法类型
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, method);

        setHeaderIfNotEmpty(response, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, config.getAllowedHeaders());
        // 暴露给浏览器脚本可读的响应头（默认仅简单响应头可读，如 Authorization 需显式暴露）
        setHeaderIfNotEmpty(response, HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, config.getExposedHeaders());

    }

    /**
     * 头集合非空时，拼接（含 {@link CCorsConfig#ALL} 用 {@code *}）并设置响应头；集合为 null 或空则均不设置
     *
     * @param response   响应
     * @param headerName 响应头名
     * @param headers    头集合
     */
    public void setHeaderIfNotEmpty(
        HttpServletResponse response,
        String headerName,
        Set<String> headers
    ) {
        if (CollUtil.isEmpty(headers)) {
            return;
        }
        response.setHeader(headerName, joinHeaders(headers));
    }

    /**
     * 将头集合转为逗号分隔的头值；集合含 {@link CCorsConfig#ALL} 时直接使用 {@code *} 通配
     *
     * @param headers 头集合（已保证非 null 非空，由 {@link #setHeaderIfNotEmpty} 调用）
     * @return 头值
     */
    public String joinHeaders(Set<String> headers) {
        return headers.contains(CCorsConfig.ALL)
            ? CCorsConfig.ALL
            : CollUtil.join(headers, ",");
    }

}
